package com.hft.dto;

import com.hft.model.Side;
import java.time.Instant;

public record TradeEvent(
    String initiatorId,
    String targetId,
    Side side,
    double price,
    int quantity,
    Instant timestamp
) {}
