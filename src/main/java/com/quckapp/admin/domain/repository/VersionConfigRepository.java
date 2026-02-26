package com.quckapp.admin.domain.repository;

import com.quckapp.admin.domain.entity.VersionConfig;
import com.quckapp.admin.domain.entity.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VersionConfigRepository extends JpaRepository<VersionConfig, UUID> {
    List<VersionConfig> findByEnvironment(String environment);
    List<VersionConfig> findByEnvironmentAndServiceKey(String environment, String serviceKey);
    Optional<VersionConfig> findByEnvironmentAndServiceKeyAndApiVersion(String environment, String serviceKey, String apiVersion);
    List<VersionConfig> findByEnvironmentAndStatus(String environment, VersionStatus status);
    List<VersionConfig> findByEnvironmentAndApiVersion(String environment, String apiVersion);
    boolean existsByEnvironmentAndServiceKeyAndStatusAndApiVersionNot(String environment, String serviceKey, VersionStatus status, String apiVersion);
}
