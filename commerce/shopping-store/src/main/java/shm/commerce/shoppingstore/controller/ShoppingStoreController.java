package shm.commerce.shoppingstore.controller;

import org.springframework.web.bind.annotation.RestController;
import shm.commerce.interactionapi.client.ShoppingStoreClient;
import shm.commerce.interactionapi.dto.PageProductDto;
import shm.commerce.interactionapi.dto.ProductCategory;
import shm.commerce.interactionapi.dto.ProductDto;
import shm.commerce.interactionapi.dto.SetProductQuantityStateRequest;
import shm.commerce.shoppingstore.service.ShoppingStoreService;

import java.util.List;
import java.util.UUID;

@RestController
public class ShoppingStoreController implements ShoppingStoreClient {

    private final ShoppingStoreService shoppingStoreService;

    public ShoppingStoreController(ShoppingStoreService shoppingStoreService) {
        this.shoppingStoreService = shoppingStoreService;
    }

    @Override
    public PageProductDto getProducts(ProductCategory category,
                                      Integer page,
                                      Integer size,
                                      List<String> sort) {
        return shoppingStoreService.getProducts(category, page, size, sort);
    }

    @Override
    public ProductDto createNewProduct(ProductDto productDto) {
        return shoppingStoreService.createNewProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        return shoppingStoreService.updateProduct(productDto);
    }

    @Override
    public Boolean removeProductFromStore(UUID productId) {
        return shoppingStoreService.removeProductFromStore(productId);
    }

    @Override
    public Boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        return shoppingStoreService.setProductQuantityState(request);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return shoppingStoreService.getProduct(productId);
    }
}
