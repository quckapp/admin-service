package com.quckapp.admin.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "firebase_configs", indexes = {
    @Index(name = "idx_fc_environment", columnList = "environment")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FirebaseEnvironmentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 20, unique = true)
    private String environment;

    @Column(length = 100)
    private String projectId;

    @Column(length = 255)
    private String clientEmail;

    @Column(columnDefinition = "TEXT")
    private String privateKeyEncrypted;

    @Column(length = 255)
    private String storageBucket;

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
