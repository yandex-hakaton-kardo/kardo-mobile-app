package ru.yandex.kardomoblieapp.security.jwt.serialization;

import ru.yandex.kardomoblieapp.security.jwt.model.Token;

public interface TokenSerializer<T> {
    T convert(Token token);
}
