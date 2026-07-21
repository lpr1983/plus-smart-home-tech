package shm.commerce.shoppingcart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shm.commerce.interactionapi.client.WarehouseClient;
import shm.commerce.interactionapi.dto.ChangeProductQuantityRequest;
import shm.commerce.interactionapi.dto.ShoppingCartDto;
import shm.commerce.shoppingcart.exception.NoProductsInShoppingCartException;
import shm.commerce.shoppingcart.exception.NotAuthorizedUserException;
import shm.commerce.shoppingcart.exception.ShoppingCartDeactivatedException;
import shm.commerce.shoppingcart.exception.ShoppingCartValidationException;
import shm.commerce.shoppingcart.mapper.ShoppingCartMapper;
import shm.commerce.shoppingcart.model.ShoppingCart;
import shm.commerce.shoppingcart.model.ShoppingCartItem;
import shm.commerce.shoppingcart.repository.ShoppingCartRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final WarehouseClient warehouseClient;

    public ShoppingCartService(ShoppingCartRepository shoppingCartRepository,
                               ShoppingCartMapper shoppingCartMapper,
                               WarehouseClient warehouseClient) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.shoppingCartMapper = shoppingCartMapper;
        this.warehouseClient = warehouseClient;
    }

    @Transactional
    public ShoppingCartDto getShoppingCart(String username) {
        ShoppingCart shoppingCart = getOrCreateShoppingCart(validateUsername(username));
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        ShoppingCart shoppingCart = getOrCreateShoppingCart(validateUsername(username));
        ensureActive(shoppingCart);
        Map<UUID, Long> validatedProducts = validateProducts(products);

        ShoppingCartDto warehouseRequest = new ShoppingCartDto();
        warehouseRequest.setShoppingCartId(shoppingCart.getId());
        warehouseRequest.setProducts(validatedProducts);
        warehouseClient.checkProductQuantityEnoughForShoppingCart(warehouseRequest);

        for (Map.Entry<UUID, Long> entry : validatedProducts.entrySet()) {
            ShoppingCartItem item = findItem(shoppingCart, entry.getKey());
            if (item == null) {
                item = new ShoppingCartItem();
                item.setShoppingCart(shoppingCart);
                item.setProductId(entry.getKey());
                item.setQuantity(entry.getValue());
                shoppingCart.getItems().add(item);
            } else {
                item.setQuantity(item.getQuantity() + entry.getValue());
            }
        }

        return shoppingCartMapper.toDto(shoppingCartRepository.save(shoppingCart));
    }

    @Transactional
    public void deactivateCurrentShoppingCart(String username) {
        ShoppingCart shoppingCart = getOrCreateShoppingCart(validateUsername(username));
        shoppingCart.setActive(false);
        shoppingCartRepository.save(shoppingCart);
    }

    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        ShoppingCart shoppingCart = getOrCreateShoppingCart(validateUsername(username));
        ensureActive(shoppingCart);
        List<UUID> validatedProductIds = validateProductIds(productIds);

        List<UUID> missingProductIds = new ArrayList<>();
        List<ShoppingCartItem> itemsToRemove = new ArrayList<>();
        for (UUID productId : validatedProductIds) {
            ShoppingCartItem item = findItem(shoppingCart, productId);
            if (item == null) {
                missingProductIds.add(productId);
            } else {
                itemsToRemove.add(item);
            }
        }

        if (!missingProductIds.isEmpty()) {
            throw new NoProductsInShoppingCartException(
                    "Products are not present in the shopping cart: " + missingProductIds
            );
        }

        shoppingCart.getItems().removeAll(itemsToRemove);
        return shoppingCartMapper.toDto(shoppingCartRepository.save(shoppingCart));
    }

    @Transactional
    public ShoppingCartDto changeProductQuantity(String username,
                                                 ChangeProductQuantityRequest request) {
        ShoppingCart shoppingCart = getOrCreateShoppingCart(validateUsername(username));
        ensureActive(shoppingCart);
        validateChangeQuantityRequest(request);

        ShoppingCartItem item = findItem(shoppingCart, request.getProductId());
        if (item == null) {
            throw new NoProductsInShoppingCartException(
                    "Product is not present in the shopping cart: " + request.getProductId()
            );
        }

        long quantityToReserve = request.getNewQuantity() - item.getQuantity();
        if (quantityToReserve > 0) {
            Map<UUID, Long> productsToReserve = new LinkedHashMap<>();
            productsToReserve.put(request.getProductId(), quantityToReserve);

            ShoppingCartDto warehouseRequest = new ShoppingCartDto();
            warehouseRequest.setShoppingCartId(shoppingCart.getId());
            warehouseRequest.setProducts(productsToReserve);
            warehouseClient.checkProductQuantityEnoughForShoppingCart(warehouseRequest);
        }

        item.setQuantity(request.getNewQuantity());
        return shoppingCartMapper.toDto(shoppingCartRepository.save(shoppingCart));
    }

    private ShoppingCart getOrCreateShoppingCart(String username) {
        return shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createShoppingCart(username));
    }

    private ShoppingCart createShoppingCart(String username) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUsername(username);
        shoppingCart.setActive(true);
        return shoppingCartRepository.save(shoppingCart);
    }

    private String validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException();
        }
        return username.trim();
    }

    private Map<UUID, Long> validateProducts(Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            throw new ShoppingCartValidationException("Products must not be empty.");
        }

        Map<UUID, Long> validatedProducts = new LinkedHashMap<>();
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            UUID productId = entry.getKey();
            Long quantity = entry.getValue();
            if (productId == null) {
                throw new ShoppingCartValidationException("Product id must not be null.");
            }
            if (quantity == null || quantity < 1) {
                throw new ShoppingCartValidationException(
                        "Product quantity must be greater than zero for product " + productId + "."
                );
            }
            validatedProducts.put(productId, quantity);
        }
        return validatedProducts;
    }

    private List<UUID> validateProductIds(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new ShoppingCartValidationException("Product ids must not be empty.");
        }
        for (UUID productId : productIds) {
            if (productId == null) {
                throw new ShoppingCartValidationException("Product id must not be null.");
            }
        }
        return productIds;
    }

    private void validateChangeQuantityRequest(ChangeProductQuantityRequest request) {
        if (request == null) {
            throw new ShoppingCartValidationException("Change quantity request must not be null.");
        }
        if (request.getProductId() == null) {
            throw new ShoppingCartValidationException("Product id must not be null.");
        }
        if (request.getNewQuantity() == null || request.getNewQuantity() < 1) {
            throw new ShoppingCartValidationException("New quantity must be greater than zero.");
        }
    }

    private void ensureActive(ShoppingCart shoppingCart) {
        if (!shoppingCart.isActive()) {
            throw new ShoppingCartDeactivatedException();
        }
    }

    private ShoppingCartItem findItem(ShoppingCart shoppingCart, UUID productId) {
        for (ShoppingCartItem item : shoppingCart.getItems()) {
            if (productId.equals(item.getProductId())) {
                return item;
            }
        }
        return null;
    }
}
