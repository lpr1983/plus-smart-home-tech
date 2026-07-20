package shm.commerce.interactionapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class DimensionDto {
    @NotNull
    @DecimalMin("1")
    private Double width;

    @NotNull
    @DecimalMin("1")
    private Double height;

    @NotNull
    @DecimalMin("1")
    private Double depth;

    public DimensionDto() {
    }

    public DimensionDto(Double width, Double height, Double depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getDepth() {
        return depth;
    }

    public void setDepth(Double depth) {
        this.depth = depth;
    }
}
