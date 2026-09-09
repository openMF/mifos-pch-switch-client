/**
 * Copyright since 2026 Mifos Initiative
 *
 * <p>This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy
 * of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.apache.fineract.rafiki.config;

/**
 * Simple thread-local tenant context.
 * In a real Fineract deployment this would be replaced / bridged to
 * org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantIdentifier(String tenantIdentifier) {
        CURRENT_TENANT.set(tenantIdentifier);
    }

    public static String getTenantIdentifier() {
        String tenant = CURRENT_TENANT.get();
        if (tenant == null || tenant.isBlank()) {
            return "default";
        }
        return tenant;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
