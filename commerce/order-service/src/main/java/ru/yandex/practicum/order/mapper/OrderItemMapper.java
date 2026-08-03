package ru.yandex.practicum.order.mapper;

import ru.yandex.practicum.order.dto.OrderItemDto;
import ru.yandex.practicum.order.entity.OrderItem;

public final class OrderItemMapper {

    private OrderItemMapper() {
    }

    public static OrderItemDto toDto(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Order item can't be null");
        }

        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }
}
