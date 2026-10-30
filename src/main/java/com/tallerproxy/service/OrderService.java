package com.tallerproxy.service;

import com.tallerproxy.proxy.MicroserviceProxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderService implements MicroserviceProxy<Map<String, Object>> {

    private final Map<String, Map<String, Object>> orders = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(1000);

    @Override
    public Map<String, Object> execute(String operation, Object... params) {
        Map<String, Object> args = Params.of(params);
        Params.latency(30, 160);
        return switch (operation) {
            case "create" -> {
                String id = "ORD-" + sequence.incrementAndGet();
                Map<String, Object> order = new LinkedHashMap<>();
                order.put("orderId", id);
                order.put("product", Params.text(args, "product", "keyboard"));
                order.put("quantity", Params.number(args, "quantity", 1));
                order.put("state", "CREATED");
                orders.put(id, order);
                yield order;
            }
            case "list" -> Map.of("count", orders.size(), "orders", new ArrayList<>(orders.values()));
            case "cancel" -> {
                String id = Params.text(args, "orderId", "");
                Map<String, Object> order = orders.get(id);
                if (order == null) {
                    throw new NoSuchElementException("Order not found: " + id);
                }
                order.put("state", "CANCELLED");
                yield order;
            }
            default -> throw new IllegalArgumentException("Unknown orders operation: " + operation);
        };
    }

    public String randomOrderId() {
        return orders.keySet().stream().findAny().orElse("ORD-0000");
    }
}
