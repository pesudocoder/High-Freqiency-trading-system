package com.hft.model;

public record Order(String id, Side side, double price, int quantity) {}
