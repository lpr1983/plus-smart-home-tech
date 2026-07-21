package shm.commerce.shoppingstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shm.commerce.interactionapi.dto.PageProductDto;
import shm.commerce.interactionapi.dto.ProductDto;
import shm.commerce.interactionapi.dto.SetProductQuantityStateRequest;
import shm.commerce.shoppingstore.exception.ProductNotFoundException;
import shm.commerce.shoppingstore.mapper.ProductMapper;
import shm.commerce.shoppingstore.model.Product;
import shm.commerce.shoppingstore.model.ProductState;
import shm.commerce.shoppingstore.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ShoppingStoreService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ShoppingStoreService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public PageProductDto getProducts(shm.commerce.interactionapi.dto.ProductCategory category,
                                      Integer page,
                                      Integer size,
                                      List<String> sortParameters) {
        PageRequest pageRequest = PageRequest.of(page, size, buildSort(sortParameters));
        Page<Product> products = productRepository.findAllByProductCategory(
                productMapper.toEntityProductCategory(category),
                pageRequest
        );
        return productMapper.toPageDto(products);
    }

    @Transactional
    public ProductDto createNewProduct(ProductDto productDto) {
        Product product = productMapper.toNewEntity(productDto);
        return productMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        Product product = findProduct(productDto.getProductId());
        productMapper.updateEntity(product, productDto);
        return productMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public Boolean removeProductFromStore(UUID productId) {
        Product product = findProduct(productId);
        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
        return true;
    }

    @Transactional
    public Boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        Product product = findProduct(request.getProductId());
        product.setQuantityState(productMapper.toEntityQuantityState(request.getQuantityState()));
        productRepository.save(product);
        return true;
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID productId) {
        return productMapper.toDto(findProduct(productId));
    }

    private Product findProduct(UUID productId) {
        if (productId == null) {
            throw new ProductNotFoundException(null);
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Sort buildSort(List<String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Sort.unsorted();
        }

        List<Sort.Order> orders = new ArrayList<>();
        int index = 0;
        while (index < parameters.size()) {
            String current = parameters.get(index);
            if (current == null || current.isBlank()) {
                index++;
                continue;
            }

            String[] parts = current.split(",", 2);
            String property = parts[0].trim();
            Sort.Direction direction = Sort.Direction.ASC;

            if (parts.length == 2 && isDirection(parts[1])) {
                direction = Sort.Direction.fromString(parts[1].trim());
            } else if (index + 1 < parameters.size() && isDirection(parameters.get(index + 1))) {
                direction = Sort.Direction.fromString(parameters.get(index + 1).trim());
                index++;
            }

            if (!property.isBlank()) {
                orders.add(new Sort.Order(direction, property));
            }
            index++;
        }

        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    private boolean isDirection(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("asc") || normalized.equals("desc");
    }
}
