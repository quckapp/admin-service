package com.quckapp.admin.controller;

import com.quckapp.admin.dto.AdminDtos.ApiResponse;
import com.quckapp.admin.dto.ServiceUrlDtos.*;
import com.quckapp.admin.service.ServiceUrlManagementService;
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
@RequestMapping("/api/admin/service-urls")
@RequiredArgsConstructor
@Tag(name = "Service URLs", description = "Service URL configuration management per environment")
public class ServiceUrlController {

    private final ServiceUrlManagementService service;

    // ===== Environment Endpoints =====

    @GetMapping("/environments")
    @Operation(summary = "List all environments with summary stats")
    public ResponseEntity<ApiResponse<List<EnvironmentSummary>>> getEnvironments() {
        return ResponseEntity.ok(ApiResponse.success(service.getEnvironments()));
    }

    // ===== Service URL Endpoints =====

    @GetMapping("/environments/{env}/services")
    @Operation(summary = "Get all service URLs for an environment")
    public ResponseEntity<ApiResponse<List<ServiceUrlResponse>>> getServiceUrls(
            @PathVariable String env,
            @RequestParam(required = false) String category) {
        List<ServiceUrlResponse> urls = category != null
            ? service.getServiceUrlsByCategory(env, category)
            : service.getServiceUrlsByEnvironment(env);
        return ResponseEntity.ok(ApiResponse.success(urls));
    }

    @GetMapping("/environments/{env}/services/{key}")
    @Operation(summary = "Get a single service URL")
    public ResponseEntity<ApiResponse<ServiceUrlResponse>> getServiceUrl(
            @PathVariable String env,
            @PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success(service.getServiceUrl(env, key)));
    }

    @PostMapping("/environments/{env}/services")
    @Operation(summary = "Create or update a service URL")
    public ResponseEntity<ApiResponse<ServiceUrlResponse>> upsertServiceUrl(
            @PathVariable String env,
            @Valid @RequestBody CreateServiceUrlRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Service URL saved", service.upsertServiceUrl(env, request, userId)));
    }

    @PutMapping("/environments/{env}/services/{key}")
    @Operation(summary = "Update a service URL")
    public ResponseEntity<ApiResponse<ServiceUrlResponse>> updateServiceUrl(
            @PathVariable String env,
            @PathVariable String key,
            @Valid @RequestBody UpdateServiceUrlRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Service URL updated",
            service.updateServiceUrl(env, key, request, userId)));
    }

    @DeleteMapping("/environments/{env}/services/{key}")
    @Operation(summary = "Delete a service URL")
    public ResponseEntity<ApiResponse<Void>> deleteServiceUrl(
            @PathVariable String env,
            @PathVariable String key) {
        service.deleteServiceUrl(env, key);
        return ResponseEntity.ok(ApiResponse.success("Service URL deleted", null));
    }

    // ===== Infrastructure Endpoints =====

    @GetMapping("/environments/{env}/infrastructure")
    @Operation(summary = "Get all infrastructure configs for an environment")
    public ResponseEntity<ApiResponse<List<InfrastructureResponse>>> getInfrastructure(
            @PathVariable String env) {
        return ResponseEntity.ok(ApiResponse.success(service.getInfrastructureByEnvironment(env)));
    }

    @PostMapping("/environments/{env}/infrastructure")
    @Operation(summary = "Create or update an infrastructure config")
    public ResponseEntity<ApiResponse<InfrastructureResponse>> upsertInfrastructure(
            @PathVariable String env,
            @Valid @RequestBody CreateInfrastructureRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Infrastructure saved", service.upsertInfrastructure(env, request, userId)));
    }

    @PutMapping("/environments/{env}/infrastructure/{key}")
    @Operation(summary = "Update an infrastructure config")
    public ResponseEntity<ApiResponse<InfrastructureResponse>> updateInfrastructure(
            @PathVariable String env,
            @PathVariable String key,
            @Valid @RequestBody UpdateInfrastructureRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Infrastructure updated",
            service.updateInfrastructure(env, key, request, userId)));
    }

    // ===== Firebase Endpoints =====

    @GetMapping("/environments/{env}/firebase")
    @Operation(summary = "Get Firebase config for an environment (private key masked)")
    public ResponseEntity<ApiResponse<FirebaseConfigResponse>> getFirebaseConfig(
            @PathVariable String env) {
        return ResponseEntity.ok(ApiResponse.success(service.getFirebaseConfig(env)));
    }

    @PostMapping("/environments/{env}/firebase")
    @Operation(summary = "Create or update Firebase config")
    public ResponseEntity<ApiResponse<FirebaseConfigResponse>> upsertFirebaseConfig(
            @PathVariable String env,
            @Valid @RequestBody UpsertFirebaseConfigRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Firebase config saved",
            service.upsertFirebaseConfig(env, request, userId)));
    }

    // ===== Bulk Operations =====

    @PostMapping("/environments/{env}/import")
    @Operation(summary = "Bulk import service URLs, infrastructure, and Firebase config")
    public ResponseEntity<ApiResponse<BulkExportResponse>> bulkImport(
            @PathVariable String env,
            @Valid @RequestBody BulkImportRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Bulk import completed",
            service.bulkImport(env, request, userId)));
    }

    @GetMapping("/environments/{env}/export")
    @Operation(summary = "Export all configs for an environment")
    public ResponseEntity<ApiResponse<BulkExportResponse>> bulkExport(
            @PathVariable String env) {
        return ResponseEntity.ok(ApiResponse.success(service.bulkExport(env)));
    }

    @PostMapping("/environments/clone")
    @Operation(summary = "Clone all configs from one environment to another")
    public ResponseEntity<ApiResponse<BulkExportResponse>> cloneEnvironment(
            @Valid @RequestBody CloneEnvironmentRequest request,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Environment cloned",
            service.cloneEnvironment(request, userId)));
    }

    // ===== URL Validation =====

    @PostMapping("/validate-url")
    @Operation(summary = "Validate a URL format")
    public ResponseEntity<ApiResponse<ValidateUrlResponse>> validateUrl(
            @Valid @RequestBody ValidateUrlRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.validateUrl(request)));
    }
}
