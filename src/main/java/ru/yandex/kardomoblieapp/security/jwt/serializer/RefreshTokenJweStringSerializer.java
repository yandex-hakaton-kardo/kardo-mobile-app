package ru.yandex.kardomoblieapp.security.jwt.serializer;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.kardomoblieapp.security.jwt.model.Token;

import java.util.Date;

@RequiredArgsConstructor
@AllArgsConstructor
@Setter
@Slf4j
public class RefreshTokenJweStringSerializer implements TokenSerilazer<String> {

    private final JWEEncrypter jweEncrypter;

    private JWEAlgorithm jweAlgorithm = JWEAlgorithm.DIR;

    private EncryptionMethod encryptionMethod = EncryptionMethod.A128GCM;

    @Override
    public String convert(Token token) {
        var header = new JWEHeader.Builder(this.jweAlgorithm, this.encryptionMethod)
                .keyID(token.getId().toString())
                .build();
        var claimsSet = new JWTClaimsSet.Builder()
                .jwtID(token.getId().toString())
                .subject(token.getSubject())
                .issueTime(Date.from(token.getCreatedAt()))
                .expirationTime(Date.from(token.getExpiresAt()))
                .claim("authorities", token.getAuthorities())
                .build();

        var encryptedJWT = new EncryptedJWT(header, claimsSet);
        try {
            encryptedJWT.encrypt(this.jweEncrypter);
            return encryptedJWT.serialize();
        } catch (JOSEException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
