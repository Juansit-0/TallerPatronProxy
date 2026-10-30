package com.tallerproxy.web;

import com.tallerproxy.metrics.LogStore;
import com.tallerproxy.metrics.Status;
import com.tallerproxy.proxy.MicroserviceProxy;
import com.tallerproxy.proxy.ServiceCallException;
import com.tallerproxy.service.OrderService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private static final Map<String, List<String>> OPERATIONS = Map.of(
            "inventory", List.of("list", "check", "restock"),
            "orders", List.of("create", "list", "cancel"),
            "payments", List.of("charge", "refund", "status"));
    private static final List<String> SKUS = List.of("keyboard", "mouse", "monitor", "headset", "webcam");

    private final LogStore store;
    private final Map<String, MicroserviceProxy<Map<String, Object>>> services;
    private final OrderService orderService;

    public MetricsController(LogStore store, Map<String, MicroserviceProxy<Map<String, Object>>> services,
            OrderService orderService) {
        this.store = store;
        this.services = services;
        this.orderService = orderService;
    }

    @GetMapping("/summary")
    public List<Map<String, Object>> summary() {
        return store.summary();
    }

    @GetMapping("/logs")
    public Map<String, Object> logs(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return store.query(service, status, from, to, page, size);
    }

    @PostMapping("/simulate-load")
    public Map<String, Object> simulateLoad() {
        AtomicInteger errors = new AtomicInteger();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 50; i++) {
                executor.submit(() -> {
                    try {
                        randomCall();
                    } catch (ServiceCallException e) {
                        errors.incrementAndGet();
                    }
                });
            }
        }
        return Map.of("calls", 50, "errors", errors.get());
    }

    private void randomCall() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<String> ids = List.copyOf(OPERATIONS.keySet());
        String service = ids.get(random.nextInt(ids.size()));
        List<String> operations = OPERATIONS.get(service);
        String operation = operations.get(random.nextInt(operations.size()));
        Map<String, Object> params = Map.of(
                "sku", SKUS.get(random.nextInt(SKUS.size())),
                "quantity", random.nextInt(1, 20),
                "product", SKUS.get(random.nextInt(SKUS.size())),
                "orderId", orderService.randomOrderId(),
                "amount", random.nextInt(20, 900));
        services.get(service).execute(operation, params);
    }
}
