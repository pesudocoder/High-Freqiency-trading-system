package com.hft.service;

import com.hft.dto.MarketDepthSnapshotDTO;
import com.hft.engine.MatchingEngine;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DepthBroadcaster {

    private final MatchingEngine matchingEngine;
    private final SimpMessagingTemplate messagingTemplate;

    public DepthBroadcaster(MatchingEngine matchingEngine, SimpMessagingTemplate messagingTemplate) {
        this.matchingEngine = matchingEngine;
        this.messagingTemplate = messagingTemplate;
    }

    // Runs every 500ms on a background thread
    @Scheduled(fixedRate = 500)
    public void broadcastMarketDepth() {
        // Poll the engine for an aggregated Top-10 snapshot
        MarketDepthSnapshotDTO snapshot = matchingEngine.generateDepthSnapshot();
        
        // Broadcast to clients listening to the new depth topic
        messagingTemplate.convertAndSend("/topic/depth", snapshot);
    }
}
