package com.quckapp.admin.domain.repository;

import com.quckapp.admin.domain.entity.ServiceUrlConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceUrlConfigRepository extends JpaRepository<ServiceUrlConfig, UUID> {
    List<ServiceUrlConfig> findByEnvironment(String environment);
    List<ServiceUrlConfig> findByEnvironmentAndCategory(String environment, String category);
    Optional<ServiceUrlConfig> findByEnvironmentAndServiceKey(String environment, String serviceKey);
    boolean existsByEnvironmentAndServiceKey(String environment, String serviceKey);
    long countByEnvironment(String environment);
    void deleteByEnvironmentAndServiceKey(String environment, String serviceKey);
    void deleteByEnvironment(String environment);
}
