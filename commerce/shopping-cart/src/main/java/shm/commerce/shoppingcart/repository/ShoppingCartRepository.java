package shm.commerce.shoppingcart.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import shm.commerce.shoppingcart.model.ShoppingCart;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {

    @EntityGraph(attributePaths = "items")
    Optional<ShoppingCart> findByUsername(String username);
}
