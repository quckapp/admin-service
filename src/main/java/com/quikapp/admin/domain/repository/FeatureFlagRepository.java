package com.quikapp.admin.domain.repository;

import com.quikapp.admin.domain.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {
    Optional<FeatureFlag> findByFeatureKey(String featureKey);
    List<FeatureFlag> findByEnabledTrue();
    List<FeatureFlag> findByWorkspaceIdOrWorkspaceIdIsNull(UUID workspaceId);
    boolean existsByFeatureKey(String featureKey);
}
