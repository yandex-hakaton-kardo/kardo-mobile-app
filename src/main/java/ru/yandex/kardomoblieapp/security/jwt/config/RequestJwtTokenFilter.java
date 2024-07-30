package ru.yandex.kardomoblieapp.security.jwt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.yandex.kardomoblieapp.security.jwt.factory.DefaultAccessTokenFactory;
import ru.yandex.kardomoblieapp.security.jwt.factory.DefaultRefreshTokenFactory;
import ru.yandex.kardomoblieapp.security.jwt.factory.TokenFactory;
import ru.yandex.kardomoblieapp.security.jwt.model.AccessToken;
import ru.yandex.kardomoblieapp.security.jwt.model.RefreshToken;
import ru.yandex.kardomoblieapp.security.jwt.model.TokensResponse;
import ru.yandex.kardomoblieapp.security.jwt.serializer.TokenSerilazer;

import java.io.IOException;
import java.util.Objects;

@Setter
public class RequestJwtTokenFilter extends OncePerRequestFilter {

    private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

    private TokenFactory<Authentication, RefreshToken> refreshTokenTokenFactory = new DefaultRefreshTokenFactory();

    private TokenFactory<RefreshToken, AccessToken> accessTokenTokenFactory = new DefaultAccessTokenFactory();

    private TokenSerilazer<String> refreshTokenStringSerializer = Objects::toString;

    private TokenSerilazer<String> accessTokenStringSerializer = Objects::toString;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (this.securityContextRepository.containsContext(request)) {
            var context = this.securityContextRepository.loadDeferredContext(request).get();
            if (context != null && !(context.getAuthentication() instanceof PreAuthenticatedAuthenticationToken)) {
                var refreshToken = this.refreshTokenTokenFactory.getToken(context.getAuthentication());
                var accessToken = this.accessTokenTokenFactory.getToken(refreshToken);

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                String tokens = this.objectMapper.writeValueAsString(
                        new TokensResponse(this.accessTokenStringSerializer.convert(accessToken),
                                accessToken.getExpiresAt().toString(),
                                this.refreshTokenStringSerializer.convert(refreshToken),
                                refreshToken.getExpiresAt().toString()));
                response.getWriter().write(tokens);
                return;
            }
        }
        throw new AccessDeniedException("User must be authenticated.");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getRequestURI().equals("/users/tokens");
    }
}
