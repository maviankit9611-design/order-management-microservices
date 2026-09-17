package com.learning.order_management.service;

import com.learning.order_management.dto.CreateOrderRequest;
import com.learning.order_management.dto.OrderItemRequest;
import com.learning.order_management.dto.OrderItemResponse;
import com.learning.order_management.dto.OrderResponse;
import com.learning.order_management.model.Order;
import com.learning.order_management.model.OrderItem;
import com.learning.order_management.model.OrderStatus;
import com.learning.order_management.repo.OrderRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepo orderRepository;

    public OrderService(OrderRepo orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        Order order = new Order();

        order.setUserId(request.getUserId());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = request.getOrderItemRequests()
                .stream()
                .map(itemRequest -> mapToOrderItem(itemRequest, order))
                .toList();

        order.setItems(orderItems);

        // Price calculation will be added after Product Service integration.
        order.setTotalAmount(BigDecimal.ZERO);

        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    private OrderItem mapToOrderItem(
            OrderItemRequest request,
            Order order) {

        OrderItem item = new OrderItem();

        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());

        // Product price will come from Product Service in Step 10D.
        item.setUnitPrice(BigDecimal.ZERO);
        item.setSubTotal(BigDecimal.ZERO);

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