package ru.yandex.kardomoblieapp.shared.exception;

public class InvalidDateOfBirthException extends RuntimeException {
    public InvalidDateOfBirthException(String message) {
        super(message);
    }
}
