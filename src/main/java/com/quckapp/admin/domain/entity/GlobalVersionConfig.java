package com.quckapp.admin.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "global_version_configs", uniqueConstraints = {
    @UniqueConstraint(name = "uk_gvc_environment", columnNames = {"environment"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GlobalVersionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String environment;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String defaultApiVersion = "v1";

    @Column(nullable = false)
    @Builder.Default
    private int defaultSunsetDays = 90;

    @Column(length = 255)
    private String updatedBy;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
