package shm.commerce.interactionapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AddProductToWarehouseRequest {
    private UUID productId;

    @NotNull
    @Min(1)
    private Long quantity;

    public AddProductToWarehouseRequest() {
    }

    public AddProductToWarehouseRequest(UUID productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
