package com.quckapp.admin.service;

import com.quckapp.admin.domain.entity.FirebaseEnvironmentConfig;
import com.quckapp.admin.domain.entity.InfrastructureConfig;
import com.quckapp.admin.domain.entity.ServiceUrlConfig;
import com.quckapp.admin.domain.repository.FirebaseEnvironmentConfigRepository;
import com.quckapp.admin.domain.repository.InfrastructureConfigRepository;
import com.quckapp.admin.domain.repository.ServiceUrlConfigRepository;
import com.quckapp.admin.dto.ServiceUrlDtos.*;
import com.quckapp.admin.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServiceUrlManagementService {

    private final ServiceUrlConfigRepository serviceUrlRepo;
    private final InfrastructureConfigRepository infraRepo;
    private final FirebaseEnvironmentConfigRepository firebaseRepo;

    private static final List<String> VALID_ENVIRONMENTS = List.of(
        "local", "development", "qa", "uat1", "uat2", "uat3", "staging", "production"
    );

    // ===== Environment Operations =====

    @Transactional(readOnly = true)
    public List<EnvironmentSummary> getEnvironments() {
        return VALID_ENVIRONMENTS.stream().map(env -> {
            long serviceCount = serviceUrlRepo.countByEnvironment(env);
            long infraCount = infraRepo.countByEnvironment(env);
            boolean hasFirebase = firebaseRepo.existsByEnvironment(env);

            Instant lastUpdated = serviceUrlRepo.findByEnvironment(env).stream()
                .map(ServiceUrlConfig::getUpdatedAt)
                .max(Instant::compareTo)
                .orElse(null);

            return EnvironmentSummary.builder()
                .environment(env)
                .serviceCount(serviceCount)
                .infraCount(infraCount)
                .hasFirebase(hasFirebase)
                .lastUpdated(lastUpdated)
                .build();
        }).toList();
    }

    // ===== Service URL Operations =====

    @Cacheable(value = "serviceUrls", key = "#environment")
    @Transactional(readOnly = true)
    public List<ServiceUrlResponse> getServiceUrlsByEnvironment(String environment) {
        validateEnvironment(environment);
        return serviceUrlRepo.findByEnvironment(environment).stream()
            .map(this::mapToServiceUrlResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceUrlResponse> getServiceUrlsByCategory(String environment, String category) {
        validateEnvironment(environment);
        return serviceUrlRepo.findByEnvironmentAndCategory(environment, category).stream()
            .map(this::mapToServiceUrlResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ServiceUrlResponse getServiceUrl(String environment, String serviceKey) {
        validateEnvironment(environment);
        ServiceUrlConfig config = serviceUrlRepo.findByEnvironmentAndServiceKey(environment, serviceKey)
            .orElseThrow(() -> new ResourceNotFoundException("Service URL not found: " + serviceKey));
        return mapToServiceUrlResponse(config);
    }

    @CacheEvict(value = "serviceUrls", key = "#environment")
    public ServiceUrlResponse upsertServiceUrl(String environment, CreateServiceUrlRequest request, UUID userId) {
        validateEnvironment(environment);
        ServiceUrlConfig config = serviceUrlRepo.findByEnvironmentAndServiceKey(environment, request.getServiceKey())
            .orElse(ServiceUrlConfig.builder()
                .environment(environment)
                .serviceKey(request.getServiceKey())
                .build());

        config.setCategory(request.getCategory());
        config.setUrl(request.getUrl());
        config.setDescription(request.getDescription());
        config.setActive(true);
        config.setUpdatedBy(userId);

        config = serviceUrlRepo.save(config);
        log.info("Upserted service URL: {} in {}", request.getServiceKey(), environment);
        return mapToServiceUrlResponse(config);
    }

    @CacheEvict(value = "serviceUrls", key = "#environment")
    public ServiceUrlResponse updateServiceUrl(String environment, String serviceKey, UpdateServiceUrlRequest request, UUID userId) {
        validateEnvironment(environment);
        ServiceUrlConfig config = serviceUrlRepo.findByEnvironmentAndServiceKey(environment, serviceKey)
            .orElseThrow(() -> new ResourceNotFoundException("Service URL not found: " + serviceKey));

        if (request.getUrl() != null) config.setUrl(request.getUrl());
        if (request.getDescription() != null) config.setDescription(request.getDescription());
        if (request.getIsActive() != null) config.setActive(request.getIsActive());
        config.setUpdatedBy(userId);

        config = serviceUrlRepo.save(config);
        log.info("Updated service URL: {} in {}", serviceKey, environment);
        return mapToServiceUrlResponse(config);
    }

    @CacheEvict(value = "serviceUrls", key = "#environment")
    public void deleteServiceUrl(String environment, String serviceKey) {
        validateEnvironment(environment);
        serviceUrlRepo.deleteByEnvironmentAndServiceKey(environment, serviceKey);
        log.info("Deleted service URL: {} from {}", serviceKey, environment);
    }

    // ===== Infrastructure Operations =====

    @Cacheable(value = "infrastructure", key = "#environment")
    @Transactional(readOnly = true)
    public List<InfrastructureResponse> getInfrastructureByEnvironment(String environment) {
        validateEnvironment(environment);
        return infraRepo.findByEnvironment(environment).stream()
            .map(this::mapToInfraResponse)
            .toList();
    }

    @CacheEvict(value = "infrastructure", key = "#environment")
    public InfrastructureResponse upsertInfrastructure(String environment, CreateInfrastructureRequest request, UUID userId) {
        validateEnvironment(environment);
        InfrastructureConfig config = infraRepo.findByEnvironmentAndInfraKey(environment, request.getInfraKey())
            .orElse(InfrastructureConfig.builder()
                .environment(environment)
                .infraKey(request.getInfraKey())
                .build());

        config.setHost(request.getHost());
        config.setPort(request.getPort());
        config.setUsername(request.getUsername());
        if (request.getPassword() != null) config.setPasswordEncrypted(request.getPassword());
        config.setConnectionString(request.getConnectionString());
        config.setActive(true);
        config.setUpdatedBy(userId);

        config = infraRepo.save(config);
        log.info("Upserted infrastructure: {} in {}", request.getInfraKey(), environment);
        return mapToInfraResponse(config);
    }

    @CacheEvict(value = "infrastructure", key = "#environment")
    public InfrastructureResponse updateInfrastructure(String environment, String infraKey, UpdateInfrastructureRequest request, UUID userId) {
        validateEnvironment(environment);
        InfrastructureConfig config = infraRepo.findByEnvironmentAndInfraKey(environment, infraKey)
            .orElseThrow(() -> new ResourceNotFoundException("Infrastructure not found: " + infraKey));

        if (request.getHost() != null) config.setHost(request.getHost());
        if (request.getPort() != null) config.setPort(request.getPort());
        if (request.getUsername() != null) config.setUsername(request.getUsername());
        if (request.getPassword() != null) config.setPasswordEncrypted(request.getPassword());
        if (request.getConnectionString() != null) config.setConnectionString(request.getConnectionString());
        if (request.getIsActive() != null) config.setActive(request.getIsActive());
        config.setUpdatedBy(userId);

        config = infraRepo.save(config);
        log.info("Updated infrastructure: {} in {}", infraKey, environment);
        return mapToInfraResponse(config);
    }

    // ===== Firebase Operations =====

    @Transactional(readOnly = true)
    public FirebaseConfigResponse getFirebaseConfig(String environment) {
        validateEnvironment(environment);
        return firebaseRepo.findByEnvironment(environment)
            .map(this::mapToFirebaseResponse)
            .orElse(null);
    }

    public FirebaseConfigResponse upsertFirebaseConfig(String environment, UpsertFirebaseConfigRequest request, UUID userId) {
        validateEnvironment(environment);
        FirebaseEnvironmentConfig config = firebaseRepo.findByEnvironment(environment)
            .orElse(FirebaseEnvironmentConfig.builder()
                .environment(environment)
                .build());

        if (request.getProjectId() != null) config.setProjectId(request.getProjectId());
        if (request.getClientEmail() != null) config.setClientEmail(request.getClientEmail());
        if (request.getPrivateKey() != null) config.setPrivateKeyEncrypted(request.getPrivateKey());
        if (request.getStorageBucket() != null) config.setStorageBucket(request.getStorageBucket());
        config.setActive(true);
        config.setUpdatedBy(userId);

        config = firebaseRepo.save(config);
        log.info("Upserted Firebase config for {}", environment);
        return mapToFirebaseResponse(config);
    }

    // ===== Bulk Operations =====

    @Transactional(readOnly = true)
    public BulkExportResponse bulkExport(String environment) {
        validateEnvironment(environment);
        return BulkExportResponse.builder()
            .environment(environment)
            .services(getServiceUrlsByEnvironment(environment))
            .infrastructure(getInfrastructureByEnvironment(environment))
            .firebase(getFirebaseConfig(environment))
            .build();
    }

    @CacheEvict(value = {"serviceUrls", "infrastructure"}, key = "#environment")
    public BulkExportResponse bulkImport(String environment, BulkImportRequest request, UUID userId) {
        validateEnvironment(environment);

        if (request.getServices() != null) {
            for (CreateServiceUrlRequest svc : request.getServices()) {
                upsertServiceUrl(environment, svc, userId);
            }
        }

        if (request.getInfrastructure() != null) {
            for (CreateInfrastructureRequest infra : request.getInfrastructure()) {
                upsertInfrastructure(environment, infra, userId);
            }
        }

        if (request.getFirebase() != null) {
            upsertFirebaseConfig(environment, request.getFirebase(), userId);
        }

        log.info("Bulk imported configs for {}", environment);
        return bulkExport(environment);
    }

    @CacheEvict(value = {"serviceUrls", "infrastructure"}, allEntries = true)
    public BulkExportResponse cloneEnvironment(CloneEnvironmentRequest request, UUID userId) {
        validateEnvironment(request.getSourceEnvironment());
        validateEnvironment(request.getTargetEnvironment());

        // Clone service URLs
        List<ServiceUrlConfig> sourceServices = serviceUrlRepo.findByEnvironment(request.getSourceEnvironment());
        for (ServiceUrlConfig source : sourceServices) {
            CreateServiceUrlRequest createReq = CreateServiceUrlRequest.builder()
                .serviceKey(source.getServiceKey())
                .category(source.getCategory())
                .url(source.getUrl())
                .description(source.getDescription())
                .build();
            upsertServiceUrl(request.getTargetEnvironment(), createReq, userId);
        }

        // Clone infrastructure
        List<InfrastructureConfig> sourceInfra = infraRepo.findByEnvironment(request.getSourceEnvironment());
        for (InfrastructureConfig source : sourceInfra) {
            CreateInfrastructureRequest createReq = CreateInfrastructureRequest.builder()
                .infraKey(source.getInfraKey())
                .host(source.getHost())
                .port(source.getPort())
                .username(source.getUsername())
                .connectionString(source.getConnectionString())
                .build();
            upsertInfrastructure(request.getTargetEnvironment(), createReq, userId);
        }

        // Clone Firebase
        firebaseRepo.findByEnvironment(request.getSourceEnvironment()).ifPresent(source -> {
            UpsertFirebaseConfigRequest fbReq = UpsertFirebaseConfigRequest.builder()
                .projectId(source.getProjectId())
                .clientEmail(source.getClientEmail())
                .privateKey(source.getPrivateKeyEncrypted())
                .storageBucket(source.getStorageBucket())
                .build();
            upsertFirebaseConfig(request.getTargetEnvironment(), fbReq, userId);
        });

        log.info("Cloned environment {} -> {}", request.getSourceEnvironment(), request.getTargetEnvironment());
        return bulkExport(request.getTargetEnvironment());
    }

    // ===== URL Validation =====

    public ValidateUrlResponse validateUrl(ValidateUrlRequest request) {
        try {
            URI uri = URI.create(request.getUrl());
            if (uri.getScheme() == null || uri.getHost() == null) {
                return ValidateUrlResponse.builder()
                    .url(request.getUrl())
                    .valid(false)
                    .message("Invalid URL: missing scheme or host")
                    .build();
            }
            return ValidateUrlResponse.builder()
                .url(request.getUrl())
                .valid(true)
                .message("URL format is valid")
                .build();
        } catch (IllegalArgumentException e) {
            return ValidateUrlResponse.builder()
                .url(request.getUrl())
                .valid(false)
                .message("Invalid URL format: " + e.getMessage())
                .build();
        }
    }

    // ===== Private Helpers =====

    private void validateEnvironment(String environment) {
        if (!VALID_ENVIRONMENTS.contains(environment)) {
            throw new IllegalArgumentException("Invalid environment: " + environment +
                ". Valid: " + VALID_ENVIRONMENTS);
        }
    }

    private ServiceUrlResponse mapToServiceUrlResponse(ServiceUrlConfig config) {
        return ServiceUrlResponse.builder()
            .id(config.getId())
            .environment(config.getEnvironment())
            .serviceKey(config.getServiceKey())
            .category(config.getCategory())
            .url(config.getUrl())
            .description(config.getDescription())
            .isActive(config.isActive())
            .updatedBy(config.getUpdatedBy())
            .createdAt(config.getCreatedAt())
            .updatedAt(config.getUpdatedAt())
            .build();
    }

    private InfrastructureResponse mapToInfraResponse(InfrastructureConfig config) {
        return InfrastructureResponse.builder()
            .id(config.getId())
            .environment(config.getEnvironment())
            .infraKey(config.getInfraKey())
            .host(config.getHost())
            .port(config.getPort())
            .username(config.getUsername())
            .connectionString(config.getConnectionString())
            .isActive(config.isActive())
            .updatedBy(config.getUpdatedBy())
            .createdAt(config.getCreatedAt())
            .updatedAt(config.getUpdatedAt())
            .build();
    }

    private FirebaseConfigResponse mapToFirebaseResponse(FirebaseEnvironmentConfig config) {
        String maskedKey = null;
        if (config.getPrivateKeyEncrypted() != null) {
            maskedKey = config.getPrivateKeyEncrypted().length() > 20
                ? config.getPrivateKeyEncrypted().substring(0, 10) + "..." +
                  config.getPrivateKeyEncrypted().substring(config.getPrivateKeyEncrypted().length() - 10)
                : "***";
        }
        return FirebaseConfigResponse.builder()
            .id(config.getId())
            .environment(config.getEnvironment())
            .projectId(config.getProjectId())
            .clientEmail(config.getClientEmail())
            .privateKeyMasked(maskedKey)
            .storageBucket(config.getStorageBucket())
            .isActive(config.isActive())
            .updatedAt(config.getUpdatedAt())
            .build();
    }
}
