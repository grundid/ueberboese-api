package com.github.juliusd.ueberboeseapi.spotify;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.TestBase;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SpotifyAccountServiceTest extends TestBase {

  @Autowired private SpotifyAccountService spotifyAccountService;

  @Autowired private SpotifyAccountRepository repository;

  @Test
  void saveAccount_shouldSaveToDatabaseAndReturnAccountId() {
    // Given
    String spotifyUserId = "spotify_user_123";
    String displayName = "Test User";
    String refreshToken = "refresh_token_xyz";

    // When
    String accountId = spotifyAccountService.saveAccount(spotifyUserId, displayName, refreshToken);

    // Then
    assertThat(accountId).isEqualTo(spotifyUserId);
    assertThat(repository.existsBySpotifyUserId(spotifyUserId)).isTrue();
  }

  @Test
  void getAccountBySpotifyUserId_shouldReturnAccountWhenExists() {
    // Given
    String spotifyUserId = "spotify_user_456";
    String displayName = "Another User";
    String refreshToken = "refresh_token_uvw";

    spotifyAccountService.saveAccount(spotifyUserId, displayName, refreshToken);

    // When
    Optional<SpotifyAccount> result =
        spotifyAccountService.getAccountBySpotifyUserId(spotifyUserId);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().spotifyUserId()).isEqualTo(spotifyUserId);
    assertThat(result.get().displayName()).isEqualTo(displayName);
    assertThat(result.get().refreshToken()).isEqualTo(refreshToken);
    assertThat(result.get().createdAt()).isNotNull();
  }

  @Test
  void getAccountBySpotifyUserId_shouldReturnEmptyWhenNotExists() {
    // Given
    String spotifyUserId = "non_existent_user";

    // When
    Optional<SpotifyAccount> result =
        spotifyAccountService.getAccountBySpotifyUserId(spotifyUserId);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void accountExists_shouldReturnTrueWhenAccountExists() {
    // Given
    String spotifyUserId = "spotify_user_789";
    spotifyAccountService.saveAccount(spotifyUserId, "User Name", "refresh");

    // When
    boolean exists = spotifyAccountService.accountExists(spotifyUserId);

    // Then
    assertThat(exists).isTrue();
  }

  @Test
  void accountExists_shouldReturnFalseWhenAccountDoesNotExist() {
    // Given
    String spotifyUserId = "non_existent_user_999";

    // When
    boolean exists = spotifyAccountService.accountExists(spotifyUserId);

    // Then
    assertThat(exists).isFalse();
  }

  @Test
  void saveAccount_shouldOverwriteExistingAccount() {
    // Given
    String spotifyUserId = "spotify_user_101";
    String firstDisplayName = "First Name";
    String secondDisplayName = "Second Name";
    String refreshToken = "refresh_token";

    // When
    spotifyAccountService.saveAccount(spotifyUserId, firstDisplayName, refreshToken);
    spotifyAccountService.saveAccount(spotifyUserId, secondDisplayName, refreshToken);

    // Then
    Optional<SpotifyAccount> result =
        spotifyAccountService.getAccountBySpotifyUserId(spotifyUserId);
    assertThat(result).isPresent();
    assertThat(result.get().displayName()).isEqualTo(secondDisplayName);
  }

  @Test
  void listAllAccounts_shouldReturnAllAccountsSortedByCreatedAtDesc() {
    // Given
    spotifyAccountService.saveAccount("user1", "User 1", "token1");
    spotifyAccountService.saveAccount("user2", "User 2", "token2");
    spotifyAccountService.saveAccount("user3", "User 3", "token3");

    // When
    List<SpotifyAccount> accounts = spotifyAccountService.listAllAccounts();

    // Then
    assertThat(accounts).hasSize(3);
    // Accounts should be sorted by createdAt descending (newest first)
    assertThat(accounts.get(0).spotifyUserId()).isEqualTo("user3");
    assertThat(accounts.get(1).spotifyUserId()).isEqualTo("user2");
    assertThat(accounts.get(2).spotifyUserId()).isEqualTo("user1");
  }
}
