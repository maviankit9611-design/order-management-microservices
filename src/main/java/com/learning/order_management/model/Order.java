package com.learning.order_management.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotBlank(message = "userId cannot be blank")
    private Long userId;
    @NotBlank(message = "status cannot be empty")
    private OrderStatus status;
    @PositiveOrZero(message = "total_amount cannot be -ve or zero")
    private BigDecimal totalAmount;
    private Date createdDate;
    private Date updatedDate;
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> orderItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "userId cannot be blank") Long getUserId() {
        return userId;
    }

    public void setUserId(@NotBlank(message = "userId cannot be blank") Long userId) {
        this.userId = userId;
    }

    public @NotBlank(message = "status cannot be empty") OrderStatus getStatus() {
        return status;
    }

    public void setStatus(@NotBlank(message = "status cannot be empty") OrderStatus status) {
        this.status = status;
    }

    public @PositiveOrZero(message = "total_amount cannot be -ve or zero") BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(@PositiveOrZero(message = "total_amount cannot be -ve or zero") BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    public void addItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }
}
