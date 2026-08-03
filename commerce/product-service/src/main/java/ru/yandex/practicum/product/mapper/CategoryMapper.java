package ru.yandex.practicum.product.mapper;

import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateCategoryRequest;
import ru.yandex.practicum.product.entity.Category;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static Category toEntity(CreateCategoryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create category request can't be null");
        }

        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        return category;
    }

    public static CategoryDto toDto(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category can't be null");
        }

        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
