package com.quckapp.admin.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "version_profile_entries")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VersionProfileEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private VersionProfile profile;

    @Column(nullable = false, length = 50)
    private String serviceKey;

    @Column(nullable = false, length = 20)
    private String apiVersion;

    @Column(length = 50)
    private String releaseVersion;
}
