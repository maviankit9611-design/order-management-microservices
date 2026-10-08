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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

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
    public OrderResponse createOrder(CreateOrderRequest request) {
        //create order
        Order order = new Order();
        //Adding order details
        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        //Add order items
        List<OrderItem> orderItems = request.getItems()
                .stream()
                .map(itemRequest -> mapToOrderItem(itemRequest, order))
                .toList();
        for (OrderItem orderItem : orderItems) {
            productClient.reduceStock(orderItem.getProductId(),orderItem.getQuantity());
        }
        order.setItems(orderItems);
        //Calculate Toatl Amount
        BigDecimal totalAmount = orderItems.stream()
                .map(x->x.getSubTotal())
                .reduce(BigDecimal.ZERO, (c,e)->c.add(e));

        order.setTotalAmount(totalAmount);
        //Save order
        Order savedOrder = orderRepository.save(order);
        //Create order event
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setAggregateType("OrderCreated");
        orderEvent.setCreatedAt(LocalDateTime.now());
        orderEvent.setAggregateId(savedOrder.getId().toString());
        String payload;

        try {
            payload = objectMapper.writeValueAsString(
                    createOrderCreatedEvent(savedOrder)
            );
            orderEvent.setPayload(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize OrderCreatedEvent", e);
        }
        orderEvent.setEventType("OrderEvent");
        orderEvent.setStatus("PENDING");
        //Save order Event
        orderEventRepo.save(orderEvent);

        return mapToResponse(savedOrder);
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