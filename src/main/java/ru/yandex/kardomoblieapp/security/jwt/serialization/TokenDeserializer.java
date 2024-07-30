package ru.yandex.kardomoblieapp.security.jwt.serialization;

import ru.yandex.kardomoblieapp.security.jwt.model.Token;

public interface TokenDeserializer<T> {
    Token convert(T object);
}
