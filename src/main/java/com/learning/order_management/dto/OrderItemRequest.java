package com.learning.order_management.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderItemRequest {

    @NotNull
    private Long productId;
    @Min(value = 1,message ="quantity should be greater than 1")
    private Integer quantity;

    public @NotNull Long getProductId() {
        return productId;
    }

    public void setProductId(@NotNull Long productId) {
        this.productId = productId;
    }

    public @Min(value = 1, message = "quantity should be greater than 1") Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(@Min(value = 1, message = "quantity should be greater than 1") Integer quantity) {
        this.quantity = quantity;
    }
}
