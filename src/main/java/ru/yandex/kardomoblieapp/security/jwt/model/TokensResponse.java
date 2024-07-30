package ru.yandex.kardomoblieapp.security.jwt.model;

public record TokensResponse(String accessToken, String accessTokenExpiry,
                             String refreshToken, String refreshTokenExpiry) {
}
