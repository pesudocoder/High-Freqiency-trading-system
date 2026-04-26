package com.hft.event;

import com.hft.model.Order;
import java.time.Instant;

public record OrderAccepted(Order order, Instant timestamp) implements DomainEvent {}
