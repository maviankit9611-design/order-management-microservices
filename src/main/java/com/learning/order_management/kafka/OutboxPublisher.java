package com.learning.order_management.kafka;

import com.learning.order_management.model.OrderEvent;
import com.learning.order_management.repo.OrderEventRepo;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxPublisher {

    private final OrderEventRepo outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(
            OrderEventRepo  outboxRepository,
            KafkaTemplate<String, String> kafkaTemplate) {

        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        List<OrderEvent> events =
                outboxRepository
                        .findByStatusOrderByCreatedAtAsc("PENDING");

        for (OrderEvent event : events) {

            try {

                kafkaTemplate.send(
                        "order-created",
                        event.getAggregateId(),
                        event.getPayload()
                );

                event.setStatus("PUBLISHED");

                outboxRepository.save(event);

            } catch (Exception ex) {

                event.setStatus("FAILED");

                outboxRepository.save(event);
            }
        }
    }
}
