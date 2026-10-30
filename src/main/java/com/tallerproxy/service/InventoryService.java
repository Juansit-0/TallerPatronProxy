package com.tallerproxy.service;

import com.tallerproxy.proxy.MicroserviceProxy;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryService implements MicroserviceProxy<Map<String, Object>> {

    private final Map<String, Integer> stock = new ConcurrentHashMap<>(
            Map.of("keyboard", 40, "mouse", 75, "monitor", 12, "headset", 28, "webcam", 9));

    @Override
    public Map<String, Object> execute(String operation, Object... params) {
        Map<String, Object> args = Params.of(params);
        Params.latency(15, 90);
        return switch (operation) {
            case "list" -> Map.of("items", new TreeMap<>(stock));
            case "check" -> {
                String sku = Params.text(args, "sku", "keyboard");
                yield Map.of("sku", sku, "available", find(sku));
            }
            case "restock" -> {
                String sku = Params.text(args, "sku", "keyboard");
                int quantity = Params.number(args, "quantity", 10);
                find(sku);
                yield Map.of("sku", sku, "available", stock.merge(sku, quantity, Integer::sum));
            }
            default -> throw new IllegalArgumentException("Unknown inventory operation: " + operation);
        };
    }

    private int find(String sku) {
        Integer available = stock.get(sku);
        if (available == null) {
            throw new NoSuchElementException("SKU not found: " + sku);
        }
        return available;
    }
}
