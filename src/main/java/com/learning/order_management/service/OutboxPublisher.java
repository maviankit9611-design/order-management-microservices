package com.learning.order_management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.order_management.event.OrderCreatedEvent;
import com.learning.order_management.kafka.OrderEventProducer;
import com.learning.order_management.model.OrderEvent;
import com.learning.order_management.repo.OrderEventRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisher {

    private final OrderEventRepo orderEventRepo;
    private final OrderEventProducer orderEventProducer;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OrderEventRepo orderEventRepo,
            OrderEventProducer orderEventProducer,
            ObjectMapper objectMapper) {

        this.orderEventRepo = orderEventRepo;
        this.orderEventProducer = orderEventProducer;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OrderEvent> events =
                orderEventRepo.findByStatusOrderByCreatedAtAsc("PENDING");

        for (OrderEvent event : events) {

            try {

                OrderCreatedEvent orderCreatedEvent =
                        objectMapper.readValue(
                                event.getPayload(),
                                OrderCreatedEvent.class
                        );

                orderEventProducer.publishOrderCreated(orderCreatedEvent);

                event.setStatus("PUBLISHED");

                orderEventRepo.save(event);

            } catch (Exception e) {

                System.err.println(
                        "Failed to publish outbox event ID: "
                                + event.getOrderId()
                                + ", Error: "
                                + e.getMessage()
                );
            }
        }
    }
}