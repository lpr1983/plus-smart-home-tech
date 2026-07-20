package shm.commerce.interactionapi.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class NewProductInWarehouseRequest {
    @NotNull
    private UUID productId;

    private Boolean fragile;

    @Valid
    @NotNull
    private DimensionDto dimension;

    @NotNull
    @DecimalMin("1")
    private Double weight;

    public NewProductInWarehouseRequest() {
    }

    public NewProductInWarehouseRequest(UUID productId, Boolean fragile,
                                        DimensionDto dimension, Double weight) {
        this.productId = productId;
        this.fragile = fragile;
        this.dimension = dimension;
        this.weight = weight;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Boolean getFragile() {
        return fragile;
    }

    public void setFragile(Boolean fragile) {
        this.fragile = fragile;
    }

    public DimensionDto getDimension() {
        return dimension;
    }

    public void setDimension(DimensionDto dimension) {
        this.dimension = dimension;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
