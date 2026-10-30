package com.tallerproxy.service;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class Params {

    private Params() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> of(Object... params) {
        if (params.length > 0 && params[0] instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    public static String text(Map<String, Object> args, String key, String fallback) {
        Object value = args.get(key);
        return value == null ? fallback : value.toString();
    }

    public static int number(Map<String, Object> args, String key, int fallback) {
        Object value = args.get(key);
        return value == null ? fallback : (int) Double.parseDouble(value.toString());
    }

    public static void latency(int minMs, int maxMs) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(minMs, maxMs));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
