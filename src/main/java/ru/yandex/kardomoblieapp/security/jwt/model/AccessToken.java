package ru.yandex.kardomoblieapp.security.jwt.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AccessToken extends Token {
    public AccessToken(UUID id, String subject, List<String> authorities, Instant createdAt, Instant expiresAt) {
        super(id, subject, authorities, createdAt, expiresAt);
    }
}
