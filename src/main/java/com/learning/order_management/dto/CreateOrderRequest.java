package com.learning.order_management.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateOrderRequest{

@NotEmpty(message = "order must contain atleast one item")
@Valid
private List<OrderItemRequest> items;

public @NotEmpty(message = "order must contain atleast one item") List<OrderItemRequest> getItems() {
    return items;
}

public void setItems(@NotEmpty(message = "order must contain atleast one item") List<OrderItemRequest> items) {
    this.items = items;
}

}