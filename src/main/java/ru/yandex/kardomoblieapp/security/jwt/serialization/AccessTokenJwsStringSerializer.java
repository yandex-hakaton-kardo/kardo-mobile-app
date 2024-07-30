package ru.yandex.kardomoblieapp.security.jwt.serialization;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.kardomoblieapp.security.jwt.model.Token;

import java.util.Date;

@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Slf4j
public class AccessTokenJwsStringSerializer implements TokenSerializer<String> {

    private final JWSSigner jwsSigner;

    private JWSAlgorithm jwsAlgorithm = JWSAlgorithm.HS256;

    @Override
    public String convert(Token token) {
        var header = new JWSHeader.Builder(this.jwsAlgorithm)
                .keyID(token.getId().toString())
                .build();
        var claimsSet = new JWTClaimsSet.Builder()
                .jwtID(token.getId().toString())
                .subject(token.getSubject())
                .issueTime(Date.from(token.getCreatedAt()))
                .expirationTime(Date.from(token.getExpiresAt()))
                .claim("authorities", token.getAuthorities())
                .build();

        var signedJWT = new SignedJWT(header, claimsSet);
        try {
            signedJWT.sign(this.jwsSigner);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return null;
    }
}
