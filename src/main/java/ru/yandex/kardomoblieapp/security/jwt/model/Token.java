package ru.yandex.kardomoblieapp.security.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public abstract class Token {

    private UUID id;

    private String subject;

    private List<String> authorities;

    private Instant createdAt;

    private Instant expiresAt;
}
