package shm.commerce.shoppingstore.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String userMessage;

    public ProductNotFoundException(UUID productId) {
        super(productId == null
                ? "Product identifier is not specified"
                : "Product with id " + productId + " was not found");
        this.httpStatus = HttpStatus.NOT_FOUND;
        this.userMessage = "Товар не найден";
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
