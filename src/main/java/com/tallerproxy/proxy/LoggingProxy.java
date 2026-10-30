package com.tallerproxy.proxy;

import com.tallerproxy.metrics.CallLog;
import com.tallerproxy.metrics.LogStore;
import com.tallerproxy.metrics.Status;
import com.tallerproxy.service.Params;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProxy<T> implements MicroserviceProxy<T> {

    private static final Logger log = LoggerFactory.getLogger(LoggingProxy.class);

    private final String serviceId;
    private final MicroserviceProxy<T> target;
    private final LogStore store;

    public LoggingProxy(String serviceId, MicroserviceProxy<T> target, LogStore store) {
        this.serviceId = serviceId;
        this.target = target;
        this.store = store;
    }

    @Override
    public T execute(String operation, Object... params) {
        String requestId = UUID.randomUUID().toString();
        Instant timestamp = Instant.now();
        long start = System.nanoTime();
        log.info("[{}] {}.{} started", requestId, serviceId, operation);
        try {
            T result = target.execute(operation, params);
            long durationMs = elapsed(start);
            store.add(new CallLog(requestId, serviceId, operation, durationMs, Status.SUCCESS, timestamp,
                    Params.of(params), result, null, List.of()));
            log.info("[{}] {}.{} SUCCESS in {} ms", requestId, serviceId, operation, durationMs);
            return result;
        } catch (RuntimeException e) {
            long durationMs = elapsed(start);
            String error = e.getClass().getSimpleName() + ": " + e.getMessage();
            List<String> trace = Arrays.stream(e.getStackTrace()).limit(3).map(StackTraceElement::toString).toList();
            store.add(new CallLog(requestId, serviceId, operation, durationMs, Status.ERROR, timestamp,
                    Params.of(params), null, error, trace));
            log.error("[{}] {}.{} ERROR in {} ms: {} at {}", requestId, serviceId, operation, durationMs, error, trace);
            throw new ServiceCallException(requestId, e);
        }
    }

    private static long elapsed(long start) {
        return (System.nanoTime() - start) / 1_000_000;
    }
}
