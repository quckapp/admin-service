package com.quckapp.admin.domain.repository;

import com.quckapp.admin.domain.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, UUID> {
    Optional<SystemSettings> findBySettingKey(String settingKey);
    List<SystemSettings> findByCategory(String category);
    List<SystemSettings> findByEditableTrue();
    boolean existsBySettingKey(String settingKey);
}
