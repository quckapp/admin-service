package com.quckapp.admin.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "infrastructure_configs", uniqueConstraints = {
    @UniqueConstraint(name = "uk_env_infra", columnNames = {"environment", "infraKey"})
}, indexes = {
    @Index(name = "idx_ic_environment", columnList = "environment"),
    @Index(name = "idx_ic_infra_key", columnList = "infraKey")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InfrastructureConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String environment;

    @Column(nullable = false, length = 30)
    private String infraKey;

    @Column(nullable = false, length = 255)
    private String host;

    @Column(nullable = false)
    private int port;

    @Column(length = 100)
    private String username;

    @Column(length = 512)
    private String passwordEncrypted;

    @Column(length = 512)
    private String connectionString;

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
