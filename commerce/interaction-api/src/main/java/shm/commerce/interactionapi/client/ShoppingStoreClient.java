package shm.commerce.interactionapi.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import shm.commerce.interactionapi.dto.PageProductDto;
import shm.commerce.interactionapi.dto.ProductCategory;
import shm.commerce.interactionapi.dto.ProductDto;
import shm.commerce.interactionapi.dto.SetProductQuantityStateRequest;

import java.util.List;
import java.util.UUID;

//@FeignClient(name = "shopping-store")
public interface ShoppingStoreClient {

    @GetMapping("/api/v1/shopping-store")
    PageProductDto getProducts(@RequestParam("category") ProductCategory category,
                               @RequestParam("page") Integer page,
                               @RequestParam("size") Integer size,
                               @RequestParam("sort") List<String> sort);

    @PutMapping("/api/v1/shopping-store")
    ProductDto createNewProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store")
    ProductDto updateProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    Boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    Boolean setProductQuantityState(
            @Valid @RequestBody SetProductQuantityStateRequest request);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto getProduct(@PathVariable("productId") UUID productId);
}
