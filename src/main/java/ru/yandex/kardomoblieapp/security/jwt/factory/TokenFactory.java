package ru.yandex.kardomoblieapp.security.jwt.factory;

public interface TokenFactory<I, O> {
    O getToken(I inputType);
}
