package com.quckapp.admin.domain.repository;

import com.quckapp.admin.domain.entity.GlobalVersionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GlobalVersionConfigRepository extends JpaRepository<GlobalVersionConfig, UUID> {
    Optional<GlobalVersionConfig> findByEnvironment(String environment);
}
