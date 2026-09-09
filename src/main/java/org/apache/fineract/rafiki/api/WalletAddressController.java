package org.apache.fineract.rafiki.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.rafiki.config.TenantContext;
import org.apache.fineract.rafiki.data.CreateWalletAddressRequest;
import org.apache.fineract.rafiki.data.WalletAddressResponse;
import org.apache.fineract.rafiki.service.WalletAddressMappingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/rafiki/wallet-addresses")
@RequiredArgsConstructor
public class WalletAddressController {

    private final WalletAddressMappingService mappingService;

    @PostMapping
    public ResponseEntity<WalletAddressResponse> create(
            @RequestHeader(value = "X-Tenant-Identifier", required = false) String tenantHeader,
            @Valid @RequestBody CreateWalletAddressRequest request) {

        String tenant = (tenantHeader != null && !tenantHeader.isBlank()) ? tenantHeader : "default";
        TenantContext.setTenantIdentifier(tenant);
        try {
            WalletAddressResponse response = mappingService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } finally {
            TenantContext.clear();
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<WalletAddressResponse>> listByClient(
            @RequestHeader(value = "X-Tenant-Identifier", required = false) String tenantHeader,
            @PathVariable Long clientId) {

        String tenant = (tenantHeader != null && !tenantHeader.isBlank()) ? tenantHeader : "default";
        TenantContext.setTenantIdentifier(tenant);
        try {
            return ResponseEntity.ok(mappingService.findByClientId(clientId));
        } finally {
            TenantContext.clear();
        }
    }
}
