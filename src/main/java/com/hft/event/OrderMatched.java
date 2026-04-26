package com.hft.event;

import com.hft.model.Side;
import java.time.Instant;

public record OrderMatched(
    String initiatorId,
    String targetId,
    Side side,
    double price,
    int quantity,
    Instant timestamp
) implements DomainEvent {}
