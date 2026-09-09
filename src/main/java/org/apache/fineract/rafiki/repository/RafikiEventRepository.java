package org.apache.fineract.rafiki.repository;

import org.apache.fineract.rafiki.domain.RafikiEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RafikiEventRepository extends JpaRepository<RafikiEvent, Long> {

    boolean existsByTenantIdentifierAndEventId(String tenantIdentifier, String eventId);
}
