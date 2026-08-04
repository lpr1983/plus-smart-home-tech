package ru.yandex.practicum.order.exception;

public class InactiveProductException extends RuntimeException {

    public InactiveProductException(String message) {
        super(message);
    }
}
