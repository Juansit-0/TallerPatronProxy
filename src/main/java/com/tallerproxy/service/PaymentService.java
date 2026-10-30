package com.tallerproxy.service;

import com.tallerproxy.proxy.MicroserviceProxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PaymentService implements MicroserviceProxy<Map<String, Object>> {

    private static final double FAILURE_RATE = 0.10;

    @Override
    public Map<String, Object> execute(String operation, Object... params) {
        Map<String, Object> args = Params.of(params);
        Params.latency(60, 260);
        if (ThreadLocalRandom.current().nextDouble() < FAILURE_RATE) {
            throw new IllegalStateException("Payment gateway timeout");
        }
        return switch (operation) {
            case "charge" -> receipt("CHARGED", args);
            case "refund" -> receipt("REFUNDED", args);
            case "status" -> Map.of("gateway", "ONLINE", "orderId", Params.text(args, "orderId", "ORD-0000"));
            default -> throw new IllegalArgumentException("Unknown payments operation: " + operation);
        };
    }

    private Map<String, Object> receipt(String state, Map<String, Object> args) {
        Map<String, Object> receipt = new LinkedHashMap<>();
        receipt.put("transactionId", "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        receipt.put("orderId", Params.text(args, "orderId", "ORD-0000"));
        receipt.put("amount", Params.number(args, "amount", 100));
        receipt.put("state", state);
        return receipt;
    }
}
