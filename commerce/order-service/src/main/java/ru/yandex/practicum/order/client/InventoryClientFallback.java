package ru.yandex.practicum.order.client;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.order.dto.InventoryReleaseRequestDto;
import ru.yandex.practicum.order.dto.InventoryReleaseResponseDto;
import ru.yandex.practicum.order.dto.InventoryReserveRequestDto;
import ru.yandex.practicum.order.dto.InventoryReserveResponseDto;
import ru.yandex.practicum.order.exception.OrderConflictException;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.exception.ServiceDegradationException;

final class InventoryClientFallback implements InventoryClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryClientFallback.class);

    private final Throwable cause;

    InventoryClientFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public InventoryReserveResponseDto reserveStock(InventoryReserveRequestDto request) {
        Long productId = request.productId();
        Integer quantity = request.quantity();

        if (cause instanceof FeignException.BadRequest) {
            log.warn(
                    "Inventory service rejected reservation: productId={}, quantity={}",
                    productId,
                    quantity
            );
            throw new OrderProcessingException(String.format(
                    "Inventory rejected reservation for product %d",
                    productId
            ), cause);
        }

        if (cause instanceof FeignException.NotFound) {
            log.warn("Inventory not found during reservation: productId={}", productId);
            throw new OrderProcessingException(String.format(
                    "Inventory for product %d was not found",
                    productId
            ), cause);
        }

        if (cause instanceof FeignException.Conflict) {
            log.warn(
                    "Inventory reservation conflict: productId={}, quantity={}",
                    productId,
                    quantity
            );
            throw new OrderConflictException(String.format(
                    "Inventory reservation conflict for product %d",
                    productId
            ), cause);
        }

        log.error(
                "Inventory service reservation call failed: productId={}, quantity={}",
                productId,
                quantity,
                cause
        );
        throw new ServiceDegradationException("inventory-service", cause);
    }

    @Override
    public InventoryReleaseResponseDto releaseStock(InventoryReleaseRequestDto request) {
        Long productId = request.productId();
        Integer quantity = request.quantity();

        if (cause instanceof FeignException.BadRequest) {
            log.warn(
                    "Inventory service rejected release: productId={}, quantity={}",
                    productId,
                    quantity
            );
            throw new OrderProcessingException(String.format(
                    "Inventory rejected release for product %d",
                    productId
            ), cause);
        }

        if (cause instanceof FeignException.NotFound) {
            log.warn("Inventory not found during release: productId={}", productId);
            throw new OrderProcessingException(String.format(
                    "Inventory for product %d was not found",
                    productId
            ), cause);
        }

        if (cause instanceof FeignException.Conflict) {
            log.warn(
                    "Inventory release conflict: productId={}, quantity={}",
                    productId,
                    quantity
            );
            throw new OrderConflictException(String.format(
                    "Inventory release conflict for product %d",
                    productId
            ), cause);
        }

        log.error(
                "Inventory service release call failed: productId={}, quantity={}",
                productId,
                quantity,
                cause
        );
        throw new ServiceDegradationException("inventory-service", cause);
    }
}
