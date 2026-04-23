package com.hft.engine;

import com.hft.model.Order;
import com.hft.model.Side;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class MatchingEngine {
    // Max-Heap for Bids (Buys): Highest price first
    private final PriorityQueue<Order> bids;
    // Min-Heap for Asks (Sells): Lowest price first
    private final PriorityQueue<Order> asks;

    public MatchingEngine() {
        bids = new PriorityQueue<>(Comparator.comparingDouble(Order::price).reversed());
        asks = new PriorityQueue<>(Comparator.comparingDouble(Order::price));
    }

    // Thread Safety: synchronized ensures only one HTTP request modifies the book at a time
    public synchronized void processOrder(Order newOrder) {
        System.out.println("-> Incoming via API: " + newOrder.side() + " " + newOrder.quantity() + " @ Rs " + newOrder.price() + " (ID: " + newOrder.id() + ")");
        int remainingQuantity = newOrder.quantity();

        if (newOrder.side() == Side.BUY) {
            while (remainingQuantity > 0 && !asks.isEmpty()) {
                Order bestAsk = asks.peek();
                if (newOrder.price() >= bestAsk.price()) {
                    int matchQty = Math.min(remainingQuantity, bestAsk.quantity());
                    System.out.println("   [MATCH] Buy " + newOrder.id() + " matched with Sell " + bestAsk.id() + " for " + matchQty + " @ Rs " + bestAsk.price());
                    remainingQuantity -= matchQty;

                    if (matchQty == bestAsk.quantity()) {
                        asks.poll(); 
                    } else {
                        asks.poll();
                        asks.add(new Order(bestAsk.id(), bestAsk.side(), bestAsk.price(), bestAsk.quantity() - matchQty));
                    }
                } else {
                    break;
                }
            }
            if (remainingQuantity > 0) {
                bids.add(new Order(newOrder.id(), newOrder.side(), newOrder.price(), remainingQuantity));
            }
        } else {
            while (remainingQuantity > 0 && !bids.isEmpty()) {
                Order bestBid = bids.peek();
                if (newOrder.price() <= bestBid.price()) {
                    int matchQty = Math.min(remainingQuantity, bestBid.quantity());
                    System.out.println("   [MATCH] Sell " + newOrder.id() + " matched with Buy " + bestBid.id() + " for " + matchQty + " @ Rs " + bestBid.price());
                    remainingQuantity -= matchQty;

                    if (matchQty == bestBid.quantity()) {
                        bids.poll();
                    } else {
                        bids.poll();
                        bids.add(new Order(bestBid.id(), bestBid.side(), bestBid.price(), bestBid.quantity() - matchQty));
                    }
                } else {
                    break;
                }
            }
            if (remainingQuantity > 0) {
                asks.add(new Order(newOrder.id(), newOrder.side(), newOrder.price(), remainingQuantity));
            }
        }
    }

    // Return safely copied lists to prevent ConcurrentModificationException when serializing to JSON
    public synchronized List<Order> getBids() {
        List<Order> sortedBids = new ArrayList<>();
        // Note: iterators of PriorityQueue do not guarantee total order. 
        // For strict sorted JSON view, we could drain and refill, but for simplicity we list them raw 
        // or sort the copy before returning.
        PriorityQueue<Order> copy = new PriorityQueue<>(bids);
        while(!copy.isEmpty()){
            sortedBids.add(copy.poll());
        }
        return sortedBids;
    }

    public synchronized List<Order> getAsks() {
        List<Order> sortedAsks = new ArrayList<>();
        PriorityQueue<Order> copy = new PriorityQueue<>(asks);
        while(!copy.isEmpty()){
            sortedAsks.add(copy.poll());
        }
        return sortedAsks;
    }
}
