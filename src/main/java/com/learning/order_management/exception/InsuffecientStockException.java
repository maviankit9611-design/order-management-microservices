package com.learning.order_management.exception;

public class InsuffecientStockException extends RuntimeException{
    public InsuffecientStockException(String message) {
        super(message);
    }
}
