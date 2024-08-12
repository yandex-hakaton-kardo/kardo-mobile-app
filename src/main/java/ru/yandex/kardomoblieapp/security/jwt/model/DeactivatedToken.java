package ru.yandex.kardomoblieapp.security.jwt.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deactivated_tokens")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DeactivatedToken {

    @Id
    private UUID id;

    @Column(name = "keep_until")
    private Instant keepUntil;
}
