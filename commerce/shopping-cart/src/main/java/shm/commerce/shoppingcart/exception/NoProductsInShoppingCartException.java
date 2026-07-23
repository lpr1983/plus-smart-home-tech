package shm.commerce.shoppingcart.exception;

public class NoProductsInShoppingCartException extends RuntimeException {

    public NoProductsInShoppingCartException(String message) {
        super(message);
    }
}
