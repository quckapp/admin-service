package com.quckapp.admin.dto;

import com.quckapp.admin.domain.entity.MaintenanceWindow;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminDtos {

    // ===== System Settings DTOs =====

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SystemSettingRequest {
        @NotBlank @Size(max = 50) private String category;
        @NotBlank @Size(max = 100) private String settingKey;
        private String settingValue;
        @Size(max = 255) private String description;
        private boolean encrypted;
        private boolean editable;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SystemSettingResponse {
        private UUID id;
        private String category;
        private String settingKey;
        private String settingValue;
        private String description;
        private boolean encrypted;
        private boolean editable;
        private UUID updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }

    // ===== Workspace Settings DTOs =====

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class WorkspaceSettingRequest {
        @NotNull private UUID workspaceId;
        @NotBlank @Size(max = 100) private String settingKey;
        private String settingValue;
        @Size(max = 255) private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class WorkspaceSettingResponse {
        private UUID id;
        private UUID workspaceId;
        private String settingKey;
        private String settingValue;
        private String description;
        private UUID updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }

    // ===== Feature Flag DTOs =====

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FeatureFlagRequest {
        @NotBlank @Size(max = 100) private String featureKey;
        @NotBlank @Size(max = 255) private String name;
        @Size(max = 500) private String description;
        private boolean enabled;
        private Map<String, Object> targetRules;
        @Min(0) @Max(100) private int rolloutPercentage;
        private UUID workspaceId;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FeatureFlagResponse {
        private UUID id;
        private String featureKey;
        private String name;
        private String description;
        private boolean enabled;
        private Map<String, Object> targetRules;
        private int rolloutPercentage;
        private UUID workspaceId;
        private Instant createdAt;
        private Instant updatedAt;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FeatureCheckRequest {
        @NotBlank private String featureKey;
        private UUID userId;
        private UUID workspaceId;
        private Map<String, Object> context;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class FeatureCheckResponse {
        private String featureKey;
        private boolean enabled;
        private String reason;
    }

    // ===== Maintenance Window DTOs =====

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MaintenanceWindowRequest {
        @NotBlank @Size(max = 200) private String title;
        private String description;
        @NotNull private Instant startTime;
        @NotNull private Instant endTime;
        @NotNull private MaintenanceWindow.MaintenanceType type;
        private List<String> affectedServices;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MaintenanceWindowResponse {
        private UUID id;
        private String title;
        private String description;
        private Instant startTime;
        private Instant endTime;
        private MaintenanceWindow.MaintenanceType type;
        private MaintenanceWindow.MaintenanceStatus status;
        private List<String> affectedServices;
        private UUID createdBy;
        private Instant createdAt;
    }

    // ===== System Health DTOs =====

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SystemHealthResponse {
        private String status;
        private Map<String, ServiceHealth> services;
        private Instant timestamp;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ServiceHealth {
        private String name;
        private String status;
        private String url;
        private long responseTimeMs;
        private String error;
    }

    // ===== API Response =====

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private Instant timestamp;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder().success(true).data(data).timestamp(Instant.now()).build();
        }
        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder().success(true).message(message).data(data).timestamp(Instant.now()).build();
        }
        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder().success(false).message(message).timestamp(Instant.now()).build();
        }
    }
}
