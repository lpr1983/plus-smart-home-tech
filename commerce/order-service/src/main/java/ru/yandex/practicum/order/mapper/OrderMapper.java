package ru.yandex.practicum.order.mapper;

import ru.yandex.practicum.order.dto.CreateOrderRequest;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.entity.Order;

import java.math.BigDecimal;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static Order toEntity(CreateOrderRequest request, BigDecimal totalPrice) {
        if (request == null) {
            throw new IllegalArgumentException("Create order request can't be null");
        }
        if (totalPrice == null) {
            throw new IllegalArgumentException("Total price can't be null");
        }

        Order order = new Order();
        order.setCustomerName(request.customerName());
        order.setCustomerEmail(request.customerEmail());
        order.setTotalPrice(totalPrice);

        request.items().stream()
                .map(OrderItemMapper::toEntity)
                .forEach(order::addItem);

        return order;
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
