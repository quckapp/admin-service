package com.quikapp.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quikapp.admin.domain.entity.FeatureFlag;
import com.quikapp.admin.domain.repository.FeatureFlagRepository;
import com.quikapp.admin.dto.AdminDtos.*;
import com.quikapp.admin.exception.DuplicateResourceException;
import com.quikapp.admin.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FeatureFlagService {

    private final FeatureFlagRepository flagRepository;
    private final ObjectMapper objectMapper;

    public FeatureFlagResponse createFlag(FeatureFlagRequest request) {
        if (flagRepository.existsByFeatureKey(request.getFeatureKey())) {
            throw new DuplicateResourceException("Feature flag key already exists");
        }

        FeatureFlag flag = FeatureFlag.builder()
            .featureKey(request.getFeatureKey())
            .name(request.getName())
            .description(request.getDescription())
            .enabled(request.isEnabled())
            .targetRules(toJson(request.getTargetRules()))
            .rolloutPercentage(request.getRolloutPercentage())
            .workspaceId(request.getWorkspaceId())
            .build();

        flag = flagRepository.save(flag);
        log.info("Created feature flag: {}", flag.getFeatureKey());
        return mapToResponse(flag);
    }

    @Cacheable(value = "featureFlags", key = "#featureKey")
    @Transactional(readOnly = true)
    public FeatureFlagResponse getFlagByKey(String featureKey) {
        FeatureFlag flag = flagRepository.findByFeatureKey(featureKey)
            .orElseThrow(() -> new ResourceNotFoundException("Feature flag not found"));
        return mapToResponse(flag);
    }

    @Transactional(readOnly = true)
    public List<FeatureFlagResponse> getAllFlags() {
        return flagRepository.findAll().stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FeatureFlagResponse> getFlagsForWorkspace(UUID workspaceId) {
        return flagRepository.findByWorkspaceIdOrWorkspaceIdIsNull(workspaceId).stream()
            .map(this::mapToResponse)
            .toList();
    }

    @CacheEvict(value = "featureFlags", key = "#featureKey")
    public FeatureFlagResponse updateFlag(String featureKey, FeatureFlagRequest request) {
        FeatureFlag flag = flagRepository.findByFeatureKey(featureKey)
            .orElseThrow(() -> new ResourceNotFoundException("Feature flag not found"));

        if (request.getName() != null) flag.setName(request.getName());
        if (request.getDescription() != null) flag.setDescription(request.getDescription());
        flag.setEnabled(request.isEnabled());
        if (request.getTargetRules() != null) flag.setTargetRules(toJson(request.getTargetRules()));
        flag.setRolloutPercentage(request.getRolloutPercentage());

        flag = flagRepository.save(flag);
        log.info("Updated feature flag: {}", featureKey);
        return mapToResponse(flag);
    }

    @Transactional(readOnly = true)
    public FeatureCheckResponse checkFeature(FeatureCheckRequest request) {
        FeatureFlag flag = flagRepository.findByFeatureKey(request.getFeatureKey())
            .orElse(null);

        if (flag == null) {
            return FeatureCheckResponse.builder()
                .featureKey(request.getFeatureKey())
                .enabled(false)
                .reason("Feature flag not found")
                .build();
        }

        if (!flag.isEnabled()) {
            return FeatureCheckResponse.builder()
                .featureKey(request.getFeatureKey())
                .enabled(false)
                .reason("Feature is disabled")
                .build();
        }

        // Check workspace scope
        if (flag.getWorkspaceId() != null && !flag.getWorkspaceId().equals(request.getWorkspaceId())) {
            return FeatureCheckResponse.builder()
                .featureKey(request.getFeatureKey())
                .enabled(false)
                .reason("Feature not available for this workspace")
                .build();
        }

        // Check rollout percentage
        if (flag.getRolloutPercentage() < 100 && request.getUserId() != null) {
            int userBucket = Math.abs(request.getUserId().hashCode() % 100);
            if (userBucket >= flag.getRolloutPercentage()) {
                return FeatureCheckResponse.builder()
                    .featureKey(request.getFeatureKey())
                    .enabled(false)
                    .reason("User not in rollout group")
                    .build();
            }
        }

        return FeatureCheckResponse.builder()
            .featureKey(request.getFeatureKey())
            .enabled(true)
            .reason("Feature enabled")
            .build();
    }

    @CacheEvict(value = "featureFlags", key = "#featureKey")
    public void deleteFlag(String featureKey) {
        FeatureFlag flag = flagRepository.findByFeatureKey(featureKey)
            .orElseThrow(() -> new ResourceNotFoundException("Feature flag not found"));
        flagRepository.delete(flag);
        log.info("Deleted feature flag: {}", featureKey);
    }

    private FeatureFlagResponse mapToResponse(FeatureFlag flag) {
        return FeatureFlagResponse.builder()
            .id(flag.getId())
            .featureKey(flag.getFeatureKey())
            .name(flag.getName())
            .description(flag.getDescription())
            .enabled(flag.isEnabled())
            .targetRules(fromJson(flag.getTargetRules()))
            .rolloutPercentage(flag.getRolloutPercentage())
            .workspaceId(flag.getWorkspaceId())
            .createdAt(flag.getCreatedAt())
            .updatedAt(flag.getUpdatedAt())
            .build();
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize to JSON", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fromJson(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize from JSON", e);
            return null;
        }
    }
}
