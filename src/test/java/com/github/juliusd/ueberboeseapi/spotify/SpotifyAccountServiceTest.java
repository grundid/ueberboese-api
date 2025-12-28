package com.github.juliusd.ueberboeseapi.spotify;

import static org.assertj.core.api.Assertions.assertThat;
import static tools.jackson.databind.json.JsonMapper.*;

import com.github.juliusd.ueberboeseapi.DataDirectoryProperties;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccountService.SpotifyAccount;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class SpotifyAccountServiceTest {

  @TempDir Path tempDir;

  private SpotifyAccountService spotifyAccountService;

  @BeforeEach
  void setUp() {
    JsonMapper jsonMapper = builder().findAndAddModules().build();
    DataDirectoryProperties properties = new DataDirectoryProperties(tempDir.toString());
    spotifyAccountService = new SpotifyAccountService(jsonMapper, properties);
  }

  @AfterEach
  void tearDown() throws IOException {
    // Clean up any created files
    if (Files.exists(tempDir)) {
      Files.walk(tempDir)
          .sorted((a, b) -> -a.compareTo(b)) // Reverse order to delete files before directories
          .forEach(
              path -> {
                try {
                  if (!path.equals(tempDir)) { // Don't delete the temp dir itself, JUnit does it
                    Files.deleteIfExists(path);
                  }
                } catch (IOException e) {
                  // Ignore
                }
              });
    }
  }

  @Test
  void saveAccount_shouldCreateFileAndReturnAccountId() throws IOException {
    // Given
    String spotifyUserId = "spotify_user_123";
    String displayName = "Test User";
    String refreshToken = "refresh_token_xyz";

    // When
    String accountId = spotifyAccountService.saveAccount(spotifyUserId, displayName, refreshToken);

    // Then
    assertThat(accountId).isEqualTo(spotifyUserId);
    Path expectedFile = tempDir.resolve("spotify-account-" + spotifyUserId + ".json");
    assertThat(expectedFile).exists();
  }

  @Test
  void getAccountBySpotifyUserId_shouldReturnAccountWhenExists() throws IOException {
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
  void accountExists_shouldReturnTrueWhenAccountExists() throws IOException {
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
  void saveAccount_shouldOverwriteExistingAccount() throws IOException {
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
  void saveAccount_shouldCreateDirectoryIfNotExists() throws IOException {
    // Given
    Path nestedDir = tempDir.resolve("nested/dir");
    DataDirectoryProperties properties = new DataDirectoryProperties(nestedDir.toString());
    SpotifyAccountService service =
        new SpotifyAccountService(builder().findAndAddModules().build(), properties);

    String spotifyUserId = "spotify_user_nested";

    // When
    String accountId = service.saveAccount(spotifyUserId, "Nested User", "refresh");

    // Then
    assertThat(accountId).isEqualTo(spotifyUserId);
    assertThat(nestedDir).exists();
    Path expectedFile = nestedDir.resolve("spotify-account-" + spotifyUserId + ".json");
    assertThat(expectedFile).exists();
  }
}
