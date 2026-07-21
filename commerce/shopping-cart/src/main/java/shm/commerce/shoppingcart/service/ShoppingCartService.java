package shm.commerce.shoppingcart.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ShoppingCartService.class);

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
        String normalizedUsername = validateUsername(username);
        ShoppingCart shoppingCart = getOrCreateShoppingCart(normalizedUsername);

        log.debug("Shopping cart requested: shoppingCartId={}, username={}, itemCount={}",
                shoppingCart.getId(), normalizedUsername, shoppingCart.getItems().size());
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        String normalizedUsername = validateUsername(username);
        ShoppingCart shoppingCart = getOrCreateShoppingCart(normalizedUsername);
        ensureActive(shoppingCart);
        Map<UUID, Long> validatedProducts = validateProducts(products);

        ShoppingCartDto warehouseRequest = new ShoppingCartDto();
        warehouseRequest.setShoppingCartId(shoppingCart.getId());
        warehouseRequest.setProducts(validatedProducts);

        // Резервируем товары до изменения локальной корзины. Если склад отклонит
        // запрос, корзина в рамках текущей транзакции останется без изменений.
        log.debug("Requesting warehouse reservation: shoppingCartId={}, productCount={}",
                shoppingCart.getId(), validatedProducts.size());
        warehouseClient.checkProductQuantityEnoughForShoppingCart(warehouseRequest);

        // Объединяем запрошенное количество с уже существующими позициями корзины.
        // Новую сущность создаём только тогда, когда товара в корзине ещё нет.
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

        ShoppingCart savedCart = shoppingCartRepository.save(shoppingCart);
        log.info("Products added to shopping cart: shoppingCartId={}, username={}, productCount={}, itemCount={}",
                savedCart.getId(), normalizedUsername, validatedProducts.size(), savedCart.getItems().size());
        return shoppingCartMapper.toDto(savedCart);
    }

    @Transactional
    public void deactivateCurrentShoppingCart(String username) {
        String normalizedUsername = validateUsername(username);
        ShoppingCart shoppingCart = getOrCreateShoppingCart(normalizedUsername);
        shoppingCart.setActive(false);
        shoppingCartRepository.save(shoppingCart);

        log.info("Shopping cart deactivated: shoppingCartId={}, username={}",
                shoppingCart.getId(), normalizedUsername);
    }

    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        String normalizedUsername = validateUsername(username);
        ShoppingCart shoppingCart = getOrCreateShoppingCart(normalizedUsername);
        ensureActive(shoppingCart);
        List<UUID> validatedProductIds = validateProductIds(productIds);

        List<UUID> missingProductIds = new ArrayList<>();
        List<ShoppingCartItem> itemsToRemove = new ArrayList<>();

        // До изменения корзины собираем отсутствующие товары и позиции для удаления.
        // Поэтому при отсутствии хотя бы одного товара операция не выполняется частично.
        for (UUID productId : validatedProductIds) {
            ShoppingCartItem item = findItem(shoppingCart, productId);
            if (item == null) {
                missingProductIds.add(productId);
            } else {
                itemsToRemove.add(item);
            }
        }

        if (!missingProductIds.isEmpty()) {
            log.warn("Cannot remove products from shopping cart: shoppingCartId={}, missingProductIds={}",
                    shoppingCart.getId(), missingProductIds);
            throw new NoProductsInShoppingCartException(
                    "Products are not present in the shopping cart: " + missingProductIds
            );
        }

        shoppingCart.getItems().removeAll(itemsToRemove);
        ShoppingCart savedCart = shoppingCartRepository.save(shoppingCart);

        log.info("Products removed from shopping cart: shoppingCartId={}, removedCount={}, itemCount={}",
                savedCart.getId(), itemsToRemove.size(), savedCart.getItems().size());
        return shoppingCartMapper.toDto(savedCart);
    }

    @Transactional
    public ShoppingCartDto changeProductQuantity(String username,
                                                 ChangeProductQuantityRequest request) {
        String normalizedUsername = validateUsername(username);
        ShoppingCart shoppingCart = getOrCreateShoppingCart(normalizedUsername);
        ensureActive(shoppingCart);
        validateChangeQuantityRequest(request);

        ShoppingCartItem item = findItem(shoppingCart, request.getProductId());
        if (item == null) {
            throw new NoProductsInShoppingCartException(
                    "Product is not present in the shopping cart: " + request.getProductId()
            );
        }

        long previousQuantity = item.getQuantity();
        long quantityToReserve = request.getNewQuantity() - previousQuantity;

        // API склада умеет резервировать товары, но не содержит операции возврата.
        // Поэтому на склад отправляем только положительную разницу, а уменьшение
        // количества выполняем локально, без внешнего вызова.
        if (quantityToReserve > 0) {
            Map<UUID, Long> productsToReserve = new LinkedHashMap<>();
            productsToReserve.put(request.getProductId(), quantityToReserve);

            ShoppingCartDto warehouseRequest = new ShoppingCartDto();
            warehouseRequest.setShoppingCartId(shoppingCart.getId());
            warehouseRequest.setProducts(productsToReserve);

            log.debug("Requesting additional warehouse reservation: shoppingCartId={}, productId={}, quantity={}",
                    shoppingCart.getId(), request.getProductId(), quantityToReserve);
            warehouseClient.checkProductQuantityEnoughForShoppingCart(warehouseRequest);
        }

        item.setQuantity(request.getNewQuantity());
        ShoppingCart savedCart = shoppingCartRepository.save(shoppingCart);

        log.info("Shopping cart product quantity changed: shoppingCartId={}, productId={}, previous={}, current={}",
                savedCart.getId(), request.getProductId(), previousQuantity, request.getNewQuantity());
        return shoppingCartMapper.toDto(savedCart);
    }

    private ShoppingCart getOrCreateShoppingCart(String username) {
        return shoppingCartRepository.findByUsername(username)
                .orElseGet(() -> createShoppingCart(username));
    }

    private ShoppingCart createShoppingCart(String username) {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUsername(username);
        shoppingCart.setActive(true);
        ShoppingCart savedCart = shoppingCartRepository.save(shoppingCart);

        log.info("Shopping cart created: shoppingCartId={}, username={}", savedCart.getId(), username);
        return savedCart;
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

        // Проверяем весь запрос и создаём защитную копию коллекции до её передачи
        // клиенту склада или использования для изменения сохраняемых сущностей.
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
