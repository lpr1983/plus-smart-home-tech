package ru.yandex.practicum.order.exception;

public class OrderConflictException extends RuntimeException {

    public OrderConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
