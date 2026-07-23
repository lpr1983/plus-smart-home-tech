package shm.commerce.shoppingcart.exception;

public class ShoppingCartDeactivatedException extends RuntimeException {

    public ShoppingCartDeactivatedException() {
        super("The shopping cart is deactivated and cannot be changed.");
    }
}
