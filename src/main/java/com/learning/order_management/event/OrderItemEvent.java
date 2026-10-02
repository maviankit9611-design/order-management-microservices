package com.learning.order_management.event;

import java.math.BigDecimal;

public record OrderItemEvent(Long productId,
                             Long quantity,
                             BigDecimal unitPrice,
                             BigDecimal subtotal) {
}
