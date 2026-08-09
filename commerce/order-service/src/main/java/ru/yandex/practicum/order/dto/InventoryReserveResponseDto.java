package ru.yandex.practicum.order.dto;

public record InventoryReserveResponseDto(
        boolean success,
        Integer availableQuantity,
        String message
) {
}
