package shm.commerce.shoppingcart.controller;

import org.springframework.web.bind.annotation.RestController;
import shm.commerce.interactionapi.client.ShoppingCartClient;
import shm.commerce.interactionapi.dto.ChangeProductQuantityRequest;
import shm.commerce.interactionapi.dto.ShoppingCartDto;
import shm.commerce.shoppingcart.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class ShoppingCartController implements ShoppingCartClient {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        return shoppingCartService.getShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        return shoppingCartService.addProductToShoppingCart(username, products);
    }

    @Override
    public void deactivateCurrentShoppingCart(String username) {
        shoppingCartService.deactivateCurrentShoppingCart(username);
    }

    @Override
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        return shoppingCartService.removeFromShoppingCart(username, productIds);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(String username,
                                                 ChangeProductQuantityRequest request) {
        return shoppingCartService.changeProductQuantity(username, request);
    }
}
