package ru.yandex.practicum.order.dto;

public record InventoryReserveResponseDto(
        Long productId,
        Integer reservedQuantity,
        Integer availableQuantity
) {
}
