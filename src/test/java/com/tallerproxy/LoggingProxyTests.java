package com.tallerproxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tallerproxy.metrics.CallLog;
import com.tallerproxy.metrics.LogStore;
import com.tallerproxy.metrics.Status;
import com.tallerproxy.proxy.LoggingProxy;
import com.tallerproxy.proxy.ServiceCallException;
import com.tallerproxy.service.InventoryService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LoggingProxyTests {

    private final LogStore store = new LogStore();
    private final LoggingProxy<Map<String, Object>> proxy = new LoggingProxy<>("inventory", new InventoryService(), store);

    @Test
    void recordsSuccessfulCall() {
        Map<String, Object> result = proxy.execute("check", Map.of("sku", "mouse"));

        CallLog log = only(null);
        assertEquals(75, result.get("available"));
        assertEquals(Status.SUCCESS, log.status());
        assertEquals("check", log.operation());
        assertEquals("mouse", log.params().get("sku"));
    }

    @Test
    void recordsFailureAndRethrows() {
        assertThrows(ServiceCallException.class, () -> proxy.execute("check", Map.of("sku", "laptop")));

        CallLog log = only(Status.ERROR);
        assertEquals("NoSuchElementException: SKU not found: laptop", log.error());
        assertFalse(log.stackTrace().isEmpty());
    }

    @Test
    void filtersLogsByStatus() {
        proxy.execute("list");
        assertThrows(ServiceCallException.class, () -> proxy.execute("explode"));

        assertEquals(1, ((List<?>) store.query(null, Status.SUCCESS, null, null, 0, 10).get("content")).size());
        assertEquals(50.0, store.summary().getFirst().get("errorRate"));
    }

    private CallLog only(Status status) {
        List<?> content = (List<?>) store.query("inventory", status, null, null, 0, 10).get("content");
        assertEquals(1, content.size());
        return (CallLog) content.getFirst();
    }
}
