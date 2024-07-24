package ru.yandex.kardomoblieapp.shared.exception;

public class NotAuthorizedException extends RuntimeException {

    public NotAuthorizedException(String message) {
        super(message);
    }
}
