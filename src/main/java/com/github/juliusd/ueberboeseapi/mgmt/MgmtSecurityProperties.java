package com.github.juliusd.ueberboeseapi.mgmt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Management API security.
 *
 * <p>These properties configure the Basic Auth credentials for /mgmt/** endpoints.
 *
 * <p>Example configuration in application.properties:
 *
 * <pre>
 * ueberboese.mgmt.username=admin
 * ueberboese.mgmt.password=changeme
 * </pre>
 *
 * <p>Example environment variable configuration:
 *
 * <pre>
 * UEBERBOESE_MGMT_USERNAME=admin
 * UEBERBOESE_MGMT_PASSWORD=secret123
 * </pre>
 */
@ConfigurationProperties(prefix = "ueberboese.mgmt")
public record MgmtSecurityProperties(
    /**
     * Username for Basic Auth on /mgmt/** endpoints.
     *
     * <p>Example: admin
     */
    String username,
    /**
     * Password for Basic Auth on /mgmt/** endpoints.
     *
     * <p>Example: changeme
     */
    String password) {}
