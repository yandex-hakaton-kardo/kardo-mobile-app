package ru.yandex.kardomoblieapp.security.jwt.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class RefreshToken extends Token {
    public RefreshToken(UUID id, String subject, List<String> authorities, Instant createdAt, Instant expiresAt) {
        super(id, subject, authorities, createdAt, expiresAt);
    }
}
