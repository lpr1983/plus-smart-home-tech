package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.dto.InventoryReleaseRequestDto;
import ru.yandex.practicum.order.dto.InventoryReleaseResponseDto;
import ru.yandex.practicum.order.dto.InventoryReserveRequestDto;
import ru.yandex.practicum.order.dto.InventoryReserveResponseDto;
import ru.yandex.practicum.order.exception.ServiceDegradationException;

@Component
public class InventoryClientFallbackFactory implements FallbackFactory<InventoryClient> {

    private static final String SERVICE_NAME = "inventory-service";

    @Override
    public InventoryClient create(Throwable cause) {
        return new InventoryClientFallback(cause);
    }

    private static class InventoryClientFallback implements InventoryClient {

        private final Throwable cause;

        private InventoryClientFallback(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public InventoryReserveResponseDto reserveStock(InventoryReserveRequestDto request) {
            throw new ServiceDegradationException(SERVICE_NAME, cause);
        }

        @Override
        public InventoryReleaseResponseDto releaseStock(InventoryReleaseRequestDto request) {
            throw new ServiceDegradationException(SERVICE_NAME, cause);
        }
    }
}
