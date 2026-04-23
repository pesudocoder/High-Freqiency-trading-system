package com.hft.dto;

import com.hft.model.Order;
import java.util.List;

public record OrderBookResponse(
    List<Order> bids,
    List<Order> asks
) {}
