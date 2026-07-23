package shm.commerce.warehouse.exception;

import java.util.UUID;

public class SpecifiedProductAlreadyInWarehouseException extends RuntimeException {

    public SpecifiedProductAlreadyInWarehouseException(UUID productId) {
        super("Product with id " + productId + " is already registered in warehouse.");
    }
}
