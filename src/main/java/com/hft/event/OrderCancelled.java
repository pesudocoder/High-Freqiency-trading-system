package com.hft.event;

import java.time.Instant;

public record OrderCancelled(String orderId, Instant timestamp) implements DomainEvent {}
