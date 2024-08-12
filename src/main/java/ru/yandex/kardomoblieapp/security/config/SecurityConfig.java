package ru.yandex.kardomoblieapp.security.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.yandex.kardomoblieapp.security.jwt.config.JwtAuthenticationConfigurer;
import ru.yandex.kardomoblieapp.security.jwt.repository.DeactivatedTokenRepository;
import ru.yandex.kardomoblieapp.security.jwt.serialization.AccessTokenJwsStringDeserializer;
import ru.yandex.kardomoblieapp.security.jwt.serialization.AccessTokenJwsStringSerializer;
import ru.yandex.kardomoblieapp.security.jwt.serialization.RefreshTokenJweStringDeserializer;
import ru.yandex.kardomoblieapp.security.jwt.serialization.RefreshTokenJweStringSerializer;

import java.text.ParseException;

import static ru.yandex.kardomoblieapp.user.model.UserRole.ADMIN;
import static ru.yandex.kardomoblieapp.user.model.UserRole.USER;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationConfigurer jwtAuthenticationConfigurer(
            @Value("${jwt.access-token-key}") String accessTokenKey,
            @Value("${jwt.refresh-token-key}") String refreshTokenKey,
            DeactivatedTokenRepository deactivatedTokenRepository) throws ParseException, JOSEException {

        return JwtAuthenticationConfigurer.builder()
                .accessTokenStringSerializer(new AccessTokenJwsStringSerializer(
                        new MACSigner(OctetSequenceKey.parse(accessTokenKey))
                ))
                .refreshTokenStringSerializer(new RefreshTokenJweStringSerializer(
                        new DirectEncrypter(OctetSequenceKey.parse(refreshTokenKey))
                ))
                .accessTokenStringDeserializer(new AccessTokenJwsStringDeserializer(
                        new MACVerifier(OctetSequenceKey.parse(accessTokenKey))
                ))
                .refreshTokenStringDeserializer(new RefreshTokenJweStringDeserializer(
                        new DirectDecrypter(OctetSequenceKey.parse(refreshTokenKey))
                ))
                .deactivatedTokenRepository(deactivatedTokenRepository)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConfigurer jwtAuthenticationConfigurer)
            throws Exception {

        http
                .with(jwtAuthenticationConfigurer, Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/content/**", HttpMethod.GET.name()).permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/users/register", HttpMethod.POST.name()).permitAll()
                        .requestMatchers("/users/{userId}", HttpMethod.DELETE.name()).hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers("/users/{userId}", HttpMethod.GET.name()).hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers("/users/{userId}/friends", HttpMethod.GET.name()).hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers("/users/{userId}/avatar", HttpMethod.DELETE.name()).hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers("/users/**").hasRole(USER.name())
                        .requestMatchers("/posts/{postId}", HttpMethod.DELETE.name()).hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers("/posts/{postId}/comment/{commentId}", HttpMethod.DELETE.name()).hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers("/posts/**", HttpMethod.GET.name()).hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers("/posts/**").hasRole(USER.name())
                        .requestMatchers("/events/**", HttpMethod.GET.name()).hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers("/events/**").hasRole(USER.name())
                        .requestMatchers("/events/{participationId}", HttpMethod.DELETE.name()).hasAnyRole(ADMIN.name(), USER.name())
                        .requestMatchers("/participations/**").hasRole(USER.name())
                        .requestMatchers("/admin/**").hasRole(ADMIN.name())
                        .requestMatchers("/actuator/**").hasRole(ADMIN.name())
                        .anyRequest().authenticated())

                .csrf(csrf -> csrf.ignoringRequestMatchers("/users/register"));

        return http.getOrBuild();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
