package shm.commerce.shoppingstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shm.commerce.shoppingstore.model.Product;
import shm.commerce.shoppingstore.model.ProductCategory;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findAllByProductCategory(ProductCategory productCategory,
                                           Pageable pageable);
}
