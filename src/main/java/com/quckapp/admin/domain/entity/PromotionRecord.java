package com.quckapp.admin.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "promotion_records", indexes = {
    @Index(name = "idx_pr_service", columnList = "serviceKey, apiVersion"),
    @Index(name = "idx_pr_environment", columnList = "toEnvironment"),
    @Index(name = "idx_pr_type", columnList = "promotionType"),
    @Index(name = "idx_pr_created", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PromotionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID versionConfigId;

    @Column(nullable = false, length = 50)
    private String serviceKey;

    @Column(nullable = false, length = 20)
    private String apiVersion;

    @Column(nullable = false, length = 20)
    private String fromEnvironment;

    @Column(nullable = false, length = 20)
    private String toEnvironment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PromotionType promotionType = PromotionType.NORMAL;

    @Column(nullable = false, length = 255)
    private String promotedBy;

    @Column(length = 255)
    private String approver1;

    @Column(length = 255)
    private String approver2;

    @Column(length = 50)
    private String jiraTicket;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
