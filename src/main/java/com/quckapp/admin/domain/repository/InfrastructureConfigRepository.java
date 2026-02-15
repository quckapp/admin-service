package com.quckapp.admin.domain.repository;

import com.quckapp.admin.domain.entity.InfrastructureConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InfrastructureConfigRepository extends JpaRepository<InfrastructureConfig, UUID> {
    List<InfrastructureConfig> findByEnvironment(String environment);
    Optional<InfrastructureConfig> findByEnvironmentAndInfraKey(String environment, String infraKey);
    boolean existsByEnvironmentAndInfraKey(String environment, String infraKey);
    long countByEnvironment(String environment);
    void deleteByEnvironment(String environment);
}
