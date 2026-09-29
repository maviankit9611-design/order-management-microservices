package com.learning.order_management.service;

import com.learning.order_management.client.ProductClient;
import com.learning.order_management.dto.*;
import com.learning.order_management.exception.InsufficientStockException;
import com.learning.order_management.exception.ProductNotFoundException;
import com.learning.order_management.model.Order;
import com.learning.order_management.model.OrderItem;
import com.learning.order_management.model.OrderStatus;
import com.learning.order_management.repo.OrderRepo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepo orderRepository;
    private final ProductClient productClient;

    public OrderService(
            OrderRepo orderRepository,
            ProductClient productClient) {

        this.orderRepository = orderRepository;
        this.productClient = productClient;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        Order order = new Order();

        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        List<OrderItem> orderItems = request.getItems()
                .stream()
                .map(itemRequest -> mapToOrderItem(itemRequest, order))
                .toList();
        for (OrderItem orderItem : orderItems) {
            productClient.reduceStock(orderItem.getProductId(),orderItem.getQuantity());
        }

        order.setItems(orderItems);
        BigDecimal totalAmount = orderItems.stream()
                .map(x->x.getSubTotal())
                .reduce(BigDecimal.ZERO, (c,e)->c.add(e));

        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

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
}