package com.quikapp.admin.service;

import com.quikapp.admin.domain.entity.SystemSettings;
import com.quikapp.admin.domain.repository.SystemSettingsRepository;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SystemSettingsService {

    private final SystemSettingsRepository settingsRepository;

    public SystemSettingResponse createSetting(SystemSettingRequest request, UUID updatedBy) {
        if (settingsRepository.existsBySettingKey(request.getSettingKey())) {
            throw new DuplicateResourceException("Setting key already exists");
        }

        SystemSettings settings = SystemSettings.builder()
            .category(request.getCategory())
            .settingKey(request.getSettingKey())
            .settingValue(request.getSettingValue())
            .description(request.getDescription())
            .encrypted(request.isEncrypted())
            .editable(request.isEditable())
            .updatedBy(updatedBy)
            .build();

        settings = settingsRepository.save(settings);
        log.info("Created system setting: {}", settings.getSettingKey());
        return mapToResponse(settings);
    }

    @Cacheable(value = "systemSettings", key = "#key")
    @Transactional(readOnly = true)
    public SystemSettingResponse getSettingByKey(String key) {
        SystemSettings settings = settingsRepository.findBySettingKey(key)
            .orElseThrow(() -> new ResourceNotFoundException("Setting not found"));
        return mapToResponse(settings);
    }

    @Transactional(readOnly = true)
    public List<SystemSettingResponse> getSettingsByCategory(String category) {
        return settingsRepository.findByCategory(category).stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SystemSettingResponse> getAllSettings() {
        return settingsRepository.findAll().stream()
            .map(this::mapToResponse)
            .toList();
    }

    @CacheEvict(value = "systemSettings", key = "#key")
    public SystemSettingResponse updateSetting(String key, String value, UUID updatedBy) {
        SystemSettings settings = settingsRepository.findBySettingKey(key)
            .orElseThrow(() -> new ResourceNotFoundException("Setting not found"));

        if (!settings.isEditable()) {
            throw new IllegalStateException("Setting is not editable");
        }

        settings.setSettingValue(value);
        settings.setUpdatedBy(updatedBy);
        settings = settingsRepository.save(settings);
        log.info("Updated system setting: {}", key);
        return mapToResponse(settings);
    }

    @CacheEvict(value = "systemSettings", key = "#key")
    public void deleteSetting(String key) {
        SystemSettings settings = settingsRepository.findBySettingKey(key)
            .orElseThrow(() -> new ResourceNotFoundException("Setting not found"));
        settingsRepository.delete(settings);
        log.info("Deleted system setting: {}", key);
    }

    private SystemSettingResponse mapToResponse(SystemSettings settings) {
        return SystemSettingResponse.builder()
            .id(settings.getId())
            .category(settings.getCategory())
            .settingKey(settings.getSettingKey())
            .settingValue(settings.isEncrypted() ? "***" : settings.getSettingValue())
            .description(settings.getDescription())
            .encrypted(settings.isEncrypted())
            .editable(settings.isEditable())
            .updatedBy(settings.getUpdatedBy())
            .createdAt(settings.getCreatedAt())
            .updatedAt(settings.getUpdatedAt())
            .build();
    }
}
