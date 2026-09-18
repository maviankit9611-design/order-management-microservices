package com.learning.order_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class ProductResponse {
    @NotBlank(message ="Product name cannot be blank")
    private String productName;
    @NotBlank (message ="Description cannot be blank")
    private String productDescription;
    @Positive(message="Price cannot be zero or negative")
    private BigDecimal productPrice;
    @PositiveOrZero( message ="Quantity cannot be zero")
    private BigDecimal productQuantity;

    public @NotBlank(message = "Product name cannot be blank") String getProductName() {
        return productName;
    }

    public void setProductName(@NotBlank(message = "Product name cannot be blank") String productName) {
        this.productName = productName;
    }

    public @NotBlank(message = "Description cannot be blank") String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(@NotBlank(message = "Description cannot be blank") String productDescription) {
        this.productDescription = productDescription;
    }

    public @Positive(message = "Price cannot be zero or negative") BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(@Positive(message = "Price cannot be zero or negative") BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public @PositiveOrZero(message = "Quantity cannot be zero") BigDecimal getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(@PositiveOrZero(message = "Quantity cannot be zero") BigDecimal productQuantity) {
        this.productQuantity = productQuantity;
    }

    @Override
    public String toString() {
        return "ProductResponse{" +
                "productName='" + productName + '\'' +
                ", productDescription='" + productDescription + '\'' +
                ", productPrice=" + productPrice +
                ", productQuantity=" + productQuantity +
                '}';
    }
}
