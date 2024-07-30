package ru.yandex.kardomoblieapp.security.jwt.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.yandex.kardomoblieapp.security.jwt.repository.DeactivatedTokenRepository;
import ru.yandex.kardomoblieapp.security.jwt.serialization.TokenDeserializer;
import ru.yandex.kardomoblieapp.security.jwt.serialization.TokenSerializer;
import ru.yandex.kardomoblieapp.security.userservice.TokenAuthenticationUserDetailsService;

import static ru.yandex.kardomoblieapp.security.SecurityUtils.LOGIN_PATH;

@Builder
public class JwtAuthenticationConfigurer extends AbstractHttpConfigurer<JwtAuthenticationConfigurer, HttpSecurity> {

    private TokenSerializer<String> refreshTokenStringSerializer;

    private TokenSerializer<String> accessTokenStringSerializer;

    private TokenDeserializer<String> refreshTokenStringDeserializer;

    private TokenDeserializer<String> accessTokenStringDeserializer;

    private DeactivatedTokenRepository deactivatedTokenRepository;

    @Override
    public void init(HttpSecurity builder) throws Exception {
        var configurer = builder.getConfigurer(CsrfConfigurer.class);
        if (configurer != null) {
            configurer.ignoringRequestMatchers(new AntPathRequestMatcher(LOGIN_PATH, HttpMethod.POST.name()));
        }
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        var requestJwtTokenFilter = new RequestJwtTokenFilter();
        requestJwtTokenFilter.setAccessTokenStringSerializer(this.accessTokenStringSerializer);
        requestJwtTokenFilter.setRefreshTokenStringSerializer(this.refreshTokenStringSerializer);

        var jwtAuthenticationFilter = new AuthenticationFilter(builder.getSharedObject(AuthenticationManager.class),
                new JwtAuthenticationConverter(this.accessTokenStringDeserializer, this.refreshTokenStringDeserializer));
        jwtAuthenticationFilter.setSuccessHandler((request, response, authentication) ->
                CsrfFilter.skipRequest(request));
        jwtAuthenticationFilter.setFailureHandler((request, response, exception) ->
                response.sendError(HttpServletResponse.SC_FORBIDDEN));

        var authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(
                new TokenAuthenticationUserDetailsService(this.deactivatedTokenRepository));

        var refreshTokenFilter = new RefreshTokenFilter();
        refreshTokenFilter.setAccessTokenStringSerializer(this.accessTokenStringSerializer);

        var jwtLogoutFilter = new JwtLogoutFilter(this.deactivatedTokenRepository);

        builder.addFilterAfter(requestJwtTokenFilter, ExceptionTranslationFilter.class);
        builder.addFilterAfter(refreshTokenFilter, ExceptionTranslationFilter.class);
        builder.addFilterAfter(jwtLogoutFilter, ExceptionTranslationFilter.class);
        builder.addFilterBefore(jwtAuthenticationFilter, CsrfFilter.class);
        builder.authenticationProvider(authenticationProvider);
    }
}
