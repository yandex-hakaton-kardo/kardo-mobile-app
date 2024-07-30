package ru.yandex.kardomoblieapp.security.jwt.serialization;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.kardomoblieapp.security.jwt.model.RefreshToken;

import java.text.ParseException;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class RefreshTokenJweStringDeserializer implements TokenDeserializer<String> {

    private final JWEDecrypter jweDecrypter;

    @Override
    public RefreshToken convert(String string) {
        try {
            var encryptedJWT = EncryptedJWT.parse(string);
            encryptedJWT.decrypt(this.jweDecrypter);
            var jwtClaimsSet = encryptedJWT.getJWTClaimsSet();
            return new RefreshToken(UUID.fromString(jwtClaimsSet.getJWTID()), jwtClaimsSet.getSubject(),
                    jwtClaimsSet.getStringListClaim("authorities"),
                    jwtClaimsSet.getIssueTime().toInstant(),
                    jwtClaimsSet.getExpirationTime().toInstant());
        } catch (ParseException | JOSEException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
