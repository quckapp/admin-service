package com.quikapp.admin.domain.repository;

import com.quikapp.admin.domain.entity.WorkspaceSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceSettingsRepository extends JpaRepository<WorkspaceSettings, UUID> {
    List<WorkspaceSettings> findByWorkspaceId(UUID workspaceId);
    Optional<WorkspaceSettings> findByWorkspaceIdAndSettingKey(UUID workspaceId, String settingKey);
    void deleteByWorkspaceIdAndSettingKey(UUID workspaceId, String settingKey);
    boolean existsByWorkspaceIdAndSettingKey(UUID workspaceId, String settingKey);
}
