package ru.yandex.kardomoblieapp.security.jwt.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Токены доступа")
public record TokensResponse(@Schema(description = "Access токен") String accessToken,
                             @Schema(description = "Дата окончания действия access токена") String accessTokenExpiry,
                             @Schema(description = "Refresh токен") String refreshToken,
                             @Schema(description = "Дата окончания действия refresh токена") String refreshTokenExpiry) {
}
