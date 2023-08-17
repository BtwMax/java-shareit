package ru.practicum.gateway.exceptions;

public class ServerErrorException extends RuntimeException {

    public ServerErrorException(String message) {
        super(message);
    }
}
