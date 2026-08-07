package ru.yandex.practicum.order.client;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.order.dto.ProductDto;
import ru.yandex.practicum.order.exception.OrderProcessingException;
import ru.yandex.practicum.order.exception.ServiceDegradationException;

final class ProductClientFallback implements ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClientFallback.class);

    private final Throwable cause;

    ProductClientFallback(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public ProductDto getProductById(Long id) {
        if (cause instanceof FeignException.NotFound) {
            log.warn("Product service returned product not found: productId={}", id);
            throw new OrderProcessingException(String.format(
                    "Product with id %d was not found",
                    id
            ), cause);
        }

        log.error("Product service call failed: productId={}", id, cause);
        throw new ServiceDegradationException("product-service", cause);
    }
}
