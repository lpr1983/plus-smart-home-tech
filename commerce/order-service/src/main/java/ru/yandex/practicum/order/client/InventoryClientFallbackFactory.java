package ru.yandex.practicum.order.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.dto.InventoryReleaseRequestDto;
import ru.yandex.practicum.order.dto.InventoryReleaseResponseDto;
import ru.yandex.practicum.order.dto.InventoryReserveRequestDto;
import ru.yandex.practicum.order.dto.InventoryReserveResponseDto;
import ru.yandex.practicum.order.exception.OrderConflictException;
import ru.yandex.practicum.order.exception.OrderProcessingException;
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
            throw translateFailure(request.productId(), "reservation");
        }

        @Override
        public InventoryReleaseResponseDto releaseStock(InventoryReleaseRequestDto request) {
            throw translateFailure(request.productId(), "release");
        }

        private RuntimeException translateFailure(Long productId, String operation) {
            if (cause instanceof FeignException.BadRequest) {
                return new OrderProcessingException(String.format(
                        "Inventory rejected %s for product %d",
                        operation,
                        productId
                ), cause);
            }

            if (cause instanceof FeignException.NotFound) {
                return new OrderProcessingException(String.format(
                        "Inventory for product %d was not found",
                        productId
                ), cause);
            }

            if (cause instanceof FeignException.Conflict) {
                return new OrderConflictException(String.format(
                        "Inventory %s conflict for product %d",
                        operation,
                        productId
                ), cause);
            }

            return new ServiceDegradationException(SERVICE_NAME, cause);
        }
    }
}
