package shm.commerce.shoppingcart.exception;

public class NotAuthorizedUserException extends RuntimeException {

    public NotAuthorizedUserException() {
        super("Username must not be blank.");
    }
}
