package com.quikapp.admin.domain.repository;

import com.quikapp.admin.domain.entity.MaintenanceWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceWindowRepository extends JpaRepository<MaintenanceWindow, UUID> {
    List<MaintenanceWindow> findByStatus(MaintenanceWindow.MaintenanceStatus status);

    @Query("SELECT m FROM MaintenanceWindow m WHERE m.status IN ('SCHEDULED', 'IN_PROGRESS') ORDER BY m.startTime")
    List<MaintenanceWindow> findActiveAndScheduled();

    @Query("SELECT m FROM MaintenanceWindow m WHERE m.startTime <= :now AND m.endTime >= :now AND m.status = 'IN_PROGRESS'")
    List<MaintenanceWindow> findCurrentMaintenance(@Param("now") Instant now);

    @Query("SELECT m FROM MaintenanceWindow m WHERE m.startTime > :now AND m.status = 'SCHEDULED' ORDER BY m.startTime")
    List<MaintenanceWindow> findUpcoming(@Param("now") Instant now);
}
