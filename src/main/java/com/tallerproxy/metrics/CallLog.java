package com.tallerproxy.metrics;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CallLog(
        String requestId,
        String serviceId,
        String operation,
        long durationMs,
        Status status,
        Instant timestamp,
        Map<String, Object> params,
        Object response,
        String error,
        List<String> stackTrace) {
}
