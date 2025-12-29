package com.github.juliusd.ueberboeseapi.spotify;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpotifyAccountService {

  private final SpotifyAccountRepository repository;

  /**
   * Saves a Spotify account after successful OAuth authentication.
   *
   * <p>Note: This method performs an upsert (insert or update). If an account with the same
   * spotifyUserId already exists, it will be updated with the new values.
   *
   * @param spotifyUserId The Spotify user ID
   * @param displayName The user's display name from Spotify
   * @param refreshToken The refresh token
   * @return The accountId (same as spotifyUserId for simplicity)
   */
  public String saveAccount(String spotifyUserId, String displayName, String refreshToken) {
    log.debug("Attempting to save Spotify account for userId: {}", spotifyUserId);

    OffsetDateTime now = OffsetDateTime.now();

    // Check if account exists to preserve createdAt and version
    Optional<SpotifyAccount> existing = repository.findById(spotifyUserId);
    OffsetDateTime createdAt = existing.map(SpotifyAccount::createdAt).orElse(now);
    Long version = existing.map(SpotifyAccount::version).orElse(null);

    SpotifyAccount account =
        new SpotifyAccount(spotifyUserId, displayName, refreshToken, createdAt, now, version);

    repository.save(account);
    log.info("Successfully saved Spotify account for accountId: {}", spotifyUserId);
    return spotifyUserId;
  }

  /**
   * Retrieves a Spotify account by Spotify user ID.
   *
   * @param spotifyUserId The Spotify user ID
   * @return Optional containing the account if found
   */
  public Optional<SpotifyAccount> getAccountBySpotifyUserId(String spotifyUserId) {
    log.debug("Attempting to load Spotify account for userId: {}", spotifyUserId);

    Optional<SpotifyAccount> account = repository.findById(spotifyUserId);

    if (account.isPresent()) {
      log.info("Successfully loaded Spotify account for accountId: {}", spotifyUserId);
    } else {
      log.debug("Spotify account not found for userId: {}", spotifyUserId);
    }

    return account;
  }

  /**
   * Checks if a Spotify account exists for the given user ID.
   *
   * @param spotifyUserId The Spotify user ID
   * @return true if the account exists, false otherwise
   */
  public boolean accountExists(String spotifyUserId) {
    return repository.existsBySpotifyUserId(spotifyUserId);
  }

  /**
   * Lists all stored Spotify accounts.
   *
   * @return List of all Spotify accounts, sorted by createdAt descending (newest first)
   */
  public List<SpotifyAccount> listAllAccounts() {
    log.debug("Listing all Spotify accounts from database");

    List<SpotifyAccount> accounts = repository.findAllByOrderByCreatedAtDesc();

    log.info("Found {} Spotify account(s)", accounts.size());
    return accounts;
  }
}
