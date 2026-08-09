package ru.yandex.practicum.order.dto;

public record InventoryReleaseRequestDto(
        Long productId,
        Integer quantity
) {
}
