package ru.yandex.kardomoblieapp.security.jwt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.yandex.kardomoblieapp.security.jwt.factory.DefaultAccessTokenFactory;
import ru.yandex.kardomoblieapp.security.jwt.factory.TokenFactory;
import ru.yandex.kardomoblieapp.security.jwt.model.AccessToken;
import ru.yandex.kardomoblieapp.security.jwt.model.RefreshToken;
import ru.yandex.kardomoblieapp.security.jwt.model.TokensResponse;
import ru.yandex.kardomoblieapp.security.jwt.serialization.TokenSerializer;
import ru.yandex.kardomoblieapp.security.userservice.TokenUser;

import java.io.IOException;

@Setter

public class RefreshTokenFilter extends OncePerRequestFilter {

    private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

    private TokenFactory<RefreshToken, AccessToken> accessTokenTokenFactory = new DefaultAccessTokenFactory();

    private TokenSerializer<String> accessTokenStringSerializer;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (this.securityContextRepository.containsContext(request)) {
            var context = this.securityContextRepository.loadDeferredContext(request).get();
            if (context != null && context.getAuthentication() instanceof PreAuthenticatedAuthenticationToken &&
                    context.getAuthentication().getPrincipal() instanceof TokenUser user &&
                    context.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("JWT_REFRESH"))) {
                var accessToken = this.accessTokenTokenFactory.getToken((RefreshToken) user.getToken());

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                this.objectMapper.writeValue(response.getWriter(),
                        new TokensResponse(this.accessTokenStringSerializer.convert(accessToken),
                                accessToken.getExpiresAt().toString(), null, null));
            }

            throw new AccessDeniedException("User must be authenticated with JWT");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getRequestURI().equals("/users/tokens/refresh");
    }
}
