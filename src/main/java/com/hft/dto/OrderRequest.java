package com.hft.dto;

import com.hft.model.Side;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
    @NotBlank(message = "Order ID cannot be blank")
    String id,
    
    @NotNull(message = "Side (BUY or SELL) is required")
    Side side,
    
    @DecimalMin(value = "0.01", message = "Price must be strictly positive")
    double price,
    
    @Min(value = 1, message = "Quantity must be at least 1")
    int quantity
) {}
