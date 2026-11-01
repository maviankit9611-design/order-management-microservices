package com.learning.order_management;

import com.learning.order_management.model.OrderEvent;
import com.learning.order_management.repo.OrderEventRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderManagementApplicationTests {

    @Autowired
    private OrderEventRepo orderEventRepo;

    @Test
    void pendingEventsCanBeFetchedUnderWriteLockInCreationOrder() {
        OrderEvent pendingEvent = new OrderEvent();
        pendingEvent.setEventId(UUID.randomUUID());
        pendingEvent.setAggregateType("OrderCreated");
        pendingEvent.setAggregateId("42");
        pendingEvent.setEventType("OrderCreated");
        pendingEvent.setPayload("{}");
        pendingEvent.setStatus("PENDING");
        pendingEvent.setCreatedAt(LocalDateTime.now());
        orderEventRepo.save(pendingEvent);

        assertThat(orderEventRepo.findByStatusForUpdateOrderByCreatedAtAsc("PENDING"))
                .hasSize(1)
                .first()
                .extracting(OrderEvent::getStatus)
                .isEqualTo("PENDING");
    }
}