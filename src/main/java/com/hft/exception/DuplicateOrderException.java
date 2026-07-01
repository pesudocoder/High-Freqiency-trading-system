package com.hft.exception;

public class DuplicateOrderException extends RuntimeException {
    public DuplicateOrderException(String orderId) {
        super("Order ID '" + orderId + "' already exists in the order book");
    }
}
