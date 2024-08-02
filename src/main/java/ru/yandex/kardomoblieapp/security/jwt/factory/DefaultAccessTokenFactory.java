package ru.yandex.kardomoblieapp.security.jwt.factory;

import lombok.Setter;
import ru.yandex.kardomoblieapp.security.jwt.model.AccessToken;
import ru.yandex.kardomoblieapp.security.jwt.model.RefreshToken;

import java.time.Duration;
import java.time.Instant;

@Setter
public class DefaultAccessTokenFactory implements TokenFactory<RefreshToken, AccessToken> {

    private Duration tokenTtl = Duration.ofMinutes(5);

    @Override
    public AccessToken getToken(RefreshToken refreshToken) {
        var now = Instant.now();
        var authorities = refreshToken.getAuthorities().stream()
                .filter(authority -> authority.startsWith("GRANT_"))
                .map(authority -> authority.substring("GRANT_".length()))
                .toList();

        return new AccessToken(refreshToken.getId(), refreshToken.getSubject(),
                authorities, now, now.plus(this.tokenTtl));
    }
}
