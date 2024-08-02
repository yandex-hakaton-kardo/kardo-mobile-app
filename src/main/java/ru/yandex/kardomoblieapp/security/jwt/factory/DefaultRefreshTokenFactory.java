package ru.yandex.kardomoblieapp.security.jwt.factory;

import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import ru.yandex.kardomoblieapp.security.jwt.model.RefreshToken;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.UUID;

@Setter
public class DefaultRefreshTokenFactory implements TokenFactory<Authentication, RefreshToken> {

    private Duration tokenTtl = Duration.ofDays(1);

    @Override
    public RefreshToken getToken(Authentication authentication) {
        var now = Instant.now();
        var authorities = new LinkedList<String>();
        authorities.add("JWT_REFRESH");
        authorities.add("JWT_LOGOUT");
        authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> "GRANT_" + authority)
                .forEach(authorities::add);

        return new RefreshToken(UUID.randomUUID(), authentication.getName(), authorities, now, now.plus(this.tokenTtl));
    }
}
