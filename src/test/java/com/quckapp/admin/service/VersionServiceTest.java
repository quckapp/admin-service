package com.quckapp.admin.service;

import com.quckapp.admin.domain.entity.VersionConfig;
import com.quckapp.admin.domain.entity.VersionStatus;
import com.quckapp.admin.domain.repository.GlobalVersionConfigRepository;
import com.quckapp.admin.domain.repository.VersionConfigRepository;
import com.quckapp.admin.domain.repository.VersionProfileRepository;
import com.quckapp.admin.dto.VersionDtos.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    @Mock
    private VersionConfigRepository versionRepo;

    @Mock
    private GlobalVersionConfigRepository globalConfigRepo;

    @Mock
    private VersionProfileRepository profileRepo;

    @InjectMocks
    private VersionService versionService;

    private VersionConfig readyVersion(String env, String serviceKey, String apiVersion) {
        return VersionConfig.builder()
                .id(UUID.randomUUID())
                .environment(env)
                .serviceKey(serviceKey)
                .apiVersion(apiVersion)
                .status(VersionStatus.READY)
                .build();
    }

    private VersionConfig activeVersion(String env, String serviceKey, String apiVersion) {
        return VersionConfig.builder()
                .id(UUID.randomUUID())
                .environment(env)
                .serviceKey(serviceKey)
                .apiVersion(apiVersion)
                .status(VersionStatus.ACTIVE)
                .build();
    }

    // ===== Chain Validation Tests =====

    @Nested
    class ChainValidation {

        @Test
        void activate_unrestrictedEnvironment_passesWithoutChainCheck() {
            VersionConfig config = readyVersion("dev", "user-service", "v2");
            when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("dev", "user-service", "v2"))
                    .thenReturn(Optional.of(config));
            when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            VersionConfigResponse result = versionService.activate("dev", "user-service", "v2", "admin");

            assertEquals(VersionStatus.ACTIVE, result.status());
            // Should NOT call existsBy... for chain check on unrestricted envs
            verify(versionRepo, never()).existsByEnvironmentAndServiceKeyAndApiVersionAndStatus(
                    anyString(), anyString(), anyString(), any());
        }

        @Test
        void activate_restrictedEnvironment_requiresActiveInPrevious() {
            VersionConfig config = readyVersion("staging", "user-service", "v2");
            when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("staging", "user-service", "v2"))
                    .thenReturn(Optional.of(config));
            // Version IS active in uat (previous of staging)
            when(versionRepo.existsByEnvironmentInAndServiceKeyAndApiVersionAndStatus(
                    anyList(), eq("user-service"), eq("v2"), eq(VersionStatus.ACTIVE)))
                    .thenReturn(true);
            when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            VersionConfigResponse result = versionService.activate("staging", "user-service", "v2", "admin");

            assertEquals(VersionStatus.ACTIVE, result.status());
        }

        @Test
        void activate_uatVariantHandling_checksAllVariants() {
            // Activating in staging should check all UAT variants
            VersionConfig config = readyVersion("staging", "user-service", "v2");
            when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("staging", "user-service", "v2"))
                    .thenReturn(Optional.of(config));
            when(versionRepo.existsByEnvironmentInAndServiceKeyAndApiVersionAndStatus(
                    eq(List.of("uat", "uat1", "uat2", "uat3")), eq("user-service"), eq("v2"), eq(VersionStatus.ACTIVE)))
                    .thenReturn(true);
            when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            versionService.activate("staging", "user-service", "v2", "admin");

            // Verify it used the multi-env query with UAT variants
            verify(versionRepo).existsByEnvironmentInAndServiceKeyAndApiVersionAndStatus(
                    eq(List.of("uat", "uat1", "uat2", "uat3")), eq("user-service"), eq("v2"), eq(VersionStatus.ACTIVE));
        }

        @Test
        void activate_chainViolation_throws() {
            VersionConfig config = readyVersion("staging", "user-service", "v2");
            when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("staging", "user-service", "v2"))
                    .thenReturn(Optional.of(config));
            // Version NOT active in any UAT variant
            when(versionRepo.existsByEnvironmentInAndServiceKeyAndApiVersionAndStatus(
                    anyList(), eq("user-service"), eq("v2"), eq(VersionStatus.ACTIVE)))
                    .thenReturn(false);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> versionService.activate("staging", "user-service", "v2", "admin"));

            assertTrue(ex.getMessage().contains("must be ACTIVE in uat first"));
        }
    }

    // ===== canPromote Tests =====

    @Nested
    class CanPromote {

        @Test
        void canPromote_allowed_whenActiveInCurrentEnv() {
            when(versionRepo.existsByEnvironmentAndServiceKeyAndApiVersionAndStatus(
                    "staging", "user-service", "v2", VersionStatus.ACTIVE)).thenReturn(true);
            when(versionRepo.existsByEnvironmentAndServiceKeyAndApiVersionAndStatus(
                    "production", "user-service", "v2", VersionStatus.ACTIVE)).thenReturn(false);

            CanPromoteResponse result = versionService.canPromote("staging", "user-service", "v2");

            assertTrue(result.allowed());
            assertEquals("production", result.nextEnvironment());
            assertNull(result.blockedReason());
        }

        @Test
        void canPromote_blocked_whenNotActiveInCurrentEnv() {
            when(versionRepo.existsByEnvironmentAndServiceKeyAndApiVersionAndStatus(
                    "staging", "user-service", "v2", VersionStatus.ACTIVE)).thenReturn(false);

            CanPromoteResponse result = versionService.canPromote("staging", "user-service", "v2");

            assertFalse(result.allowed());
            assertTrue(result.blockedReason().contains("not ACTIVE"));
        }

        @Test
        void canPromote_blocked_whenLastInChain() {
            when(versionRepo.existsByEnvironmentAndServiceKeyAndApiVersionAndStatus(
                    "live", "user-service", "v2", VersionStatus.ACTIVE)).thenReturn(true);

            CanPromoteResponse result = versionService.canPromote("live", "user-service", "v2");

            assertFalse(result.allowed());
            assertTrue(result.blockedReason().contains("last environment"));
        }

        @Test
        void canPromote_blocked_whenAlreadyActiveInNext() {
            when(versionRepo.existsByEnvironmentAndServiceKeyAndApiVersionAndStatus(
                    "staging", "user-service", "v2", VersionStatus.ACTIVE)).thenReturn(true);
            when(versionRepo.existsByEnvironmentAndServiceKeyAndApiVersionAndStatus(
                    "production", "user-service", "v2", VersionStatus.ACTIVE)).thenReturn(true);

            CanPromoteResponse result = versionService.canPromote("staging", "user-service", "v2");

            assertFalse(result.allowed());
            assertTrue(result.blockedReason().contains("already ACTIVE"));
        }
    }

    // ===== promote Tests =====

    @Nested
    class Promote {

        @Test
        void promote_createsVersionInNextEnv() {
            // ACTIVE in staging
            when(versionRepo.existsByEnvironmentAndServiceKeyAndApiVersionAndStatus(
                    "staging", "user-service", "v2", VersionStatus.ACTIVE)).thenReturn(true);
            // Not active in production
            when(versionRepo.existsByEnvironmentAndServiceKeyAndApiVersionAndStatus(
                    "production", "user-service", "v2", VersionStatus.ACTIVE)).thenReturn(false);
            // No existing version in production
            when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("production", "user-service", "v2"))
                    .thenReturn(Optional.empty());
            when(versionRepo.save(any())).thenAnswer(inv -> {
                VersionConfig config = inv.getArgument(0);
                config.setId(UUID.randomUUID());
                return config;
            });

            PromotionResponse result = versionService.promote(
                    "staging", "user-service", "v2", new PromoteRequest("rolling out"), "deployer");

            assertEquals("staging", result.fromEnvironment());
            assertEquals("production", result.toEnvironment());
            assertEquals("PROMOTE", result.promotionType());
            assertEquals("deployer", result.promotedBy());
        }

        @Test
        void promote_activatesExistingReadyVersion() {
            when(versionRepo.existsByEnvironmentAndServiceKeyAndApiVersionAndStatus(
                    "staging", "user-service", "v2", VersionStatus.ACTIVE)).thenReturn(true);
            when(versionRepo.existsByEnvironmentAndServiceKeyAndApiVersionAndStatus(
                    "production", "user-service", "v2", VersionStatus.ACTIVE)).thenReturn(false);

            VersionConfig readyInProd = readyVersion("production", "user-service", "v2");
            when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("production", "user-service", "v2"))
                    .thenReturn(Optional.of(readyInProd));
            when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PromotionResponse result = versionService.promote(
                    "staging", "user-service", "v2", null, "deployer");

            assertEquals("production", result.toEnvironment());
            // Verify the version was set to ACTIVE
            verify(versionRepo).save(argThat(config ->
                    config.getStatus() == VersionStatus.ACTIVE && "production".equals(config.getEnvironment())));
        }

        @Test
        void promote_failsWhenNotAllowed() {
            // Not active in staging
            when(versionRepo.existsByEnvironmentAndServiceKeyAndApiVersionAndStatus(
                    "staging", "user-service", "v2", VersionStatus.ACTIVE)).thenReturn(false);

            assertThrows(IllegalStateException.class,
                    () -> versionService.promote("staging", "user-service", "v2", null, "deployer"));
        }
    }

    // ===== emergencyActivate Tests =====

    @Nested
    class EmergencyActivate {

        @Test
        void emergencyActivate_bypassesChain() {
            // No version exists yet
            when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("production", "user-service", "v2"))
                    .thenReturn(Optional.empty());
            when(versionRepo.save(any())).thenAnswer(inv -> {
                VersionConfig config = inv.getArgument(0);
                config.setId(UUID.randomUUID());
                return config;
            });

            EmergencyActivateRequest request = new EmergencyActivateRequest(
                    "critical hotfix", "manager1", "manager2", "JIRA-123");

            PromotionResponse result = versionService.emergencyActivate(
                    "production", "user-service", "v2", request, "deployer");

            assertEquals("EMERGENCY", result.promotionType());
            assertEquals("production", result.toEnvironment());
            // Verify no chain validation was called
            verify(versionRepo, never()).existsByEnvironmentInAndServiceKeyAndApiVersionAndStatus(
                    anyList(), anyString(), anyString(), any());
        }

        @Test
        void emergencyActivate_rejectsSameApprovers() {
            EmergencyActivateRequest request = new EmergencyActivateRequest(
                    "critical hotfix", "manager1", "manager1", "JIRA-123");

            assertThrows(IllegalArgumentException.class,
                    () -> versionService.emergencyActivate(
                            "production", "user-service", "v2", request, "deployer"));
        }

        @Test
        void emergencyActivate_rejectsPromoterAsApprover() {
            EmergencyActivateRequest request = new EmergencyActivateRequest(
                    "critical hotfix", "deployer", "manager2", "JIRA-123");

            assertThrows(IllegalArgumentException.class,
                    () -> versionService.emergencyActivate(
                            "production", "user-service", "v2", request, "deployer"));
        }

        @Test
        void emergencyActivate_activatesExistingVersion() {
            VersionConfig existing = readyVersion("production", "user-service", "v2");
            when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("production", "user-service", "v2"))
                    .thenReturn(Optional.of(existing));
            when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            EmergencyActivateRequest request = new EmergencyActivateRequest(
                    "critical hotfix", "manager1", "manager2", "JIRA-456");

            PromotionResponse result = versionService.emergencyActivate(
                    "production", "user-service", "v2", request, "deployer");

            assertEquals("EMERGENCY", result.promotionType());
            verify(versionRepo).save(argThat(config ->
                    config.getStatus() == VersionStatus.ACTIVE
                            && config.getChangelog().startsWith("EMERGENCY:")));
        }

        @Test
        void emergencyActivate_setsEmergencyChangelog() {
            when(versionRepo.findByEnvironmentAndServiceKeyAndApiVersion("production", "user-service", "v2"))
                    .thenReturn(Optional.empty());
            when(versionRepo.save(any())).thenAnswer(inv -> {
                VersionConfig config = inv.getArgument(0);
                config.setId(UUID.randomUUID());
                return config;
            });

            EmergencyActivateRequest request = new EmergencyActivateRequest(
                    "critical hotfix", "manager1", "manager2", "JIRA-789");

            versionService.emergencyActivate("production", "user-service", "v2", request, "deployer");

            verify(versionRepo).save(argThat(config ->
                    config.getChangelog().contains("EMERGENCY:")
                            && config.getChangelog().contains("JIRA-789")
                            && config.getChangelog().contains("manager1")
                            && config.getChangelog().contains("manager2")));
        }
    }
}
