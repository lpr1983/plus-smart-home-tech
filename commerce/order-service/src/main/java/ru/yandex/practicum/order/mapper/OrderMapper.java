package ru.yandex.practicum.order.mapper;

import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.entity.Order;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static OrderDto toDto(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order can't be null");
        }

        return OrderDto.builder()
                .id(order.getId())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .status(order.getStatus().name())
                .totalPrice(order.getTotalPrice())
                .statusDetails(order.getStatusDetails())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(OrderItemMapper::toDto)
                        .toList())
                .build();
    }
}
