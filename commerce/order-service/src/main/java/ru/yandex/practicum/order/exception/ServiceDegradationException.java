package ru.yandex.practicum.order.exception;

public class ServiceDegradationException extends RuntimeException {

    private final String serviceName;

    public ServiceDegradationException(String serviceName, Throwable cause) {
        super(String.format("%s is degraded", serviceName), cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
