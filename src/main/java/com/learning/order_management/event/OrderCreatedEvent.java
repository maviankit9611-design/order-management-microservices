package com.learning.order_management.event;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(Long orderId,
                                BigDecimal totalAmount,
                                com.learning.order_management.model.OrderStatus status,
                                LocalDateTime createdAt,
                                List<OrderItemEvent> items) {
}
