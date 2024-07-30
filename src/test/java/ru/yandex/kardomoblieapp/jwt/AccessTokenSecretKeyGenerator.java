package ru.yandex.kardomoblieapp.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.gen.OctetSequenceKeyGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AccessTokenSecretKeyGenerator {

    @Test
    @DisplayName("Генерация секретного ключа для access token")
    void generateAccessTokenSecretKey() throws JOSEException {
        OctetSequenceKey jwk = new OctetSequenceKeyGenerator(256)
                .generate();

        System.out.println(jwk);
    }
}
