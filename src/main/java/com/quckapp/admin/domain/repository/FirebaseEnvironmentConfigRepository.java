package com.quckapp.admin.domain.repository;

import com.quckapp.admin.domain.entity.FirebaseEnvironmentConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FirebaseEnvironmentConfigRepository extends JpaRepository<FirebaseEnvironmentConfig, UUID> {
    Optional<FirebaseEnvironmentConfig> findByEnvironment(String environment);
    boolean existsByEnvironment(String environment);
}
