package com.quckapp.admin.dto;

import com.quckapp.admin.domain.entity.VersionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VersionDtos {

    // ===== Request DTOs =====

    public record CreateVersionRequest(
        @NotBlank String environment,
        @NotBlank String serviceKey,
        @NotBlank @Pattern(regexp = "^v\\d+(\\.\\d+)?$", message = "apiVersion must match pattern vN or vN.N") String apiVersion,
        String releaseVersion,
        VersionStatus status,
        LocalDate sunsetDate,
        Integer sunsetDurationDays,
        String changelog
    ) {}

    public record UpdateVersionRequest(
        String releaseVersion,
        VersionStatus status,
        LocalDate sunsetDate,
        Integer sunsetDurationDays,
        String changelog
    ) {}

    public record BulkPlanRequest(
        @NotBlank String environment,
        @NotBlank @Pattern(regexp = "^v\\d+(\\.\\d+)?$", message = "apiVersion must match pattern vN or vN.N") String apiVersion,
        @NotEmpty List<String> serviceKeys,
        String changelog
    ) {}

    public record ApplyProfileRequest(
        @NotNull UUID profileId,
        @NotBlank String environment
    ) {}

    public record GlobalConfigRequest(
        @NotBlank @Pattern(regexp = "^v\\d+(\\.\\d+)?$", message = "defaultApiVersion must match pattern vN or vN.N") String defaultApiVersion,
        @Min(1) Integer defaultSunsetDays
    ) {}

    public record CreateProfileRequest(
        @NotBlank String name,
        String description,
        @NotEmpty @Valid List<ProfileEntryRequest> entries
    ) {}

    public record ProfileEntryRequest(
        @NotBlank String serviceKey,
        @NotBlank @Pattern(regexp = "^v\\d+(\\.\\d+)?$", message = "apiVersion must match pattern vN or vN.N") String apiVersion,
        String releaseVersion
    ) {}

    // ===== Response DTOs =====

    public record VersionConfigResponse(
        UUID id,
        String environment,
        String serviceKey,
        String apiVersion,
        String releaseVersion,
        VersionStatus status,
        LocalDate sunsetDate,
        Integer sunsetDurationDays,
        LocalDateTime deprecatedAt,
        String changelog,
        String updatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record GlobalConfigResponse(
        UUID id,
        String environment,
        String defaultApiVersion,
        int defaultSunsetDays,
        String updatedBy,
        LocalDateTime updatedAt
    ) {}

    public record ProfileResponse(
        UUID id,
        String name,
        String description,
        List<ProfileEntryResponse> entries,
        String createdBy,
        LocalDateTime createdAt
    ) {}

    public record ProfileEntryResponse(
        UUID id,
        String serviceKey,
        String apiVersion,
        String releaseVersion
    ) {}

    public record BulkActivateResponse(
        String environment,
        String apiVersion,
        int activated,
        int skipped,
        List<String> errors
    ) {}

    public record ApplyProfileResponse(
        String environment,
        String profileName,
        int applied,
        int skipped,
        List<String> errors
    ) {}

    public record PublicVersionMapResponse(
        String environment,
        String defaultApiVersion,
        Map<String, ServiceVersionInfo> services
    ) {}

    public record ServiceVersionInfo(
        String activeVersion,
        String releaseVersion,
        List<String> availableVersions,
        LocalDate sunsetDate
    ) {}

    public record ExportEnvFileResponse(
        String environment,
        String content,
        int entryCount
    ) {}

}
