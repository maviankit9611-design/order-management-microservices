package com.learning.order_management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.order_management.event.OrderCreatedEvent;
import com.learning.order_management.kafka.OrderEventProducer;
import com.learning.order_management.model.OrderEvent;
import com.learning.order_management.repo.OrderEventRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

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

    @Scheduled(fixedDelay = 50000)
    @Transactional
    public void publishPendingEvents() {

        List<OrderEvent> events =
                orderEventRepo.findByStatusForUpdateOrderByCreatedAtAsc("PENDING");

        for (OrderEvent event : events) {

            try {

                OrderCreatedEvent orderCreatedEvent =
                        objectMapper.readValue(
                                event.getPayload(),
                                OrderCreatedEvent.class
                        );

                // Do not mark the outbox row published until Kafka has acknowledged it.
                orderEventProducer.publishOrderCreated(orderCreatedEvent).get();

                event.setStatus("PUBLISHED");

                orderEventRepo.save(event);

            } catch (Exception e) {

                log.error("Failed to publish outbox event ID {}", event.getOrderId(), e);
            }
        }
    }
}
