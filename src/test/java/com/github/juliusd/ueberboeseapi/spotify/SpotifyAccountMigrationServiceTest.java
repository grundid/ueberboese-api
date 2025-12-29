package com.github.juliusd.ueberboeseapi.spotify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tools.jackson.databind.json.JsonMapper.builder;

import com.github.juliusd.ueberboeseapi.DataDirectoryProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class SpotifyAccountMigrationServiceTest {

  @TempDir Path tempDir;

  @Mock SpotifyAccountRepository repository;

  @Captor ArgumentCaptor<SpotifyAccount> accountCaptor;

  private final JsonMapper jsonMapper = builder().findAndAddModules().build();

  @Test
  void run_shouldMigrateJsonFilesToDatabase() throws IOException {
    // Given
    // Create test JSON files
    createTestJsonFile("user1", "User One", "token1");
    createTestJsonFile("user2", "User Two", "token2");

    // Mock repository to indicate accounts don't exist yet
    when(repository.existsBySpotifyUserId("user1")).thenReturn(false);
    when(repository.existsBySpotifyUserId("user2")).thenReturn(false);

    DataDirectoryProperties properties = new DataDirectoryProperties(tempDir.toString());
    SpotifyAccountMigrationService migrationService =
        new SpotifyAccountMigrationService(repository, jsonMapper, properties);

    // When
    ApplicationArguments args = new DefaultApplicationArguments();
    migrationService.run(args);

    // Then
    verify(repository).existsBySpotifyUserId("user1");
    verify(repository).existsBySpotifyUserId("user2");
    verify(repository, times(2)).save(accountCaptor.capture());

    var savedAccounts = accountCaptor.getAllValues();
    assertThat(savedAccounts).hasSize(2);
    assertThat(savedAccounts)
        .extracting(SpotifyAccount::spotifyUserId)
        .containsExactlyInAnyOrder("user1", "user2");
    assertThat(savedAccounts)
        .extracting(SpotifyAccount::displayName)
        .containsExactlyInAnyOrder("User One", "User Two");
    assertThat(savedAccounts)
        .extracting(SpotifyAccount::refreshToken)
        .containsExactlyInAnyOrder("token1", "token2");
  }

  @Test
  void run_shouldSkipAlreadyMigratedAccounts() throws IOException {
    // Given
    createTestJsonFile("user1", "User One", "token1");

    // Mock repository to indicate user1 already exists
    when(repository.existsBySpotifyUserId("user1")).thenReturn(true);

    DataDirectoryProperties properties = new DataDirectoryProperties(tempDir.toString());
    SpotifyAccountMigrationService migrationService =
        new SpotifyAccountMigrationService(repository, jsonMapper, properties);

    // When
    ApplicationArguments args = new DefaultApplicationArguments();
    migrationService.run(args);

    // Then - should check if exists but not save
    verify(repository).existsBySpotifyUserId("user1");
    verify(repository, never()).save(any(SpotifyAccount.class));
  }

  @Test
  void run_shouldHandleMalformedJsonFiles() throws IOException {
    // Given
    createTestJsonFile("user1", "User One", "token1");
    // Create malformed JSON file
    Path malformedFile = tempDir.resolve("spotify-account-malformed.json");
    Files.writeString(malformedFile, "{invalid json");

    // Mock repository to indicate user1 doesn't exist
    when(repository.existsBySpotifyUserId("user1")).thenReturn(false);

    DataDirectoryProperties properties = new DataDirectoryProperties(tempDir.toString());
    SpotifyAccountMigrationService migrationService =
        new SpotifyAccountMigrationService(repository, jsonMapper, properties);

    // When
    ApplicationArguments args = new DefaultApplicationArguments();
    migrationService.run(args);

    // Then - should only migrate the valid file
    verify(repository).existsBySpotifyUserId("user1");
    verify(repository, times(1)).save(accountCaptor.capture());

    SpotifyAccount savedAccount = accountCaptor.getValue();
    assertThat(savedAccount.spotifyUserId()).isEqualTo("user1");
  }

  @Test
  void run_shouldHandleNonExistentDirectory() {
    // Given
    Path nonExistentDir = tempDir.resolve("nonexistent");
    DataDirectoryProperties properties = new DataDirectoryProperties(nonExistentDir.toString());
    SpotifyAccountMigrationService migrationService =
        new SpotifyAccountMigrationService(repository, jsonMapper, properties);

    // When
    ApplicationArguments args = new DefaultApplicationArguments();
    migrationService.run(args);

    // Then - should not throw exception, and should not interact with repository
    verify(repository, never()).existsBySpotifyUserId(any());
    verify(repository, never()).save(any(SpotifyAccount.class));
  }

  private void createTestJsonFile(String userId, String displayName, String token)
      throws IOException {
    String json =
        """
        {
          "spotifyUserId": "%s",
          "displayName": "%s",
          "refreshToken": "%s",
          "createdAt": "%s"
        }
        """
            .formatted(userId, displayName, token, OffsetDateTime.now().toString());

    Path file = tempDir.resolve("spotify-account-" + userId + ".json");
    Files.writeString(file, json);
  }
}
