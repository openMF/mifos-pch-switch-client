package org.apache.fineract.rafiki.repository;

import org.apache.fineract.rafiki.domain.RafikiTenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RafikiTenantConfigRepository extends JpaRepository<RafikiTenantConfig, Long> {

    Optional<RafikiTenantConfig> findByTenantIdentifier(String tenantIdentifier);
}
