package com.learning.order_management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OrderItemRequest {

    @NotNull ( message = "Product ID cannot be null")
    private Long productId;
    @NotNull ( message = "quantity cannot be null")
    @Positive (message ="quantity should be greater than 1")
    private Integer quantity;

    public @NotNull(message = "Product ID cannot be null") Long getProductId() {
        return productId;
    }

    public void setProductId(@NotNull(message = "Product ID cannot be null") Long productId) {
        this.productId = productId;
    }

    public @NotNull(message = "quantity cannot be null") @Positive(message = "quantity should be greater than 1") Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(@NotNull(message = "quantity cannot be null") @Positive(message = "quantity should be greater than 1") Integer quantity) {
        this.quantity = quantity;
    }
}
