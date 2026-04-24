package com.hft.service;

import com.hft.dto.TradeEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TradeBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public TradeBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // @Async ensures this method runs in a background thread, preventing blocking the MatchingEngine
    @Async
    public void broadcastTrade(TradeEvent event) {
        // Send the complete trade event to all subscribers listening to /topic/trades
        messagingTemplate.convertAndSend("/topic/trades", event);
    }
}
