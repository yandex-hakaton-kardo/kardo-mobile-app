package ru.yandex.kardomoblieapp.jwt;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.gen.OctetSequenceKeyGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RefreshTokenSecretKeyGenerator {

    @Test
    @DisplayName("Генерация секретного ключа для refresh token")
    void generateRefreshTokenSecretKey() throws JOSEException {
        OctetSequenceKey jwk = new OctetSequenceKeyGenerator(128)
                .algorithm(EncryptionMethod.A128GCM)
                .generate();

        System.out.println(jwk);
    }
}
