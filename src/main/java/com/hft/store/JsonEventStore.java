package com.hft.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hft.event.DomainEvent;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class JsonEventStore {

    private static final String LOG_FILE = "events.log";
    private final ObjectMapper objectMapper;
    private final BufferedWriter writer;

    public JsonEventStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        try {
            Path path = Paths.get(LOG_FILE);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            // Open string writer in append mode
            this.writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Event Store file", e);
        }
    }

    // synchronized to ensure thread-safe sequential writes
    public synchronized void append(DomainEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            writer.write(json);
            writer.newLine();
            writer.flush(); // Crucial: ensure it's written disk
        } catch (IOException e) {
            throw new RuntimeException("Failed to append event to store", e);
        }
    }

    public List<DomainEvent> readAll() {
        Path path = Paths.get(LOG_FILE);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        
        List<DomainEvent> events = new ArrayList<>();
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                try {
                    DomainEvent event = objectMapper.readValue(line, DomainEvent.class);
                    events.add(event);
                } catch (JsonProcessingException e) {
                    System.err.println("Skipping corrupted event line: " + line);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Event Store", e);
        }
        return events;
    }

    @PreDestroy
    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
