package com.learning.order_management.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateOrderRequest{
@NotNull
private Long userId;
@NotEmpty(message = "order must contain atleast one item")
private List<OrderItemRequest> items;

public @NotNull Long getUserId() {
    return userId;
}

public void setUserId(@NotNull Long userId) {
    this.userId = userId;
}

public @NotEmpty(message = "order must contain atleast one item") List<OrderItemRequest> getItems() {
    return items;
}

public void setItems(@NotEmpty(message = "order must contain atleast one item") List<OrderItemRequest> items) {
    this.items = items;
}

}