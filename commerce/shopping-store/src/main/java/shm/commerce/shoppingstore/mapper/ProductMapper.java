package shm.commerce.shoppingstore.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import shm.commerce.interactionapi.dto.PageProductDto;
import shm.commerce.interactionapi.dto.PageableObject;
import shm.commerce.interactionapi.dto.ProductDto;
import shm.commerce.interactionapi.dto.SortObject;
import shm.commerce.shoppingstore.model.Product;

import java.util.List;

@Component
public class ProductMapper {

    public Product toNewEntity(ProductDto dto) {
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setQuantityState(toEntityQuantityState(dto.getQuantityState()));
        product.setProductState(toEntityProductState(dto.getProductState()));
        product.setPrice(dto.getPrice());

        if (dto.getImageSrc() != null) {
            product.setImageSrc(dto.getImageSrc());
        }
        if (dto.getProductCategory() != null) {
            product.setProductCategory(toEntityProductCategory(dto.getProductCategory()));
        }

        return product;
    }

    public void updateEntity(Product product, ProductDto dto) {
        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setQuantityState(toEntityQuantityState(dto.getQuantityState()));
        product.setProductState(toEntityProductState(dto.getProductState()));
        product.setPrice(dto.getPrice());

        if (dto.getImageSrc() != null) {
            product.setImageSrc(dto.getImageSrc());
        }
        if (dto.getProductCategory() != null) {
            product.setProductCategory(toEntityProductCategory(dto.getProductCategory()));
        }
    }

    public ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setProductId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setImageSrc(product.getImageSrc());
        dto.setQuantityState(toDtoQuantityState(product.getQuantityState()));
        dto.setProductState(toDtoProductState(product.getProductState()));
        dto.setProductCategory(toDtoProductCategory(product.getProductCategory()));
        dto.setPrice(product.getPrice());
        return dto;
    }

    public PageProductDto toPageDto(Page<Product> page) {
        List<SortObject> sort = toSortObjects(page.getSort());
        Pageable pageable = page.getPageable();

        PageableObject pageableObject = new PageableObject();
        pageableObject.setOffset(pageable.getOffset());
        pageableObject.setSort(toSortObjects(pageable.getSort()));
        pageableObject.setUnpaged(pageable.isUnpaged());
        pageableObject.setPaged(pageable.isPaged());
        pageableObject.setPageNumber(pageable.getPageNumber());
        pageableObject.setPageSize(pageable.getPageSize());

        PageProductDto dto = new PageProductDto();
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        dto.setFirst(page.isFirst());
        dto.setLast(page.isLast());
        dto.setSize(page.getSize());
        dto.setContent(page.getContent().stream().map(this::toDto).toList());
        dto.setNumber(page.getNumber());
        dto.setSort(sort);
        dto.setNumberOfElements(page.getNumberOfElements());
        dto.setPageable(pageableObject);
        dto.setEmpty(page.isEmpty());
        return dto;
    }

    public shm.commerce.shoppingstore.model.ProductCategory toEntityProductCategory(
            shm.commerce.interactionapi.dto.ProductCategory category) {
        return category == null
                ? null
                : shm.commerce.shoppingstore.model.ProductCategory.valueOf(category.name());
    }

    public shm.commerce.shoppingstore.model.QuantityState toEntityQuantityState(
            shm.commerce.interactionapi.dto.QuantityState state) {
        return shm.commerce.shoppingstore.model.QuantityState.valueOf(state.name());
    }

    private shm.commerce.shoppingstore.model.ProductState toEntityProductState(
            shm.commerce.interactionapi.dto.ProductState state) {
        return shm.commerce.shoppingstore.model.ProductState.valueOf(state.name());
    }

    private shm.commerce.interactionapi.dto.ProductCategory toDtoProductCategory(
            shm.commerce.shoppingstore.model.ProductCategory category) {
        return category == null
                ? null
                : shm.commerce.interactionapi.dto.ProductCategory.valueOf(category.name());
    }

    private shm.commerce.interactionapi.dto.QuantityState toDtoQuantityState(
            shm.commerce.shoppingstore.model.QuantityState state) {
        return shm.commerce.interactionapi.dto.QuantityState.valueOf(state.name());
    }

    private shm.commerce.interactionapi.dto.ProductState toDtoProductState(
            shm.commerce.shoppingstore.model.ProductState state) {
        return shm.commerce.interactionapi.dto.ProductState.valueOf(state.name());
    }

    private List<SortObject> toSortObjects(Sort sort) {
        return sort.stream()
                .map(order -> {
                    SortObject sortObject = new SortObject();
                    sortObject.setDirection(order.getDirection().name());
                    sortObject.setNullHandling(order.getNullHandling().name());
                    sortObject.setAscending(order.isAscending());
                    sortObject.setProperty(order.getProperty());
                    sortObject.setIgnoreCase(order.isIgnoreCase());
                    return sortObject;
                })
                .toList();
    }
}
