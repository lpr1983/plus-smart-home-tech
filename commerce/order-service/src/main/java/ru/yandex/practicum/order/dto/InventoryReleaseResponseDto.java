package ru.yandex.practicum.order.dto;

public record InventoryReleaseResponseDto(
        boolean success,
        Integer availableQuantity,
        String message
) {
}
