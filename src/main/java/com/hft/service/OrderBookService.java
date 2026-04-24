package com.hft.service;

import com.hft.dto.OrderBookResponse;
import com.hft.engine.MatchingEngine;
import com.hft.model.Order;
import org.springframework.stereotype.Service;

@Service
public class OrderBookService {

    private final MatchingEngine engine;

    public OrderBookService(MatchingEngine engine) {
        this.engine = engine;
    }

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
