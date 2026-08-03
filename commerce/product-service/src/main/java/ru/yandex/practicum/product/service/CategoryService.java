package ru.yandex.practicum.product.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CategoryDto;
import ru.yandex.practicum.product.dto.CreateCategoryRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.mapper.CategoryMapper;
import ru.yandex.practicum.product.repository.CategoryRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest request) {
        log.info("Creating category: name={}", request.name());

        Category category = CategoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created: id={}, name={}", savedCategory.getId(), savedCategory.getName());

        return CategoryMapper.toDto(savedCategory);
    }

    public List<CategoryDto> getAllCategories() {
        log.debug("Getting all categories");

        List<CategoryDto> categories = categoryRepository.findAll().stream()
                .map(CategoryMapper::toDto)
                .toList();

        log.debug("Categories retrieved: count={}", categories.size());
        return categories;
    }

    public CategoryDto getCategoryById(Long id) {
        return CategoryMapper.toDto(getCategoryEntityById(id));
    }

    public Category getCategoryEntityById(Long id) {
        log.debug("Getting category: id={}", id);

        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found: id={}", id);
                    return new NotFoundException(String.format("Category with id %d was not found", id));
                });
    }
}
