package com.hft.controller;

import com.hft.dto.OrderBookResponse;
import com.hft.dto.OrderRequest;
import com.hft.model.Order;
import com.hft.service.OrderBookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderBookService orderBookService;

    public OrderController(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(@Valid @RequestBody OrderRequest request) {
        // Map DTO to Model
        Order newOrder = new Order(request.id(), request.side(), request.price(), request.quantity());
        
        // Pass to highly synchronous service logic
        orderBookService.processOrder(newOrder);

        return ResponseEntity.ok("Order " + request.id() + " processed successfully.");
    }

    @GetMapping("/book")
    public ResponseEntity<OrderBookResponse> getBook() {
        return ResponseEntity.ok(orderBookService.getOrderBook());
    }
}
