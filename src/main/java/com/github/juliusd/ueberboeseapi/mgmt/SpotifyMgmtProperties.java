package com.github.juliusd.ueberboeseapi.mgmt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Spotify Management API.
 *
 * <p>These properties configure the OAuth redirect URI for Spotify authentication flow.
 *
 * <p>Example configuration in application.properties:
 *
 * <pre>
 * spotify.mgmt.redirect-uri=ueberboese-login://spotify
 * </pre>
 *
 * <p>Example environment variable configuration:
 *
 * <pre>
 * SPOTIFY_MGMT_REDIRECT_URI=ueberboese-login://spotify
 * </pre>
 */
@ConfigurationProperties(prefix = "spotify.mgmt")
public record SpotifyMgmtProperties(
    /**
     * Redirect URI for Spotify OAuth callback. Uses custom URI scheme for mobile/desktop app
     * integration. This is where Spotify will redirect the user after authentication.
     *
     * <p>Example: ueberboese-login://spotify
     */
    String redirectUri) {}
