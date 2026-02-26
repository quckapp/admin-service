package com.quckapp.admin.domain.repository;

import com.quckapp.admin.domain.entity.VersionProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VersionProfileRepository extends JpaRepository<VersionProfile, UUID> {
}
