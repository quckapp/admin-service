package com.quckapp.admin.controller;

import com.quckapp.admin.dto.AdminDtos.ApiResponse;
import com.quckapp.admin.dto.VersionDtos.*;
import com.quckapp.admin.service.VersionService;
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
@RequestMapping("/api/v1/admin/service-urls")
@RequiredArgsConstructor
@Tag(name = "Version Management", description = "API version lifecycle management with gated promotion")
public class VersionController {

    private final VersionService versionService;

    // TODO: Extract from auth context later
    private static final String DEFAULT_UPDATED_BY = "admin";

    // ===== CRUD Endpoints =====

    @GetMapping("/{env}/versions")
    @Operation(summary = "List all versions for an environment")
    public ResponseEntity<ApiResponse<List<VersionConfigResponse>>> listVersions(
            @PathVariable String env) {
        return ResponseEntity.ok(ApiResponse.success(versionService.listVersions(env)));
    }

    @GetMapping("/{env}/versions/{serviceKey}")
    @Operation(summary = "List versions for a specific service")
    public ResponseEntity<ApiResponse<List<VersionConfigResponse>>> listVersionsForService(
            @PathVariable String env,
            @PathVariable String serviceKey) {
        return ResponseEntity.ok(ApiResponse.success(versionService.listVersionsForService(env, serviceKey)));
    }

    @PostMapping("/{env}/versions")
    @Operation(summary = "Create a new version config")
    public ResponseEntity<ApiResponse<VersionConfigResponse>> createVersion(
            @PathVariable String env,
            @Valid @RequestBody CreateVersionRequest request) {
        // Override environment from path variable for consistency
        CreateVersionRequest effectiveRequest = new CreateVersionRequest(
                env, request.serviceKey(), request.apiVersion(), request.releaseVersion(),
                request.status(), request.sunsetDate(), request.sunsetDurationDays(), request.changelog());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Version created", versionService.createVersion(effectiveRequest, DEFAULT_UPDATED_BY)));
    }

