package ru.yandex.kardomoblieapp.security.jwt.serializer;

import ru.yandex.kardomoblieapp.security.jwt.model.Token;

public interface TokenSerilazer<T> {
    T convert(Token token);
}
