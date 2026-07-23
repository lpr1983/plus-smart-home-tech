package shm.commerce.warehouse.exception;

import java.util.UUID;

public class NoSpecifiedProductInWarehouseException extends RuntimeException {

    public NoSpecifiedProductInWarehouseException(UUID productId) {
        super("Product with id " + productId + " is not registered in warehouse.");
    }
}
