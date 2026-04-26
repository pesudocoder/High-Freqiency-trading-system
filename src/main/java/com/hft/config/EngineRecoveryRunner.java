package com.hft.config;

import com.hft.engine.MatchingEngine;
import com.hft.event.DomainEvent;
import com.hft.store.JsonEventStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EngineRecoveryRunner implements ApplicationRunner {

    private final JsonEventStore eventStore;
    private final MatchingEngine matchingEngine;

    public EngineRecoveryRunner(JsonEventStore eventStore, MatchingEngine matchingEngine) {
        this.eventStore = eventStore;
        this.matchingEngine = matchingEngine;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("--- HFT ENGINE STARTUP: RECOVERING STATE FROM WAL ---");
        List<DomainEvent> events = eventStore.readAll();
        matchingEngine.replay(events);
    }
}
