package com.learning.order_management.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.order_management.client.ProductClient;
import com.learning.order_management.dto.*;
import com.learning.order_management.event.OrderCreatedEvent;
import com.learning.order_management.event.OrderItemEvent;
import com.learning.order_management.exception.InsufficientStockException;
import com.learning.order_management.exception.ProductNotFoundException;
import com.learning.order_management.kafka.OrderEventProducer;
import com.learning.order_management.model.Order;
import com.learning.order_management.model.OrderEvent;
import com.learning.order_management.model.OrderItem;
import com.learning.order_management.model.OrderStatus;
import com.learning.order_management.repo.OrderEventRepo;
import com.learning.order_management.repo.OrderItemRepo;
import com.learning.order_management.repo.OrderRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepo orderRepository;
    private final ProductClient productClient;
    private final OrderEventRepo orderEventRepo;
    private final OrderEventProducer orderEventProducer;
    private final ObjectMapper objectMapper;

    public OrderService(
            OrderRepo orderRepository,
            ProductClient productClient,
            OrderEventRepo orderEventRepo,
            OrderEventProducer orderEventProducer,
            ObjectMapper objectMapper) {

        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.orderEventRepo = orderEventRepo;
        this.orderEventProducer = orderEventProducer;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request,Long userId) {
        //create order
        Order order = new Order();
        //Adding order details
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        //Add order items
        List<OrderItem> orderItems = request.getItems()
                .stream()
                .map(itemRequest -> mapToOrderItem(itemRequest, order))
                .toList();
        List<OrderItem> reducedItems = new ArrayList<>();
        registerStockCompensation(reducedItems);
        try {
            for (OrderItem orderItem : orderItems) {
                productClient.reduceStock(orderItem.getProductId(), orderItem.getQuantity());
                reducedItems.add(orderItem);
            }
            order.setItems(orderItems);
            BigDecimal totalAmount = orderItems.stream()
                    .map(OrderItem::getSubTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            order.setTotalAmount(totalAmount);
            Order savedOrder = orderRepository.save(order);
            OrderCreatedEvent eventPayload = createOrderCreatedEvent(savedOrder);
            OrderEvent orderEvent = new OrderEvent();
            orderEvent.setEventId(eventPayload.eventId());
            orderEvent.setAggregateType("OrderCreated");
            orderEvent.setCreatedAt(LocalDateTime.now());
            orderEvent.setAggregateId(savedOrder.getId().toString());
            try {
                orderEvent.setPayload(objectMapper.writeValueAsString(eventPayload));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize OrderCreatedEvent", e);
            }
            orderEvent.setEventType("OrderCreated");
            orderEvent.setStatus("PENDING");
            orderEventRepo.save(orderEvent);
            return mapToResponse(savedOrder);
        } catch (RuntimeException failure) {
            if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                restoreStock(reducedItems);
            }
            throw failure;
        }
    }

    private void registerStockCompensation(List<OrderItem> reducedItems) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    restoreStock(reducedItems);
                }
            }
        });
    }

    private void restoreStock(List<OrderItem> reducedItems) {
        for (int index = reducedItems.size() - 1; index >= 0; index--) {
            OrderItem item = reducedItems.get(index);
            try {
                productClient.restoreStock(item.getProductId(), item.getQuantity());
            } catch (RuntimeException compensationFailure) {
                log.error("Failed to restore stock for product {} after order rollback", item.getProductId(), compensationFailure);
            }
        }
    }

    private OrderItem mapToOrderItem(
            OrderItemRequest request,
            Order order) {
        ProductResponse product;
        try{
            product =
                    productClient.getProductById(request.getProductId());

        }catch (Exception e){
            throw new ProductNotFoundException(e.getMessage());
        }
        if(product == null){
            throw new ProductNotFoundException("Product not found");
        }
        Integer quantity = request.getQuantity();
        int totalstock = product.getProductQuantity().intValue();

        if( quantity > totalstock){
            throw new InsufficientStockException("insufficient stock");
        }

            BigDecimal unitPrice = product.getProductPrice();
        BigDecimal totalPrice =
                unitPrice.multiply(
                        BigDecimal.valueOf(request.getQuantity())
                );

        OrderItem item = new OrderItem();

        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(unitPrice);
        item.setSubTotal(totalPrice);
        item.setOrder(order);

        return item;
    }

    private OrderResponse mapToResponse(Order order) {

        OrderResponse response = new OrderResponse();

        response.setOrderId(order.getId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::mapToItemResponse)
                .toList();

        response.setItems(items);

        return response;
    }

    private OrderItemResponse mapToItemResponse(OrderItem item) {

        OrderItemResponse response = new OrderItemResponse();

        response.setProductId(item.getProductId());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnitPrice());
        response.setTotalPrice(item.getSubTotal());

        return response;
    }
    private OrderCreatedEvent createOrderCreatedEvent(Order order) {


        List<OrderItemEvent> items =
                order.getItems()
                        .stream()
                        .map(item ->
                                new OrderItemEvent(
                                        item.getProductId(),
                                        item.getQuantity(),
                                        item.getUnitPrice(),
                                        item.getSubTotal()
                                )
                        )
                        .toList();

        return new OrderCreatedEvent(
                UUID.randomUUID(),
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                items
        );
    }
}