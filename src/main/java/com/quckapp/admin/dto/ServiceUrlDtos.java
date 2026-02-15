package com.quckapp.admin.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ServiceUrlDtos {

    // ===== Service URL DTOs =====

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ServiceUrlResponse {
        private UUID id;
        private String environment;
        private String serviceKey;
        private String category;
        private String url;
        private String description;
        private boolean isActive;
        private UUID updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateServiceUrlRequest {
        @NotBlank @Size(max = 50) private String serviceKey;
        @NotBlank @Size(max = 20) private String category;
        @NotBlank @Size(max = 512) private String url;
        @Size(max = 255) private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateServiceUrlRequest {
        @Size(max = 512) private String url;
        @Size(max = 255) private String description;
        private Boolean isActive;
    }

    // ===== Infrastructure DTOs =====

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class InfrastructureResponse {
        private UUID id;
        private String environment;
        private String infraKey;
        private String host;
        private int port;
        private String username;
        private String connectionString;
        private boolean isActive;
        private UUID updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateInfrastructureRequest {
        @NotBlank @Size(max = 30) private String infraKey;
        @NotBlank @Size(max = 255) private String host;
        @NotNull private Integer port;
        @Size(max = 100) private String username;
        @Size(max = 512) private String password;
        @Size(max = 512) private String connectionString;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpdateInfrastructureRequest {
        @Size(max = 255) private String host;
        private Integer port;
        @Size(max = 100) private String username;
        @Size(max = 512) private String password;
        @Size(max = 512) private String connectionString;
        private Boolean isActive;
    }

    // ===== Firebase DTOs =====

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FirebaseConfigResponse {
        private UUID id;
        private String environment;
        private String projectId;
        private String clientEmail;
        private String privateKeyMasked;
        private String storageBucket;
        private boolean isActive;
        private Instant updatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UpsertFirebaseConfigRequest {
        @Size(max = 100) private String projectId;
        @Size(max = 255) private String clientEmail;
        private String privateKey;
        @Size(max = 255) private String storageBucket;
    }

    // ===== Bulk Operations DTOs =====

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BulkImportRequest {
        private List<CreateServiceUrlRequest> services;
        private List<CreateInfrastructureRequest> infrastructure;
        private UpsertFirebaseConfigRequest firebase;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class BulkExportResponse {
        private String environment;
        private List<ServiceUrlResponse> services;
        private List<InfrastructureResponse> infrastructure;
        private FirebaseConfigResponse firebase;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CloneEnvironmentRequest {
        @NotBlank private String sourceEnvironment;
        @NotBlank private String targetEnvironment;
    }

    // ===== Environment Summary =====

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class EnvironmentSummary {
        private String environment;
        private long serviceCount;
        private long infraCount;
        private boolean hasFirebase;
        private Instant lastUpdated;
    }

    // ===== URL Validation =====

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ValidateUrlRequest {
        @NotBlank private String url;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ValidateUrlResponse {
        private String url;
        private boolean valid;
        private String message;
    }
}
