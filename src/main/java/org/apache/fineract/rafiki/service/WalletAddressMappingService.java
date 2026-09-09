package org.apache.fineract.rafiki.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.rafiki.config.TenantContext;
import org.apache.fineract.rafiki.data.CreateWalletAddressRequest;
import org.apache.fineract.rafiki.data.WalletAddressResponse;
import org.apache.fineract.rafiki.domain.RafikiWalletAddress;
import org.apache.fineract.rafiki.exception.RafikiConnectorException;
import org.apache.fineract.rafiki.graphql.RafikiGraphQLClient;
import org.apache.fineract.rafiki.repository.RafikiWalletAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletAddressMappingService {

    private final RafikiWalletAddressRepository repository;
    private final RafikiGraphQLClient graphQLClient;

    @Transactional
    public WalletAddressResponse create(CreateWalletAddressRequest request) {
        String tenant = TenantContext.getTenantIdentifier();

        if (repository.findByTenantIdentifierAndWalletAddress(tenant, request.getWalletAddress()).isPresent()) {
            throw new RafikiConnectorException("Wallet address already mapped for this tenant");
        }

        // In a real deployment this would call Rafiki Admin API to create the wallet address
        String rafikiId = graphQLClient.createWalletAddress(request.getWalletAddress(),
                request.getAssetCode(), request.getAssetScale());

        RafikiWalletAddress entity = RafikiWalletAddress.builder()
                .tenantIdentifier(tenant)
                .clientId(request.getClientId())
                .savingsAccountId(request.getSavingsAccountId())
                .walletAddress(request.getWalletAddress())
                .rafikiWalletAddressId(rafikiId)
                .assetCode(request.getAssetCode())
                .assetScale(request.getAssetScale())
                .status("ACTIVE")
                .build();

        entity = repository.save(entity);
        log.info("Created wallet address mapping {} -> {} for tenant {}", 
                entity.getWalletAddress(), entity.getRafikiWalletAddressId(), tenant);

        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Optional<RafikiWalletAddress> findByRafikiWalletAddressId(String rafikiWalletAddressId) {
        String tenant = TenantContext.getTenantIdentifier();
        return repository.findByTenantIdentifierAndRafikiWalletAddressId(tenant, rafikiWalletAddressId);
    }

    @Transactional(readOnly = true)
    public List<WalletAddressResponse> findByClientId(Long clientId) {
        String tenant = TenantContext.getTenantIdentifier();
        return repository.findByTenantIdentifierAndClientId(tenant, clientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private WalletAddressResponse toResponse(RafikiWalletAddress e) {
        return WalletAddressResponse.builder()
                .id(e.getId())
                .clientId(e.getClientId())
                .savingsAccountId(e.getSavingsAccountId())
                .walletAddress(e.getWalletAddress())
                .rafikiWalletAddressId(e.getRafikiWalletAddressId())
                .assetCode(e.getAssetCode())
                .assetScale(e.getAssetScale())
                .status(e.getStatus())
                .build();
    }
}
