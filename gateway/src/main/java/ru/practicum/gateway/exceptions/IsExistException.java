package ru.practicum.gateway.exceptions;

public class IsExistException extends RuntimeException {
    public IsExistException(String message) {
        super(message);
    }
}
