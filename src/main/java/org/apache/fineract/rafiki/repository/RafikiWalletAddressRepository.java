package org.apache.fineract.rafiki.repository;

import org.apache.fineract.rafiki.domain.RafikiWalletAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RafikiWalletAddressRepository extends JpaRepository<RafikiWalletAddress, Long> {

    Optional<RafikiWalletAddress> findByTenantIdentifierAndRafikiWalletAddressId(
            String tenantIdentifier, String rafikiWalletAddressId);

    Optional<RafikiWalletAddress> findByTenantIdentifierAndWalletAddress(
            String tenantIdentifier, String walletAddress);

    List<RafikiWalletAddress> findByTenantIdentifierAndClientId(String tenantIdentifier, Long clientId);
}
