package com.quckapp.admin.service;

import com.quckapp.admin.dto.AdminDtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${services.user-service.url:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${services.permission-service.url:http://localhost:8083}")
    private String permissionServiceUrl;

    @Value("${services.audit-service.url:http://localhost:8084}")
    private String auditServiceUrl;

    public SystemHealthResponse checkAllServices() {
        Map<String, ServiceHealth> services = new HashMap<>();

        services.put("auth-service", checkService("auth-service", authServiceUrl));
        services.put("user-service", checkService("user-service", userServiceUrl));
        services.put("permission-service", checkService("permission-service", permissionServiceUrl));
        services.put("audit-service", checkService("audit-service", auditServiceUrl));

        String overallStatus = services.values().stream()
            .allMatch(s -> "UP".equals(s.getStatus())) ? "UP" : "DEGRADED";

        return SystemHealthResponse.builder()
            .status(overallStatus)
            .services(services)
            .timestamp(Instant.now())
            .build();
    }

    private ServiceHealth checkService(String name, String url) {
        long startTime = System.currentTimeMillis();
        try {
            String response = webClientBuilder.build()
                .get()
                .uri(url + "/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> Mono.just("ERROR"))
                .block();

            long responseTime = System.currentTimeMillis() - startTime;

            boolean isUp = response != null && response.contains("UP");
            return ServiceHealth.builder()
                .name(name)
                .status(isUp ? "UP" : "DOWN")
                .url(url)
                .responseTimeMs(responseTime)
                .build();

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.warn("Health check failed for {}: {}", name, e.getMessage());
            return ServiceHealth.builder()
                .name(name)
                .status("DOWN")
                .url(url)
                .responseTimeMs(responseTime)
                .error(e.getMessage())
                .build();
        }
    }
}
