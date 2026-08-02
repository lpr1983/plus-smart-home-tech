package ru.yandex.practicum.product.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.product.dto.CreateProductRequest;
import ru.yandex.practicum.product.dto.ProductDto;
import ru.yandex.practicum.product.dto.UpdateProductRequest;
import ru.yandex.practicum.product.entity.Category;
import ru.yandex.practicum.product.entity.Product;
import ru.yandex.practicum.product.exception.NotFoundException;
import ru.yandex.practicum.product.mapper.ProductMapper;
import ru.yandex.practicum.product.repository.ProductRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        log.info("Creating product: name={}, categoryId={}", request.name(), request.categoryId());

        Category category = resolveCategory(request.categoryId());
        Product product = ProductMapper.toEntity(request, category);
        Product savedProduct = productRepository.save(product);

        log.info("Product created: id={}, name={}", savedProduct.getId(), savedProduct.getName());
        return ProductMapper.toDto(savedProduct);
    }

    public List<ProductDto> getAllProducts() {
        log.debug("Getting all active products");

        List<ProductDto> products = mapToDto(productRepository.findAllByActiveTrue());
        log.debug("Active products retrieved: count={}", products.size());
        return products;
    }

    public ProductDto getProductById(Long id) {
        log.debug("Getting product: id={}", id);
        return ProductMapper.toDto(findProductById(id));
    }

    @Transactional
    public ProductDto updateProduct(Long id, UpdateProductRequest request) {
        log.info("Updating product: id={}", id);

        Product product = findProductById(id);
        Category category = resolveCategory(request.categoryId());
        ProductMapper.updateEntity(product, request, category);
        Product savedProduct = productRepository.save(product);

        log.info("Product updated: id={}", savedProduct.getId());
        return ProductMapper.toDto(savedProduct);
    }

    public List<ProductDto> searchProducts(String query) {
        log.debug("Searching active products: query={}", query);

        List<ProductDto> products = mapToDto(
                productRepository.findByNameContainingIgnoreCaseAndActiveTrue(query)
        );
        log.debug("Product search completed: query={}, count={}", query, products.size());
        return products;
    }

    public List<ProductDto> getProductsByCategory(Long categoryId) {
        log.debug("Getting active products by category: categoryId={}", categoryId);

        categoryService.getCategoryEntityById(categoryId);
        List<ProductDto> products = mapToDto(
                productRepository.findByCategoryIdAndActiveTrue(categoryId)
        );
        log.debug("Products by category retrieved: categoryId={}, count={}", categoryId, products.size());
        return products;
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found: id={}", id);
                    return new NotFoundException(String.format("Product with id %d was not found", id));
                });
    }

    private Category resolveCategory(Long categoryId) {
        return categoryId == null ? null : categoryService.getCategoryEntityById(categoryId);
    }

    private static List<ProductDto> mapToDto(List<Product> products) {
        return products.stream()
                .map(ProductMapper::toDto)
                .toList();
    }
}
