package ru.yandex.kardomoblieapp.security.userservice;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import ru.yandex.kardomoblieapp.security.jwt.model.Token;
import ru.yandex.kardomoblieapp.security.jwt.repository.DeactivatedTokenRepository;

import java.time.Instant;

@RequiredArgsConstructor
public class TokenAuthenticationUserDetailsService
        implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private final DeactivatedTokenRepository deactivatedTokenRepository;

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken) throws UsernameNotFoundException {
        if (authenticationToken.getPrincipal() instanceof Token token) {
            return new TokenUser(token.getSubject(), "nopassword", true, true,
                    !this.deactivatedTokenRepository.existsById(token.getId()) && token.getExpiresAt().isAfter(Instant.now()),
                    true, token.getAuthorities().stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList(), token);
        }

        throw new UsernameNotFoundException("Principal must by of type 'Token'");
    }
}
