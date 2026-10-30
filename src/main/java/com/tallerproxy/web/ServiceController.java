package com.tallerproxy.web;

import com.tallerproxy.proxy.MicroserviceProxy;
import com.tallerproxy.proxy.ServiceCallException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final Map<String, MicroserviceProxy<Map<String, Object>>> services;

    public ServiceController(Map<String, MicroserviceProxy<Map<String, Object>>> services) {
        this.services = services;
    }

    @PostMapping("/{service}/{operation}")
    public ResponseEntity<Object> call(@PathVariable String service, @PathVariable String operation,
            @RequestBody(required = false) Map<String, Object> body) {
        MicroserviceProxy<Map<String, Object>> proxy = services.get(service);
        if (proxy == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Unknown service: " + service));
        }
        return ResponseEntity.ok(proxy.execute(operation, body == null ? Map.of() : body));
    }

    @ExceptionHandler(ServiceCallException.class)
    public ResponseEntity<Map<String, Object>> failed(ServiceCallException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("requestId", e.getRequestId(), "error", e.getMessage()));
    }
}
