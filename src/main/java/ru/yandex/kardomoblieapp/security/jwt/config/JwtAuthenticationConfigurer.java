package ru.yandex.kardomoblieapp.security.jwt.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.yandex.kardomoblieapp.security.jwt.serializer.TokenSerilazer;

import java.util.Objects;

public class JwtAuthenticationConfigurer extends AbstractHttpConfigurer<JwtAuthenticationConfigurer, HttpSecurity> {

    private TokenSerilazer<String> refreshTokenStringSerializer = Objects::toString;

    private TokenSerilazer<String> accessTokenStringSerializer = Objects::toString;

    @Override
    public void init(HttpSecurity builder) throws Exception {
        var configurer = builder.getConfigurer(CsrfConfigurer.class);
        if (configurer != null) {
            configurer.ignoringRequestMatchers(new AntPathRequestMatcher("/users/tokens", HttpMethod.POST.name()));
        }
    }

    @Override
    public void configure(HttpSecurity builder) throws Exception {
        var requestJwtTokenFilter = new RequestJwtTokenFilter();
        requestJwtTokenFilter.setAccessTokenStringSerializer(this.accessTokenStringSerializer);
        requestJwtTokenFilter.setRefreshTokenStringSerializer(this.refreshTokenStringSerializer);
        builder.addFilterAfter(requestJwtTokenFilter, ExceptionTranslationFilter.class);
    }

    public JwtAuthenticationConfigurer refreshTokenStringSerializer(TokenSerilazer<String> refreshTokenStringSerializer) {
        this.refreshTokenStringSerializer = refreshTokenStringSerializer;
        return this;
    }

    public JwtAuthenticationConfigurer accessTokenStringSerializer(TokenSerilazer<String> accessTokenStringSerializer) {
        this.accessTokenStringSerializer = accessTokenStringSerializer;
        return this;
    }
}
