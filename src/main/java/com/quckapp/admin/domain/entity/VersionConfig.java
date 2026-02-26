package com.quckapp.admin.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "version_configs", uniqueConstraints = {
    @UniqueConstraint(name = "uk_env_service_version", columnNames = {"environment", "serviceKey", "apiVersion"})
}, indexes = {
    @Index(name = "idx_vc_environment", columnList = "environment"),
    @Index(name = "idx_vc_service_key", columnList = "serviceKey"),
    @Index(name = "idx_vc_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VersionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String environment;

    @Column(nullable = false, length = 50)
    private String serviceKey;

    @Column(nullable = false, length = 20)
    private String apiVersion;

    @Column(length = 50)
    private String releaseVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VersionStatus status = VersionStatus.PLANNED;

    private LocalDate sunsetDate;

    private Integer sunsetDurationDays;

    private LocalDateTime deprecatedAt;

    @Column(columnDefinition = "TEXT")
    private String changelog;

    @Column(length = 255)
    private String updatedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
