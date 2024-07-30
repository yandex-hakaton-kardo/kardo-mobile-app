package ru.yandex.kardomoblieapp.security.jwt.serialization;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.kardomoblieapp.security.jwt.model.AccessToken;

import java.text.ParseException;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class AccessTokenJwsStringDeserializer implements TokenDeserializer<String> {

    private final JWSVerifier jwsVerifier;

    @Override
    public AccessToken convert(String string) {
        try {
            var signedJWT = SignedJWT.parse(string);
            if (signedJWT.verify(jwsVerifier)) {
                var jwtClaimsSet = signedJWT.getJWTClaimsSet();
                return new AccessToken(UUID.fromString(jwtClaimsSet.getJWTID()), jwtClaimsSet.getSubject(),
                        jwtClaimsSet.getStringListClaim("authorities"),
                        jwtClaimsSet.getIssueTime().toInstant(),
                        jwtClaimsSet.getExpirationTime().toInstant());
            }
        } catch (ParseException | JOSEException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
