package shm.commerce.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shm.commerce.warehouse.model.WarehouseProduct;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, UUID> {

    Optional<WarehouseProduct> findByProductId(UUID productId);

    boolean existsByProductId(UUID productId);
}
