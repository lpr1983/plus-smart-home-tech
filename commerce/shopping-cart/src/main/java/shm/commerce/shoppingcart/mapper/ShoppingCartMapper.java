package shm.commerce.shoppingcart.mapper;

import org.springframework.stereotype.Component;
import shm.commerce.interactionapi.dto.ShoppingCartDto;
import shm.commerce.shoppingcart.model.ShoppingCart;
import shm.commerce.shoppingcart.model.ShoppingCartItem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ShoppingCartMapper {

    public ShoppingCartDto toDto(ShoppingCart shoppingCart) {
        Map<UUID, Long> products = new LinkedHashMap<>();
        for (ShoppingCartItem item : shoppingCart.getItems()) {
            products.put(item.getProductId(), item.getQuantity());
        }

        ShoppingCartDto dto = new ShoppingCartDto();
        dto.setShoppingCartId(shoppingCart.getId());
        dto.setProducts(products);
        return dto;
    }
}
