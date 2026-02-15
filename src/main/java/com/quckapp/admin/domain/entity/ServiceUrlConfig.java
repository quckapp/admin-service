package com.quckapp.admin.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_url_configs", uniqueConstraints = {
    @UniqueConstraint(name = "uk_env_service", columnNames = {"environment", "serviceKey"})
}, indexes = {
    @Index(name = "idx_suc_environment", columnList = "environment"),
    @Index(name = "idx_suc_category", columnList = "category"),
    @Index(name = "idx_suc_service_key", columnList = "serviceKey")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ServiceUrlConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String environment;

    @Column(nullable = false, length = 50)
    private String serviceKey;

    @Column(nullable = false, length = 20)
    private String category;

    @Column(nullable = false, length = 512)
    private String url;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean isActive = true;

    private UUID updatedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
