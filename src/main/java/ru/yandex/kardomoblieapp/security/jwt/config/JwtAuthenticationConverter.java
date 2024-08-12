package ru.yandex.kardomoblieapp.security.jwt.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import ru.yandex.kardomoblieapp.security.jwt.serialization.TokenDeserializer;

import static ru.yandex.kardomoblieapp.security.SecurityUtils.BEARER_AUTH;

@RequiredArgsConstructor
public class JwtAuthenticationConverter implements AuthenticationConverter {

    private final TokenDeserializer<String> accessTokenJwsStringDeserializer;

    private final TokenDeserializer<String> refreshTokenStringDeserializer;

    @Override
    public Authentication convert(HttpServletRequest request) {
        var authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith(BEARER_AUTH)) {
            var token = authorization.substring(BEARER_AUTH.length());
            var accessToken = this.accessTokenJwsStringDeserializer.convert(token);
            if (accessToken != null) {
                return new PreAuthenticatedAuthenticationToken(accessToken, token);
            }

            var refreshToken = this.refreshTokenStringDeserializer.convert(token);
            if (refreshToken != null) {
                return new PreAuthenticatedAuthenticationToken(refreshToken, token);
            }
        }

        return null;
    }
}
