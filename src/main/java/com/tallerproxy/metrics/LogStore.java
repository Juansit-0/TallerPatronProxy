package com.tallerproxy.metrics;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LogStore {

    public static final List<String> SERVICES = List.of("inventory", "orders", "payments");

    private final List<CallLog> logs = new ArrayList<>();

    public synchronized void add(CallLog entry) {
        logs.add(entry);
    }

    public synchronized List<Map<String, Object>> summary() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String service : SERVICES) {
            List<CallLog> calls = logs.stream().filter(l -> l.serviceId().equals(service)).toList();
            long errors = calls.stream().filter(l -> l.status() == Status.ERROR).count();
            double errorRate = calls.isEmpty() ? 0 : errors * 100.0 / calls.size();
            double avg = calls.stream().mapToLong(CallLog::durationMs).average().orElse(0);
            List<Map<String, Object>> recent = calls.subList(Math.max(0, calls.size() - 20), calls.size()).stream()
                    .map(l -> Map.<String, Object>of("requestId", l.requestId(), "durationMs", l.durationMs(),
                            "status", l.status(), "timestamp", l.timestamp()))
                    .toList();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("serviceId", service);
            item.put("totalCalls", calls.size());
            item.put("errorCount", errors);
            item.put("errorRate", round(errorRate));
            item.put("successRate", calls.isEmpty() ? 0 : round(100 - errorRate));
            item.put("avgDurationMs", round(avg));
            item.put("recent", recent);
            result.add(item);
        }
        return result;
    }

    public synchronized Map<String, Object> query(String service, Status status, Instant from, Instant to, int page, int size) {
        List<CallLog> filtered = logs.reversed().stream()
                .filter(l -> service == null || service.isBlank() || l.serviceId().equals(service))
                .filter(l -> status == null || l.status() == status)
                .filter(l -> from == null || !l.timestamp().isBefore(from))
                .filter(l -> to == null || !l.timestamp().isAfter(to))
                .toList();
        int safeSize = Math.clamp(size, 1, 100);
        int totalPages = Math.max(1, (int) Math.ceil(filtered.size() / (double) safeSize));
        int safePage = Math.clamp(page, 0, totalPages - 1);
        int fromIndex = safePage * safeSize;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", filtered.subList(fromIndex, Math.min(filtered.size(), fromIndex + safeSize)));
        result.put("page", safePage);
        result.put("size", safeSize);
        result.put("totalElements", filtered.size());
        result.put("totalPages", totalPages);
        return result;
    }

    private static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
