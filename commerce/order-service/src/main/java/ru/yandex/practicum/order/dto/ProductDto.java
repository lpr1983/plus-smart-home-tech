package ru.yandex.practicum.order.dto;

import java.math.BigDecimal;

public record ProductDto(

        Long id,

        String name,

        BigDecimal price,

        String description,

        Boolean active
) {
}
