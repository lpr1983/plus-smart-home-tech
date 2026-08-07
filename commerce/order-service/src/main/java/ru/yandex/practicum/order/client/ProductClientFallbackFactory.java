package ru.yandex.practicum.order.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.order.dto.ProductDto;
import ru.yandex.practicum.order.exception.ServiceDegradationException;

@Component
public class ProductClientFallbackFactory implements FallbackFactory<ProductClient> {

    private static final String SERVICE_NAME = "product-service";

    @Override
    public ProductClient create(Throwable cause) {
        return new ProductClientFallback(cause);
    }

    private static class ProductClientFallback implements ProductClient {

        private final Throwable cause;

        private ProductClientFallback(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public ProductDto getProductById(Long id) {
            throw new ServiceDegradationException(SERVICE_NAME, cause);
        }
    }
}