    @PutMapping("/{env}/versions/{serviceKey}/{ver}")
    @Operation(summary = "Update a version config")
    public ResponseEntity<ApiResponse<VersionConfigResponse>> updateVersion(
            @PathVariable String env,
            @PathVariable String serviceKey,
            @PathVariable String ver,
            @Valid @RequestBody UpdateVersionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Version updated",
                versionService.updateVersion(env, serviceKey, ver, request, DEFAULT_UPDATED_BY)));
    }

    @DeleteMapping("/{env}/versions/{serviceKey}/{ver}")
    @Operation(summary = "Delete a version config (cannot delete ACTIVE)")
    public ResponseEntity<ApiResponse<Void>> deleteVersion(
            @PathVariable String env,
            @PathVariable String serviceKey,
            @PathVariable String ver) {
        versionService.deleteVersion(env, serviceKey, ver);
        return ResponseEntity.ok(ApiResponse.success("Version deleted", null));
    }

    // ===== Gated State Transition Endpoints =====

    @PostMapping("/{env}/versions/{serviceKey}/{ver}/ready")
    @Operation(summary = "Mark version as READY (CI callback, PLANNED -> READY)")
    public ResponseEntity<ApiResponse<VersionConfigResponse>> markReady(
            @PathVariable String env,
            @PathVariable String serviceKey,
            @PathVariable String ver) {
        return ResponseEntity.ok(ApiResponse.success("Version marked as READY",
                versionService.markReady(env, serviceKey, ver, DEFAULT_UPDATED_BY)));
    }

    @PostMapping("/{env}/versions/{serviceKey}/{ver}/activate")
    @Operation(summary = "Activate version (READY -> ACTIVE)")
    public ResponseEntity<ApiResponse<VersionConfigResponse>> activate(
            @PathVariable String env,
            @PathVariable String serviceKey,
            @PathVariable String ver) {
        return ResponseEntity.ok(ApiResponse.success("Version activated",
                versionService.activate(env, serviceKey, ver, DEFAULT_UPDATED_BY)));
    }

    @PostMapping("/{env}/versions/{serviceKey}/{ver}/deprecate")
    @Operation(summary = "Deprecate version (ACTIVE -> DEPRECATED, requires newer ACTIVE)")
    public ResponseEntity<ApiResponse<VersionConfigResponse>> deprecate(
            @PathVariable String env,
            @PathVariable String serviceKey,
            @PathVariable String ver) {
        return ResponseEntity.ok(ApiResponse.success("Version deprecated",
                versionService.deprecate(env, serviceKey, ver, DEFAULT_UPDATED_BY)));
    }

    @PostMapping("/{env}/versions/{serviceKey}/{ver}/disable")
    @Operation(summary = "Disable version (DEPRECATED/SUNSET -> DISABLED)")
    public ResponseEntity<ApiResponse<VersionConfigResponse>> disable(
            @PathVariable String env,
            @PathVariable String serviceKey,
            @PathVariable String ver) {
        return ResponseEntity.ok(ApiResponse.success("Version disabled",
                versionService.disable(env, serviceKey, ver, DEFAULT_UPDATED_BY)));
    }

    // ===== Bulk Operation Endpoints =====

    @PostMapping("/{env}/versions/bulk-plan")
    @Operation(summary = "Bulk create PLANNED versions for multiple services")
    public ResponseEntity<ApiResponse<List<VersionConfigResponse>>> bulkPlan(
            @PathVariable String env,
            @Valid @RequestBody BulkPlanRequest request) {
        BulkPlanRequest effectiveRequest = new BulkPlanRequest(
                env, request.apiVersion(), request.serviceKeys(), request.changelog());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk plan completed",
                        versionService.bulkPlan(effectiveRequest, DEFAULT_UPDATED_BY)));
    }

    @PostMapping("/{env}/versions/bulk-activate")
    @Operation(summary = "Activate all READY versions in an environment")
    public ResponseEntity<ApiResponse<BulkActivateResponse>> bulkActivate(
            @PathVariable String env,
            @RequestParam(required = false) String apiVersion) {
        return ResponseEntity.ok(ApiResponse.success("Bulk activate completed",
                versionService.bulkActivate(env, apiVersion, DEFAULT_UPDATED_BY)));
    }

    // ===== Profile Endpoints =====

    @GetMapping("/profiles")
    @Operation(summary = "List all version profiles")
    public ResponseEntity<ApiResponse<List<ProfileResponse>>> listProfiles() {
        return ResponseEntity.ok(ApiResponse.success(versionService.listProfiles()));
    }

    @PostMapping("/profiles")
    @Operation(summary = "Create a version profile")
    public ResponseEntity<ApiResponse<ProfileResponse>> createProfile(
            @Valid @RequestBody CreateProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile created",
                        versionService.createProfile(request, DEFAULT_UPDATED_BY)));
    }

    @PostMapping("/profiles/{id}/apply/{env}")
    @Operation(summary = "Apply a profile to an environment")
    public ResponseEntity<ApiResponse<ApplyProfileResponse>> applyProfile(
            @PathVariable UUID id,
            @PathVariable String env,
            @RequestParam(defaultValue = "false") boolean activateReady) {
        return ResponseEntity.ok(ApiResponse.success("Profile applied",
                versionService.applyProfile(id, env, activateReady, DEFAULT_UPDATED_BY)));
    }

    @DeleteMapping("/profiles/{id}")
    @Operation(summary = "Delete a version profile")
    public ResponseEntity<ApiResponse<Void>> deleteProfile(@PathVariable UUID id) {
        versionService.deleteProfile(id);
        return ResponseEntity.ok(ApiResponse.success("Profile deleted", null));
    }

    // ===== Global Config Endpoints =====

    @GetMapping("/{env}/global-config")
    @Operation(summary = "Get global version config for an environment")
    public ResponseEntity<ApiResponse<GlobalConfigResponse>> getGlobalConfig(
            @PathVariable String env) {
        return ResponseEntity.ok(ApiResponse.success(versionService.getGlobalConfig(env)));
    }

    @PutMapping("/{env}/global-config")
    @Operation(summary = "Update global version config for an environment")
    public ResponseEntity<ApiResponse<GlobalConfigResponse>> updateGlobalConfig(
            @PathVariable String env,
            @Valid @RequestBody GlobalConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Global config updated",
                versionService.updateGlobalConfig(env, request, DEFAULT_UPDATED_BY)));
    }

    // ===== Export Endpoints =====

    @GetMapping("/{env}/export/env-file")
    @Operation(summary = "Export active versions as .env format")
    public ResponseEntity<ApiResponse<ExportEnvFileResponse>> exportEnvFile(
            @PathVariable String env) {
        return ResponseEntity.ok(ApiResponse.success(versionService.exportEnvFile(env)));
    }
}
