package ru.yandex.kardomoblieapp.security.jwt.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.yandex.kardomoblieapp.security.jwt.model.DeactivatedToken;
import ru.yandex.kardomoblieapp.security.jwt.repository.DeactivatedTokenRepository;
import ru.yandex.kardomoblieapp.security.userservice.TokenUser;

import java.io.IOException;

@RequiredArgsConstructor
@Setter
public class JwtLogoutFilter extends OncePerRequestFilter {

    private final DeactivatedTokenRepository deactivatedTokenRepository;

    private SecurityContextRepository securityContextRepository = new RequestAttributeSecurityContextRepository();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (this.securityContextRepository.containsContext(request)) {
            var context = this.securityContextRepository.loadDeferredContext(request).get();
            if (context != null && context.getAuthentication() instanceof PreAuthenticatedAuthenticationToken &&
                    context.getAuthentication().getPrincipal() instanceof TokenUser user &&
                    context.getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("JWT_LOGOUT"))) {
                DeactivatedToken deactivatedToken = new DeactivatedToken(user.getToken().getId(), user.getToken().getExpiresAt());
                this.deactivatedTokenRepository.save(deactivatedToken);

                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            throw new AccessDeniedException("User must be authenticated with JWT");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getRequestURI().equals("/users/logout");
    }
}
