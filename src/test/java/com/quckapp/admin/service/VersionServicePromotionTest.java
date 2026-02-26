package com.quckapp.admin.service;

import com.quckapp.admin.domain.entity.*;
import com.quckapp.admin.domain.repository.*;
import com.quckapp.admin.dto.VersionDtos.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VersionServicePromotionTest {

    @Mock private VersionConfigRepository versionRepo;
    @Mock private GlobalVersionConfigRepository globalConfigRepo;
    @Mock private VersionProfileRepository profileRepo;
    @Mock private PromotionRecordRepository promotionRepo;

    @InjectMocks private VersionService service;

    private VersionConfig readyVersionInQa;
    private VersionConfig activeVersionInDev;

    @BeforeEach
    void setUp() {
        activeVersionInDev = VersionConfig.builder()
                .id(UUID.randomUUID())
                .environment("dev")
                .serviceKey("user-service")
                .apiVersion("v2")
                .status(VersionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        readyVersionInQa = VersionConfig.builder()
                .id(UUID.randomUUID())
                .environment("qa")
                .serviceKey("user-service")
                .apiVersion("v2")
                .status(VersionStatus.READY)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void activate_qa_succeeds_when_active_in_dev() {
        when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("qa", "user-service", "v2"))
                .thenReturn(Optional.of(readyVersionInQa));
        when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("dev", "user-service", "v2"))
                .thenReturn(Optional.of(activeVersionInDev));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(promotionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VersionConfigResponse result = service.activate("qa", "user-service", "v2", "admin");
        assertThat(result.status()).isEqualTo(VersionStatus.ACTIVE);
    }

    @Test
    void activate_qa_fails_when_not_active_in_dev() {
        VersionConfig notActiveInDev = VersionConfig.builder()
                .environment("dev").serviceKey("user-service").apiVersion("v2")
                .status(VersionStatus.READY).build();

        when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("qa", "user-service", "v2"))
                .thenReturn(Optional.of(readyVersionInQa));
        when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("dev", "user-service", "v2"))
                .thenReturn(Optional.of(notActiveInDev));

        assertThatThrownBy(() -> service.activate("qa", "user-service", "v2", "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be ACTIVE in dev");
    }

    @Test
    void activate_qa_fails_when_version_missing_in_dev() {
        when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("qa", "user-service", "v2"))
                .thenReturn(Optional.of(readyVersionInQa));
        when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("dev", "user-service", "v2"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activate("qa", "user-service", "v2", "admin"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be ACTIVE in dev");
    }

    @Test
    void activate_local_succeeds_without_chain_check() {
        VersionConfig localReady = VersionConfig.builder()
                .id(UUID.randomUUID())
                .environment("local").serviceKey("user-service").apiVersion("v2")
                .status(VersionStatus.READY)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("local", "user-service", "v2"))
                .thenReturn(Optional.of(localReady));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(promotionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VersionConfigResponse result = service.activate("local", "user-service", "v2", "admin");
        assertThat(result.status()).isEqualTo(VersionStatus.ACTIVE);
    }

    @Test
    void canPromote_returns_allowed_when_active_in_previous() {
        when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("dev", "user-service", "v2"))
                .thenReturn(Optional.of(activeVersionInDev));

        CanPromoteResponse result = service.canPromote("user-service", "v2", "qa");
        assertThat(result.allowed()).isTrue();
    }

    @Test
    void canPromote_returns_blocked_when_not_active() {
        when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("dev", "user-service", "v2"))
                .thenReturn(Optional.empty());

        CanPromoteResponse result = service.canPromote("user-service", "v2", "qa");
        assertThat(result.allowed()).isFalse();
        assertThat(result.blockedReason()).contains("dev");
    }

    @Test
    void emergencyActivate_succeeds_with_all_fields() {
        VersionConfig prodReady = VersionConfig.builder()
                .id(UUID.randomUUID())
                .environment("production").serviceKey("user-service").apiVersion("v2")
                .status(VersionStatus.READY)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();

        when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("production", "user-service", "v2"))
                .thenReturn(Optional.of(prodReady));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(promotionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmergencyActivateRequest req = new EmergencyActivateRequest(
                "user-service", "v2", "Critical security patch",
                "alice@quckapp.com", "bob@quckapp.com", "SEC-1234");

        VersionConfigResponse result = service.emergencyActivate("production", req, "charlie@quckapp.com");
        assertThat(result.status()).isEqualTo(VersionStatus.ACTIVE);
    }

    @Test
    void emergencyActivate_fails_when_promoter_is_approver() {
        EmergencyActivateRequest req = new EmergencyActivateRequest(
                "user-service", "v2", "Critical security patch",
                "charlie@quckapp.com", "bob@quckapp.com", "SEC-1234");

        assertThatThrownBy(() -> service.emergencyActivate("production", req, "charlie@quckapp.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("approvers must differ from promoter");
    }

    @Test
    void emergencyActivate_fails_when_approvers_same() {
        EmergencyActivateRequest req = new EmergencyActivateRequest(
                "user-service", "v2", "Critical security patch",
                "alice@quckapp.com", "alice@quckapp.com", "SEC-1234");

        assertThatThrownBy(() -> service.emergencyActivate("production", req, "charlie@quckapp.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("approver1 and approver2 must be different");
    }
}
