package com.devpulse.repository;

import com.devpulse.model.EventLevel;
import com.devpulse.model.IncidentStatus;
import com.devpulse.model.Role;
import com.devpulse.model.Severity;
import com.devpulse.model.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class DataJpaRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private IngestionKeyRepository ingestionKeyRepository;

    @Autowired
    private EventRecordRepository eventRecordRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @Test
    @DisplayName("Enforce unique constraint on (service_id, event_id) - duplicate ingestion fails")
    void testUniqueServiceEventConstraint() {
        UUID orgId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        EventRecordEntity event1 = new EventRecordEntity(
                orgId, serviceId, eventId, Instant.now(),
                EventLevel.ERROR, "Database connection timeout", "SQLTimeoutException", "fp_abc123"
        );
        eventRecordRepository.saveAndFlush(event1);

        EventRecordEntity event2 = new EventRecordEntity(
                orgId, serviceId, eventId, Instant.now(),
                EventLevel.ERROR, "Duplicate submission of same event", "SQLTimeoutException", "fp_abc123"
        );

        assertThatThrownBy(() -> eventRecordRepository.saveAndFlush(event2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Enforce strict multi-tenant filtering on projects across organizations")
    void testMultiTenantIsolation() {
        UserEntity ownerA = userRepository.saveAndFlush(new UserEntity("ownerA@test.com", "hash", "Owner A"));
        OrganizationEntity orgA = organizationRepository.saveAndFlush(new OrganizationEntity("tenant-a-" + UUID.randomUUID(), "Tenant A", ownerA.getId()));

        UserEntity ownerB = userRepository.saveAndFlush(new UserEntity("ownerB@test.com", "hash", "Owner B"));
        OrganizationEntity orgB = organizationRepository.saveAndFlush(new OrganizationEntity("tenant-b-" + UUID.randomUUID(), "Tenant B", ownerB.getId()));

        ProjectEntity projA = projectRepository.saveAndFlush(new ProjectEntity(orgA.getId(), "Project Alpha", "Description"));
        ProjectEntity projB = projectRepository.saveAndFlush(new ProjectEntity(orgB.getId(), "Project Beta", "Description"));

        List<ProjectEntity> orgAProjects = projectRepository.findByOrganizationId(orgA.getId());
        assertThat(orgAProjects).extracting(ProjectEntity::getId).contains(projA.getId()).doesNotContain(projB.getId());

        List<ProjectEntity> orgBProjects = projectRepository.findByOrganizationId(orgB.getId());
        assertThat(orgBProjects).extracting(ProjectEntity::getId).contains(projB.getId()).doesNotContain(projA.getId());
    }

    @Test
    @DisplayName("Evaluate '20 events in 60 seconds' sliding window query")
    void testSlidingWindowQuery() {
        UUID orgId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        String testFingerprint = "fp_sliding_window_" + UUID.randomUUID();

        Instant now = Instant.now();
        Instant windowStart = now.minus(60, ChronoUnit.SECONDS);

        // Insert 20 events inside the 60-second window (between 1s and 39s ago)
        for (int i = 0; i < 20; i++) {
            eventRecordRepository.save(new EventRecordEntity(
                    orgId, serviceId, UUID.randomUUID(),
                    now.minus(1 + (i * 2), ChronoUnit.SECONDS),
                    EventLevel.ERROR, "Burst error " + i, "TimeoutException", testFingerprint
            ));
        }

        // Insert 5 older events outside the window (e.g. 2 minutes ago)
        for (int i = 0; i < 5; i++) {
            eventRecordRepository.save(new EventRecordEntity(
                    orgId, serviceId, UUID.randomUUID(),
                    now.minus(120 + i, ChronoUnit.SECONDS),
                    EventLevel.ERROR, "Old error " + i, "TimeoutException", testFingerprint
            ));
        }
        eventRecordRepository.flush();

        long countInWindow = eventRecordRepository.countMatchingEventsInWindow(serviceId, testFingerprint, windowStart, now);
        assertThat(countInWindow).isEqualTo(20);
    }

    @Test
    @DisplayName("Verify active incident lookup matching fingerprint and open status")
    void testActiveIncidentLookup() {
        UUID orgId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        String fingerprint = "fp_active_test_" + UUID.randomUUID();

        IncidentEntity incident = new IncidentEntity(
                orgId, serviceId, "production", fingerprint,
                "Database connection pool exhausted", Severity.CRITICAL, Instant.now()
        );
        incidentRepository.saveAndFlush(incident);

        Optional<IncidentEntity> found = incidentRepository.findActiveIncident(serviceId, "production", fingerprint);
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(IncidentStatus.OPEN);

        // Resolve incident and verify it is no longer returned as active
        found.get().setStatus(IncidentStatus.RESOLVED);
        found.get().setResolvedAt(Instant.now());
        incidentRepository.saveAndFlush(found.get());

        Optional<IncidentEntity> afterResolved = incidentRepository.findActiveIncident(serviceId, "production", fingerprint);
        assertThat(afterResolved).isEmpty();
    }

    @Test
    @DisplayName("Verify active ingestion key lookup by key hash")
    void testIngestionKeyLookup() {
        UUID orgId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        String hash = "hash_" + UUID.randomUUID();

        IngestionKeyEntity key = new IngestionKeyEntity(orgId, serviceId, "dp_live", hash, "Primary Ingestion Key");
        ingestionKeyRepository.saveAndFlush(key);

        Optional<IngestionKeyEntity> activeKey = ingestionKeyRepository.findByKeyHashAndRevokedAtIsNull(hash);
        assertThat(activeKey).isPresent();
        assertThat(activeKey.get().getName()).isEqualTo("Primary Ingestion Key");

        // Revoke key
        activeKey.get().setRevokedAt(Instant.now());
        ingestionKeyRepository.saveAndFlush(activeKey.get());

        Optional<IngestionKeyEntity> revokedKey = ingestionKeyRepository.findByKeyHashAndRevokedAtIsNull(hash);
        assertThat(revokedKey).isEmpty();
    }

    @Test
    @DisplayName("Verify membership role assignment and retrieval")
    void testMembershipRoleRetrieval() {
        UserEntity user = userRepository.saveAndFlush(new UserEntity("dev@example.com", "hash", "Dev User"));
        OrganizationEntity org = organizationRepository.saveAndFlush(new OrganizationEntity("org-slug-" + UUID.randomUUID(), "Dev Org", user.getId()));

        MembershipEntity membership = membershipRepository.saveAndFlush(new MembershipEntity(org.getId(), user.getId(), Role.ENGINEER));

        Optional<MembershipEntity> retrieved = membershipRepository.findByOrganizationIdAndUserId(org.getId(), user.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getRole()).isEqualTo(Role.ENGINEER);
    }
}
