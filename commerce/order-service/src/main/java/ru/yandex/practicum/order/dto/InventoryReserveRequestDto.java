package ru.yandex.practicum.order.dto;

public record InventoryReserveRequestDto(
        Long productId,
        Integer quantity
) {
}
