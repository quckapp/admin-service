package com.quikapp.admin.controller;

import com.quikapp.admin.domain.entity.MaintenanceWindow;
import com.quikapp.admin.dto.AdminDtos.*;
import com.quikapp.admin.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "System administration APIs")
public class AdminController {

    private final SystemSettingsService settingsService;
    private final FeatureFlagService featureFlagService;
    private final MaintenanceService maintenanceService;
    private final HealthCheckService healthCheckService;

    // ===== System Settings Endpoints =====

    @PostMapping("/settings")
    @Operation(summary = "Create system setting")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> createSetting(
            @Valid @RequestBody SystemSettingRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Setting created", settingsService.createSetting(request, userId)));
    }

    @GetMapping("/settings")
    @Operation(summary = "Get all system settings")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success(settingsService.getAllSettings()));
    }

    @GetMapping("/settings/key/{key}")
    @Operation(summary = "Get setting by key")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> getSettingByKey(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success(settingsService.getSettingByKey(key)));
    }

    @GetMapping("/settings/category/{category}")
    @Operation(summary = "Get settings by category")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getSettingsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(settingsService.getSettingsByCategory(category)));
    }

    @PutMapping("/settings/key/{key}")
    @Operation(summary = "Update setting value")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> updateSetting(
            @PathVariable String key,
            @RequestBody String value,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Setting updated", settingsService.updateSetting(key, value, userId)));
    }

    @DeleteMapping("/settings/key/{key}")
    @Operation(summary = "Delete setting")
    public ResponseEntity<ApiResponse<Void>> deleteSetting(@PathVariable String key) {
        settingsService.deleteSetting(key);
        return ResponseEntity.ok(ApiResponse.success("Setting deleted", null));
    }

    // ===== Feature Flags Endpoints =====

    @PostMapping("/features")
    @Operation(summary = "Create feature flag")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> createFeatureFlag(
            @Valid @RequestBody FeatureFlagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Feature flag created", featureFlagService.createFlag(request)));
    }

    @GetMapping("/features")
    @Operation(summary = "Get all feature flags")
    public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> getAllFeatureFlags() {
        return ResponseEntity.ok(ApiResponse.success(featureFlagService.getAllFlags()));
    }

    @GetMapping("/features/{featureKey}")
    @Operation(summary = "Get feature flag by key")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> getFeatureFlag(@PathVariable String featureKey) {
        return ResponseEntity.ok(ApiResponse.success(featureFlagService.getFlagByKey(featureKey)));
    }

    @GetMapping("/features/workspace/{workspaceId}")
    @Operation(summary = "Get feature flags for workspace")
    public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> getFlagsForWorkspace(@PathVariable UUID workspaceId) {
        return ResponseEntity.ok(ApiResponse.success(featureFlagService.getFlagsForWorkspace(workspaceId)));
    }

    @PostMapping("/features/check")
    @Operation(summary = "Check if feature is enabled")
    public ResponseEntity<ApiResponse<FeatureCheckResponse>> checkFeature(@Valid @RequestBody FeatureCheckRequest request) {
        return ResponseEntity.ok(ApiResponse.success(featureFlagService.checkFeature(request)));
    }

    @PutMapping("/features/{featureKey}")
    @Operation(summary = "Update feature flag")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> updateFeatureFlag(
            @PathVariable String featureKey,
            @Valid @RequestBody FeatureFlagRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Feature flag updated", featureFlagService.updateFlag(featureKey, request)));
    }

    @DeleteMapping("/features/{featureKey}")
    @Operation(summary = "Delete feature flag")
    public ResponseEntity<ApiResponse<Void>> deleteFeatureFlag(@PathVariable String featureKey) {
        featureFlagService.deleteFlag(featureKey);
        return ResponseEntity.ok(ApiResponse.success("Feature flag deleted", null));
    }

    // ===== Maintenance Window Endpoints =====

    @PostMapping("/maintenance")
    @Operation(summary = "Schedule maintenance window")
    public ResponseEntity<ApiResponse<MaintenanceWindowResponse>> scheduleMaintenance(
            @Valid @RequestBody MaintenanceWindowRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Maintenance scheduled", maintenanceService.scheduleMaintenance(request, userId)));
    }

    @GetMapping("/maintenance/active")
    @Operation(summary = "Get active and scheduled maintenance")
    public ResponseEntity<ApiResponse<List<MaintenanceWindowResponse>>> getActiveAndScheduled() {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.getActiveAndScheduled()));
    }

    @GetMapping("/maintenance/current")
    @Operation(summary = "Get current maintenance")
    public ResponseEntity<ApiResponse<List<MaintenanceWindowResponse>>> getCurrentMaintenance() {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.getCurrentMaintenance()));
    }

    @GetMapping("/maintenance/upcoming")
    @Operation(summary = "Get upcoming maintenance")
    public ResponseEntity<ApiResponse<List<MaintenanceWindowResponse>>> getUpcomingMaintenance() {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.getUpcoming()));
    }

    @PutMapping("/maintenance/{id}/status")
    @Operation(summary = "Update maintenance status")
    public ResponseEntity<ApiResponse<MaintenanceWindowResponse>> updateMaintenanceStatus(
            @PathVariable UUID id,
            @RequestParam MaintenanceWindow.MaintenanceStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", maintenanceService.updateStatus(id, status)));
    }

    @DeleteMapping("/maintenance/{id}")
    @Operation(summary = "Cancel maintenance")
    public ResponseEntity<ApiResponse<Void>> cancelMaintenance(@PathVariable UUID id) {
        maintenanceService.cancelMaintenance(id);
        return ResponseEntity.ok(ApiResponse.success("Maintenance cancelled", null));
    }

    // ===== Health Check Endpoints =====

    @GetMapping("/health/services")
    @Operation(summary = "Check health of all services")
    public ResponseEntity<ApiResponse<SystemHealthResponse>> checkServicesHealth() {
        return ResponseEntity.ok(ApiResponse.success(healthCheckService.checkAllServices()));
    }
}
