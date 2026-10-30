package com.tallerproxy.web;

import com.tallerproxy.metrics.LogStore;
import com.tallerproxy.proxy.LoggingProxy;
import com.tallerproxy.proxy.MicroserviceProxy;
import com.tallerproxy.service.InventoryService;
import com.tallerproxy.service.OrderService;
import com.tallerproxy.service.PaymentService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ServiceConfig implements WebMvcConfigurer {

    @Value("${app.allowed-origin}")
    private String allowedOrigin;

    @Bean
    public OrderService orderService() {
        return new OrderService();
    }

    @Bean
    public Map<String, MicroserviceProxy<Map<String, Object>>> services(LogStore store, OrderService orderService) {
        return Map.of(
                "inventory", new LoggingProxy<>("inventory", new InventoryService(), store),
                "orders", new LoggingProxy<>("orders", orderService, store),
                "payments", new LoggingProxy<>("payments", new PaymentService(), store));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOriginPatterns(allowedOrigin).allowedMethods("GET", "POST");
    }
}
