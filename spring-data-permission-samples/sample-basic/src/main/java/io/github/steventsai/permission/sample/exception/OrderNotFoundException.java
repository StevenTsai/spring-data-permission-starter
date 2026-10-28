package io.github.steventsai.permission.sample.exception;

/**
 * Thrown when an order does not exist. The sample maps this to 404 without
 * echoing the requested ID.
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException() {
        super("Order not found");
    }
}
