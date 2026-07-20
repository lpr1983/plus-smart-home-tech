package shm.commerce.interactionapi.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class ChangeProductQuantityRequest {
    @NotNull
    private UUID productId;

    @NotNull
    private Long newQuantity;

    public ChangeProductQuantityRequest() {
    }

    public ChangeProductQuantityRequest(UUID productId, Long newQuantity) {
        this.productId = productId;
        this.newQuantity = newQuantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Long getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Long newQuantity) {
        this.newQuantity = newQuantity;
    }
}
