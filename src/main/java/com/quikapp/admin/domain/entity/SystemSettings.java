package com.quikapp.admin.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "system_settings", indexes = {
    @Index(name = "idx_settings_category", columnList = "category"),
    @Index(name = "idx_settings_key", columnList = "settingKey")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SystemSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 100, unique = true)
    private String settingKey;

    @Column(columnDefinition = "TEXT")
    private String settingValue;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean encrypted;

    @Column(nullable = false)
    private boolean editable;

    private UUID updatedBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
