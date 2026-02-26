package com.quckapp.admin.service;

import com.quckapp.admin.domain.entity.*;
import com.quckapp.admin.domain.repository.GlobalVersionConfigRepository;
import com.quckapp.admin.domain.repository.VersionConfigRepository;
import com.quckapp.admin.domain.repository.VersionProfileRepository;
import com.quckapp.admin.dto.VersionDtos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VersionService {

    private final VersionConfigRepository versionRepo;
    private final GlobalVersionConfigRepository globalConfigRepo;
    private final VersionProfileRepository profileRepo;

    // ===== CRUD Operations =====

    @Transactional(readOnly = true)
    public List<VersionConfigResponse> listVersions(String environment) {
        return versionRepo.findByEnvironment(environment).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VersionConfigResponse> listVersionsForService(String environment, String serviceKey) {
        return versionRepo.findByEnvironmentAndServiceKey(environment, serviceKey).stream()
                .map(this::toResponse)
                .toList();
    }

    public VersionConfigResponse createVersion(CreateVersionRequest request, String updatedBy) {
        versionRepo.findByEnvironmentAndServiceKeyAndApiVersion(
                request.environment(), request.serviceKey(), request.apiVersion()
        ).ifPresent(existing -> {
            throw new IllegalArgumentException(
                    "Version already exists: " + request.serviceKey() + " " + request.apiVersion()
                            + " in " + request.environment());
        });

        VersionConfig config = VersionConfig.builder()
                .environment(request.environment())
                .serviceKey(request.serviceKey())
                .apiVersion(request.apiVersion())
                .releaseVersion(request.releaseVersion())
                .status(request.status() != null ? request.status() : VersionStatus.PLANNED)
                .sunsetDate(request.sunsetDate())
                .sunsetDurationDays(request.sunsetDurationDays())
                .changelog(request.changelog())
                .updatedBy(updatedBy)
                .build();

        config = versionRepo.save(config);
        log.info("Created version config: {} {} in {} [{}]",
                config.getServiceKey(), config.getApiVersion(), config.getEnvironment(), config.getStatus());
        return toResponse(config);
    }

    public VersionConfigResponse updateVersion(String environment, String serviceKey, String apiVersion,
                                                UpdateVersionRequest request, String updatedBy) {
        VersionConfig config = findVersion(environment, serviceKey, apiVersion);

        if (request.releaseVersion() != null) config.setReleaseVersion(request.releaseVersion());
        if (request.status() != null) config.setStatus(request.status());
        if (request.sunsetDate() != null) config.setSunsetDate(request.sunsetDate());
        if (request.sunsetDurationDays() != null) config.setSunsetDurationDays(request.sunsetDurationDays());
        if (request.changelog() != null) config.setChangelog(request.changelog());
        config.setUpdatedBy(updatedBy);

        config = versionRepo.save(config);
        log.info("Updated version config: {} {} in {}", serviceKey, apiVersion, environment);
        return toResponse(config);
    }

    public void deleteVersion(String environment, String serviceKey, String apiVersion) {
        VersionConfig config = findVersion(environment, serviceKey, apiVersion);

        if (config.getStatus() == VersionStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete an ACTIVE version. Deprecate it first.");
        }

        versionRepo.delete(config);
        log.info("Deleted version config: {} {} in {}", serviceKey, apiVersion, environment);
    }

    // ===== Gated State Transitions =====

    public VersionConfigResponse markReady(String environment, String serviceKey, String apiVersion, String updatedBy) {
        VersionConfig config = findVersion(environment, serviceKey, apiVersion);

        if (config.getStatus() != VersionStatus.PLANNED) {
            throw new IllegalStateException(
                    "Cannot mark as READY: current status is " + config.getStatus() + ", expected PLANNED");
        }

        config.setStatus(VersionStatus.READY);
        config.setUpdatedBy(updatedBy);
        config = versionRepo.save(config);
        log.info("Marked READY: {} {} in {}", serviceKey, apiVersion, environment);
        return toResponse(config);
    }

    public VersionConfigResponse activate(String environment, String serviceKey, String apiVersion, String updatedBy) {
        VersionConfig config = findVersion(environment, serviceKey, apiVersion);

        if (config.getStatus() != VersionStatus.READY) {
            throw new IllegalStateException(
                    "Cannot activate: current status is " + config.getStatus() + ", expected READY");
        }

        config.setStatus(VersionStatus.ACTIVE);
        config.setUpdatedBy(updatedBy);
        config = versionRepo.save(config);
        log.info("Activated: {} {} in {}", serviceKey, apiVersion, environment);
        return toResponse(config);
    }

    public VersionConfigResponse deprecate(String environment, String serviceKey, String apiVersion, String updatedBy) {
        VersionConfig config = findVersion(environment, serviceKey, apiVersion);

        if (config.getStatus() != VersionStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot deprecate: current status is " + config.getStatus() + ", expected ACTIVE");
        }

        // Must have a newer ACTIVE version for this service
        boolean hasNewerActive = versionRepo.existsByEnvironmentAndServiceKeyAndStatusAndApiVersionNot(
                environment, serviceKey, VersionStatus.ACTIVE, apiVersion);
        if (!hasNewerActive) {
            throw new IllegalStateException(
                    "Cannot deprecate: no other ACTIVE version exists for " + serviceKey + " in " + environment
                            + ". Activate a newer version first.");
        }

        config.setStatus(VersionStatus.DEPRECATED);
        config.setDeprecatedAt(LocalDateTime.now());
        config.setUpdatedBy(updatedBy);

        // Auto-calculate sunsetDate using per-service override or global default
        if (config.getSunsetDate() == null) {
            int sunsetDays = config.getSunsetDurationDays() != null
                    ? config.getSunsetDurationDays()
                    : getOrCreateGlobalConfig(environment).getDefaultSunsetDays();
            config.setSunsetDate(LocalDate.now().plusDays(sunsetDays));
        }

        config = versionRepo.save(config);
        log.info("Deprecated: {} {} in {} (sunset: {})", serviceKey, apiVersion, environment, config.getSunsetDate());
        return toResponse(config);
    }

    public VersionConfigResponse disable(String environment, String serviceKey, String apiVersion, String updatedBy) {
        VersionConfig config = findVersion(environment, serviceKey, apiVersion);

        if (config.getStatus() != VersionStatus.DEPRECATED && config.getStatus() != VersionStatus.SUNSET) {
            throw new IllegalStateException(
                    "Cannot disable: current status is " + config.getStatus() + ", expected DEPRECATED or SUNSET");
        }

        config.setStatus(VersionStatus.DISABLED);
        config.setUpdatedBy(updatedBy);
        config = versionRepo.save(config);
        log.info("Disabled: {} {} in {}", serviceKey, apiVersion, environment);
        return toResponse(config);
    }

    // ===== Bulk Operations =====

    public List<VersionConfigResponse> bulkPlan(BulkPlanRequest request, String updatedBy) {
        List<VersionConfigResponse> results = new ArrayList<>();
        for (String serviceKey : request.serviceKeys()) {
            Optional<VersionConfig> existing = versionRepo.findByEnvironmentAndServiceKeyAndApiVersion(
                    request.environment(), serviceKey, request.apiVersion());
            if (existing.isPresent()) {
                log.info("Skipping bulk plan for {} {} — already exists", serviceKey, request.apiVersion());
                results.add(toResponse(existing.get()));
                continue;
            }

            CreateVersionRequest createReq = new CreateVersionRequest(
                    request.environment(), serviceKey, request.apiVersion(),
                    null, VersionStatus.PLANNED, null, null, request.changelog());
            results.add(createVersion(createReq, updatedBy));
        }
        return results;
    }

    public BulkActivateResponse bulkActivate(String environment, String apiVersion, String updatedBy) {
        List<VersionConfig> readyVersions = versionRepo.findByEnvironmentAndStatus(environment, VersionStatus.READY)
                .stream()
                .filter(v -> apiVersion == null || apiVersion.equals(v.getApiVersion()))
                .toList();

        int activated = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (VersionConfig config : readyVersions) {
            try {
                activate(environment, config.getServiceKey(), config.getApiVersion(), updatedBy);
                activated++;
            } catch (Exception e) {
                skipped++;
                errors.add(config.getServiceKey() + " " + config.getApiVersion() + ": " + e.getMessage());
                log.warn("Bulk activate skipped {} {}: {}", config.getServiceKey(), config.getApiVersion(), e.getMessage());
            }
        }

        log.info("Bulk activate in {}: activated={}, skipped={}", environment, activated, skipped);
        return new BulkActivateResponse(environment, apiVersion, activated, skipped, errors);
    }

    // ===== Profile Operations =====

    @Transactional(readOnly = true)
    public List<ProfileResponse> listProfiles() {
        return profileRepo.findAll().stream()
                .map(this::toProfileResponse)
                .toList();
    }

    public ProfileResponse createProfile(CreateProfileRequest request, String createdBy) {
        VersionProfile profile = VersionProfile.builder()
                .name(request.name())
                .description(request.description())
                .createdBy(createdBy)
                .build();

        for (ProfileEntryRequest entryReq : request.entries()) {
            VersionProfileEntry entry = VersionProfileEntry.builder()
                    .profile(profile)
                    .serviceKey(entryReq.serviceKey())
                    .apiVersion(entryReq.apiVersion())
                    .releaseVersion(entryReq.releaseVersion())
                    .build();
            profile.getEntries().add(entry);
        }

        profile = profileRepo.save(profile);
        log.info("Created version profile: {} with {} entries", profile.getName(), profile.getEntries().size());
        return toProfileResponse(profile);
    }

    public ApplyProfileResponse applyProfile(UUID profileId, String environment, boolean activateReady, String updatedBy) {
        VersionProfile profile = profileRepo.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + profileId));

        int applied = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (VersionProfileEntry entry : profile.getEntries()) {
            try {
                Optional<VersionConfig> existing = versionRepo.findByEnvironmentAndServiceKeyAndApiVersion(
                        environment, entry.getServiceKey(), entry.getApiVersion());

                if (existing.isEmpty()) {
                    // Create PLANNED version
                    CreateVersionRequest createReq = new CreateVersionRequest(
                            environment, entry.getServiceKey(), entry.getApiVersion(),
                            entry.getReleaseVersion(), VersionStatus.PLANNED, null, null, null);
                    createVersion(createReq, updatedBy);
                    applied++;
                } else if (activateReady && existing.get().getStatus() == VersionStatus.READY) {
                    // Activate READY versions if flag is set
                    activate(environment, entry.getServiceKey(), entry.getApiVersion(), updatedBy);
                    applied++;
                } else {
                    skipped++;
                }
            } catch (Exception e) {
                skipped++;
                errors.add(entry.getServiceKey() + " " + entry.getApiVersion() + ": " + e.getMessage());
                log.warn("Apply profile skipped {} {}: {}", entry.getServiceKey(), entry.getApiVersion(), e.getMessage());
            }
        }

        log.info("Applied profile '{}' to {}: applied={}, skipped={}", profile.getName(), environment, applied, skipped);
        return new ApplyProfileResponse(environment, profile.getName(), applied, skipped, errors);
    }

    public void deleteProfile(UUID profileId) {
        VersionProfile profile = profileRepo.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + profileId));
        profileRepo.delete(profile);
        log.info("Deleted version profile: {}", profile.getName());
    }

    // ===== Global Config =====

    @Transactional(readOnly = true)
    public GlobalConfigResponse getGlobalConfig(String environment) {
        GlobalVersionConfig config = getOrCreateGlobalConfig(environment);
        return toGlobalConfigResponse(config);
    }

    public GlobalConfigResponse updateGlobalConfig(String environment, GlobalConfigRequest request, String updatedBy) {
        GlobalVersionConfig config = getOrCreateGlobalConfig(environment);

        if (request.defaultApiVersion() != null) config.setDefaultApiVersion(request.defaultApiVersion());
        if (request.defaultSunsetDays() != null) config.setDefaultSunsetDays(request.defaultSunsetDays());
        config.setUpdatedBy(updatedBy);

        config = globalConfigRepo.save(config);
        log.info("Updated global version config for {}: defaultVersion={}, sunsetDays={}",
                environment, config.getDefaultApiVersion(), config.getDefaultSunsetDays());
        return toGlobalConfigResponse(config);
    }

    // ===== Export =====

    @Transactional(readOnly = true)
    public ExportEnvFileResponse exportEnvFile(String environment) {
        List<VersionConfig> versions = versionRepo.findByEnvironmentAndStatus(environment, VersionStatus.ACTIVE);

        StringBuilder sb = new StringBuilder();
        sb.append("# Version configuration for environment: ").append(environment).append("\n");
        sb.append("# Generated at: ").append(LocalDateTime.now()).append("\n\n");

        for (VersionConfig v : versions) {
            String envVarName = v.getServiceKey().toUpperCase().replace("-", "_") + "_API_VERSION";
            sb.append(envVarName).append("=").append(v.getApiVersion()).append("\n");
            if (v.getReleaseVersion() != null) {
                String releaseVarName = v.getServiceKey().toUpperCase().replace("-", "_") + "_RELEASE_VERSION";
                sb.append(releaseVarName).append("=").append(v.getReleaseVersion()).append("\n");
            }
        }

        String content = sb.toString();
        log.info("Exported env file for {}: {} entries", environment, versions.size());
        return new ExportEnvFileResponse(environment, content, versions.size());
    }

    // ===== Public Config =====

    @Transactional(readOnly = true)
    public PublicVersionMapResponse getPublicVersionMap(String environment) {
        GlobalVersionConfig globalConfig = getOrCreateGlobalConfig(environment);
        List<VersionConfig> allVersions = versionRepo.findByEnvironment(environment);

        // Group by serviceKey
        Map<String, List<VersionConfig>> byService = allVersions.stream()
                .collect(Collectors.groupingBy(VersionConfig::getServiceKey));

        Map<String, ServiceVersionInfo> services = new LinkedHashMap<>();
        for (Map.Entry<String, List<VersionConfig>> entry : byService.entrySet()) {
            String serviceKey = entry.getKey();
            List<VersionConfig> versions = entry.getValue();

            // Find the ACTIVE version (take the first if multiple)
            VersionConfig activeVersion = versions.stream()
                    .filter(v -> v.getStatus() == VersionStatus.ACTIVE)
                    .findFirst()
                    .orElse(null);

            // Collect available versions (ACTIVE + DEPRECATED, i.e. still usable)
            List<String> availableVersions = versions.stream()
                    .filter(v -> v.getStatus() == VersionStatus.ACTIVE || v.getStatus() == VersionStatus.DEPRECATED)
                    .map(VersionConfig::getApiVersion)
                    .sorted()
                    .toList();

            // Find earliest sunset date among deprecated versions
            LocalDate sunsetDate = versions.stream()
                    .filter(v -> v.getStatus() == VersionStatus.DEPRECATED && v.getSunsetDate() != null)
                    .map(VersionConfig::getSunsetDate)
                    .min(LocalDate::compareTo)
                    .orElse(null);

            services.put(serviceKey, new ServiceVersionInfo(
                    activeVersion != null ? activeVersion.getApiVersion() : null,
                    activeVersion != null ? activeVersion.getReleaseVersion() : null,
                    availableVersions,
                    sunsetDate
            ));
        }

        return new PublicVersionMapResponse(environment, globalConfig.getDefaultApiVersion(), services);
    }

    // ===== Private Helpers =====

    private VersionConfig findVersion(String environment, String serviceKey, String apiVersion) {
        return versionRepo.findByEnvironmentAndServiceKeyAndApiVersion(environment, serviceKey, apiVersion)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Version not found: " + serviceKey + " " + apiVersion + " in " + environment));
    }

    private GlobalVersionConfig getOrCreateGlobalConfig(String environment) {
        return globalConfigRepo.findByEnvironment(environment)
                .orElseGet(() -> {
                    GlobalVersionConfig config = GlobalVersionConfig.builder()
                            .environment(environment)
                            .build();
                    return globalConfigRepo.save(config);
                });
    }

    private VersionConfigResponse toResponse(VersionConfig config) {
        return new VersionConfigResponse(
                config.getId(),
                config.getEnvironment(),
                config.getServiceKey(),
                config.getApiVersion(),
                config.getReleaseVersion(),
                config.getStatus(),
                config.getSunsetDate(),
                config.getSunsetDurationDays(),
                config.getDeprecatedAt(),
                config.getChangelog(),
                config.getUpdatedBy(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }

    private GlobalConfigResponse toGlobalConfigResponse(GlobalVersionConfig config) {
        return new GlobalConfigResponse(
                config.getId(),
                config.getEnvironment(),
                config.getDefaultApiVersion(),
                config.getDefaultSunsetDays(),
                config.getUpdatedBy(),
                config.getUpdatedAt()
        );
    }

    private ProfileResponse toProfileResponse(VersionProfile profile) {
        List<ProfileEntryResponse> entries = profile.getEntries().stream()
                .map(e -> new ProfileEntryResponse(e.getId(), e.getServiceKey(), e.getApiVersion(), e.getReleaseVersion()))
                .toList();
        return new ProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getDescription(),
                entries,
                profile.getCreatedBy(),
                profile.getCreatedAt()
        );
    }
}
