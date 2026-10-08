package com.learning.order_management.event;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
                                UUID EventId,
                                Long orderId,
                                BigDecimal totalAmount,
                                com.learning.order_management.model.OrderStatus status,
                                LocalDateTime createdAt,
                                List<OrderItemEvent> items) {
}
