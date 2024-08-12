package ru.yandex.kardomoblieapp.security.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.kardomoblieapp.security.jwt.model.DeactivatedToken;

import java.util.UUID;

public interface DeactivatedTokenRepository extends JpaRepository<DeactivatedToken, UUID> {
}
