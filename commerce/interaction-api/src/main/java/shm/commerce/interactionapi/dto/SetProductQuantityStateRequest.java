package shm.commerce.interactionapi.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class SetProductQuantityStateRequest {
    @NotNull
    private UUID productId;

    @NotNull
    private QuantityState quantityState;

    public SetProductQuantityStateRequest() {
    }

    public SetProductQuantityStateRequest(UUID productId, QuantityState quantityState) {
        this.productId = productId;
        this.quantityState = quantityState;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public QuantityState getQuantityState() {
        return quantityState;
    }

    public void setQuantityState(QuantityState quantityState) {
        this.quantityState = quantityState;
    }
}
