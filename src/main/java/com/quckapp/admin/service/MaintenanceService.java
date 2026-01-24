package com.quckapp.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quckapp.admin.domain.entity.MaintenanceWindow;
import com.quckapp.admin.domain.repository.MaintenanceWindowRepository;
import com.quckapp.admin.dto.AdminDtos.*;
import com.quckapp.admin.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MaintenanceService {

    private final MaintenanceWindowRepository maintenanceRepository;
    private final ObjectMapper objectMapper;

    public MaintenanceWindowResponse scheduleMaintenance(MaintenanceWindowRequest request, UUID createdBy) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        MaintenanceWindow window = MaintenanceWindow.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .startTime(request.getStartTime())
            .endTime(request.getEndTime())
            .type(request.getType())
            .status(MaintenanceWindow.MaintenanceStatus.SCHEDULED)
            .affectedServices(toJson(request.getAffectedServices()))
            .createdBy(createdBy)
            .build();

        window = maintenanceRepository.save(window);
        log.info("Scheduled maintenance window: {} from {} to {}", window.getTitle(), window.getStartTime(), window.getEndTime());
        return mapToResponse(window);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceWindowResponse> getActiveAndScheduled() {
        return maintenanceRepository.findActiveAndScheduled().stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceWindowResponse> getCurrentMaintenance() {
        return maintenanceRepository.findCurrentMaintenance(Instant.now()).stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MaintenanceWindowResponse> getUpcoming() {
        return maintenanceRepository.findUpcoming(Instant.now()).stream()
            .map(this::mapToResponse)
            .toList();
    }

    public MaintenanceWindowResponse updateStatus(UUID id, MaintenanceWindow.MaintenanceStatus status) {
        MaintenanceWindow window = maintenanceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Maintenance window not found"));

        window.setStatus(status);
        window = maintenanceRepository.save(window);
        log.info("Updated maintenance window {} status to {}", id, status);
        return mapToResponse(window);
    }

    public void cancelMaintenance(UUID id) {
        MaintenanceWindow window = maintenanceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Maintenance window not found"));

        if (window.getStatus() == MaintenanceWindow.MaintenanceStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed maintenance");
        }

        window.setStatus(MaintenanceWindow.MaintenanceStatus.CANCELLED);
        maintenanceRepository.save(window);
        log.info("Cancelled maintenance window: {}", id);
    }

    @Scheduled(fixedRate = 60000) // Check every minute
    public void autoUpdateMaintenanceStatus() {
        Instant now = Instant.now();

        // Start scheduled maintenance windows
        maintenanceRepository.findByStatus(MaintenanceWindow.MaintenanceStatus.SCHEDULED)
            .stream()
            .filter(w -> w.getStartTime().isBefore(now) || w.getStartTime().equals(now))
            .forEach(w -> {
                w.setStatus(MaintenanceWindow.MaintenanceStatus.IN_PROGRESS);
                maintenanceRepository.save(w);
                log.info("Started maintenance window: {}", w.getId());
            });

        // Complete in-progress maintenance windows
        maintenanceRepository.findByStatus(MaintenanceWindow.MaintenanceStatus.IN_PROGRESS)
            .stream()
            .filter(w -> w.getEndTime().isBefore(now))
            .forEach(w -> {
                w.setStatus(MaintenanceWindow.MaintenanceStatus.COMPLETED);
                maintenanceRepository.save(w);
                log.info("Completed maintenance window: {}", w.getId());
            });
    }

    @SuppressWarnings("unchecked")
    private MaintenanceWindowResponse mapToResponse(MaintenanceWindow window) {
        List<String> affectedServices = null;
        if (window.getAffectedServices() != null) {
            try {
                affectedServices = objectMapper.readValue(window.getAffectedServices(), List.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse affected services", e);
            }
        }

        return MaintenanceWindowResponse.builder()
            .id(window.getId())
            .title(window.getTitle())
            .description(window.getDescription())
            .startTime(window.getStartTime())
            .endTime(window.getEndTime())
            .type(window.getType())
            .status(window.getStatus())
            .affectedServices(affectedServices)
            .createdBy(window.getCreatedBy())
            .createdAt(window.getCreatedAt())
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
}
