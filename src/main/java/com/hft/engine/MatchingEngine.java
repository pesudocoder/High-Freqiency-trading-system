package com.hft.engine;

import com.hft.model.Order;
import com.hft.model.Side;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.time.Instant;

import com.hft.dto.TradeEvent;
import com.hft.service.TradeBroadcaster;
import com.hft.store.JsonEventStore;
import com.hft.event.DomainEvent;
import com.hft.event.OrderAccepted;
import com.hft.event.OrderMatched;
import org.springframework.stereotype.Component;

@Component
public class MatchingEngine {
    private final PriorityQueue<Order> bids;
    private final PriorityQueue<Order> asks;

    private final TradeBroadcaster broadcaster;
    private final JsonEventStore eventStore;

    public MatchingEngine(TradeBroadcaster broadcaster, JsonEventStore eventStore) {
        bids = new PriorityQueue<>(Comparator.comparingDouble(Order::price).reversed());
        asks = new PriorityQueue<>(Comparator.comparingDouble(Order::price));
        this.broadcaster = broadcaster;
        this.eventStore = eventStore;
    }

    public synchronized void processOrder(Order newOrder) {
        System.out.println("-> Incoming Command: " + newOrder.side() + " " + newOrder.quantity() + " @ Rs " + newOrder.price() + " (ID: " + newOrder.id() + ")");

        // 1. Generate Acceptance Event & Persist
        OrderAccepted accepted = new OrderAccepted(newOrder, Instant.now());
        eventStore.append(accepted);
        apply(accepted); // Mutates State

        int remainingQuantity = newOrder.quantity();

        // 2. Evaluate Matches based on current state
        if (newOrder.side() == Side.BUY) {
            while (remainingQuantity > 0 && !asks.isEmpty()) {
                Order bestAsk = asks.peek();
                if (newOrder.price() >= bestAsk.price()) {
                    int matchQty = Math.min(remainingQuantity, bestAsk.quantity());
                    
                    OrderMatched matched = new OrderMatched(newOrder.id(), bestAsk.id(), Side.BUY, bestAsk.price(), matchQty, Instant.now());
                    
                    // Persist Event
                    eventStore.append(matched);
                    
                    // Apply to State
                    apply(matched);
                    
                    // Broadcast only AFTER state and storage are confirmed safe
                    broadcaster.broadcastTrade(new TradeEvent(
                            matched.initiatorId(), matched.targetId(), matched.side(), matched.price(), matched.quantity(), matched.timestamp()
                    ));
                    
                    System.out.println("   [MATCH EVENT] Buy " + newOrder.id() + " matched with Sell " + bestAsk.id() + " for " + matchQty + " @ Rs " + bestAsk.price());
                    remainingQuantity -= matchQty;
                } else {
                    break;
                }
            }
        } else {
            while (remainingQuantity > 0 && !bids.isEmpty()) {
                // To avoid matching with itself since `apply(accepted)` put it in bids
                // Wait, if it's a SELL, it's matching against BIDS. The new order is in ASKS.
                // Oh wait, `apply(accepted)` put it in ASKS. So `bids` does not contain it. Safe!
                Order bestBid = bids.peek();
                if (newOrder.price() <= bestBid.price()) {
                    int matchQty = Math.min(remainingQuantity, bestBid.quantity());
                    
                    OrderMatched matched = new OrderMatched(newOrder.id(), bestBid.id(), Side.SELL, bestBid.price(), matchQty, Instant.now());
                    
                    eventStore.append(matched);
                    apply(matched);
                    
                    broadcaster.broadcastTrade(new TradeEvent(
                            matched.initiatorId(), matched.targetId(), matched.side(), matched.price(), matched.quantity(), matched.timestamp()
                    ));
                    
                    System.out.println("   [MATCH EVENT] Sell " + newOrder.id() + " matched with Buy " + bestBid.id() + " for " + matchQty + " @ Rs " + bestBid.price());
                    remainingQuantity -= matchQty;
                } else {
                    break;
                }
            }
        }
    }

    // --- PROJECTOR STATE MUTATION ---
    public void apply(DomainEvent event) {
        if (event instanceof OrderAccepted accepted) {
            if (accepted.order().side() == Side.BUY) {
                bids.add(accepted.order());
            } else {
                asks.add(accepted.order());
            }
        } 
        else if (event instanceof OrderMatched matched) {
            reduceOrderQuantity(bids, matched.initiatorId(), matched.quantity());
            reduceOrderQuantity(asks, matched.initiatorId(), matched.quantity());
            reduceOrderQuantity(bids, matched.targetId(), matched.quantity());
            reduceOrderQuantity(asks, matched.targetId(), matched.quantity());
        }
    }

    private void reduceOrderQuantity(PriorityQueue<Order> queue, String orderId, int matchedQty) {
        Order target = null;
        for (Order o : queue) {
            if (o.id().equals(orderId)) {
                target = o;
                break;
            }
        }
        if (target != null) {
            queue.remove(target);
            int newQty = target.quantity() - matchedQty;
            if (newQty > 0) {
                queue.add(new Order(target.id(), target.side(), target.price(), newQty));
            }
        }
    }

    public synchronized void replay(List<DomainEvent> events) {
        System.out.println("-> Starting Event Store Replay. " + events.size() + " events found.");
        bids.clear();
        asks.clear();
        for (DomainEvent event : events) {
            apply(event);
        }
        System.out.println("-> Replay Complete. Order Book Restored.");
    }

    public synchronized List<Order> getBids() {
        List<Order> sortedBids = new ArrayList<>();
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
