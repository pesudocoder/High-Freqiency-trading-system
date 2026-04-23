package com.hft.service;

import com.hft.dto.OrderBookResponse;
import com.hft.engine.MatchingEngine;
import com.hft.model.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderBookService {
    // We instantiate exactly ONE thread-safe Matching Engine here
    private final MatchingEngine engine = new MatchingEngine();

    public void processOrder(Order order) {
        engine.processOrder(order);
    }

    public OrderBookResponse getOrderBook() {
        return new OrderBookResponse(
            engine.getBids(),
            engine.getAsks()
        );
    }
}
