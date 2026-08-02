package ru.yandex.practicum.product.mapper;

import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.entity.Product;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static Product toEntity(CreateProductRequest request, Category category) {
        if (request == null) {
            throw new IllegalArgumentException("Create product request can't be null");
        }

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(category);
        product.setImageUrl(request.imageUrl());
        product.setActive(true);
        return product;
    }

    public static ProductDto toDto(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product can't be null");
        }

        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory() == null ? null : CategoryMapper.toDto(product.getCategory()),
                product.getImageUrl(),
                product.getActive()
        );
    }

    public static void updateEntity(Product product, UpdateProductRequest request, Category category) {
        if (product == null) {
            throw new IllegalArgumentException("Product can't be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("Update product request can't be null");
        }

        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.categoryId() != null) {
            product.setCategory(category);
        }
        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }
        if (request.active() != null) {
            product.setActive(request.active());
        }
    }
}
