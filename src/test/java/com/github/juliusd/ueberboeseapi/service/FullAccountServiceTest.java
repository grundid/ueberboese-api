package com.github.juliusd.ueberboeseapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.juliusd.ueberboeseapi.ProxyService;
import com.github.juliusd.ueberboeseapi.XmlMessageConverterConfig;
import com.github.juliusd.ueberboeseapi.generated.dtos.CredentialApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourceApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.SourcesContainerApiDto;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccount;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccountService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class FullAccountServiceTest {

  private FullAccountService fullAccountService;
  private AccountDataService accountDataService;
  private ProxyService proxyService;
  private XmlMapper xmlMapper;
  private SpotifyAccountService spotifyAccountService;
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    accountDataService = mock(AccountDataService.class);
    proxyService = mock(ProxyService.class);
    spotifyAccountService = mock(SpotifyAccountService.class);
    XmlMessageConverterConfig config = new XmlMessageConverterConfig();
    xmlMapper = config.customXmlMapper();
    request = mock(HttpServletRequest.class);

    fullAccountService =
        new FullAccountService(accountDataService, proxyService, xmlMapper, spotifyAccountService);
  }

  @Test
  void testGetFullAccount_CacheHit_ReturnsData() throws IOException {
    // Given
    String accountId = "test-account-123";
    FullAccountResponseApiDto expectedData = new FullAccountResponseApiDto();
    expectedData.setId(accountId);

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(expectedData);

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(accountId);

    // Verify proxy was NOT called
    verify(proxyService, never()).forwardRequest(any(), any());
  }

  @Test
  void testGetFullAccount_CacheHit_LoadFails_ReturnsEmpty() throws IOException {
    // Given
    String accountId = "test-account-456";

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId))
        .thenThrow(new IOException("Cache read error"));

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isEmpty();

    // Verify proxy was NOT called even though cache load failed
    verify(proxyService, never()).forwardRequest(any(), any());
  }

  @Test
  void testGetFullAccount_CacheMiss_ProxySuccess_ReturnsData() throws Exception {
    // Given
    String accountId = "test-account-789";
    String xmlContent =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <account id="test-account-789">
          <accountStatus>ACTIVE</accountStatus>
          <mode>global</mode>
        </account>
        """;

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);
    when(proxyService.forwardRequest(eq(request), any()))
        .thenReturn(ResponseEntity.ok(xmlContent.getBytes()));

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(accountId);

    // Verify data was cached
    verify(accountDataService).saveFullAccountDataRaw(eq(accountId), anyString());
  }

  @Test
  void testGetFullAccount_CacheMiss_ProxyFailure_ReturnsEmpty() throws IOException {
    // Given
    String accountId = "test-account-error";

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);
    when(proxyService.forwardRequest(eq(request), any()))
        .thenReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isEmpty();

    // Verify no attempt to cache was made
    verify(accountDataService, never()).saveFullAccountDataRaw(anyString(), anyString());
  }

  @Test
  void testGetFullAccount_CacheMiss_ProxyReturnsNullBody_ReturnsEmpty() throws IOException {
    // Given
    String accountId = "test-account-null";

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);
    when(proxyService.forwardRequest(eq(request), any()))
        .thenReturn(ResponseEntity.ok().build()); // No body

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isEmpty();

    // Verify no attempt to cache was made
    verify(accountDataService, never()).saveFullAccountDataRaw(anyString(), anyString());
  }

  @Test
  void testGetFullAccount_CacheMiss_ParseError_ReturnsEmpty() throws IOException {
    // Given
    String accountId = "test-account-bad-xml";
    String invalidXml = "<invalid>not complete";

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);
    when(proxyService.forwardRequest(eq(request), any()))
        .thenReturn(ResponseEntity.ok(invalidXml.getBytes()));

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isEmpty();

    // Verify no attempt to cache was made since parsing failed
    verify(accountDataService, never()).saveFullAccountDataRaw(anyString(), anyString());
  }

  @Test
  void testGetFullAccount_CacheSaveFails_StillReturnsData() throws Exception {
    // Given
    String accountId = "test-account-cache-fail";
    String xmlContent =
        """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <account id="test-account-cache-fail">
          <accountStatus>ACTIVE</accountStatus>
          <mode>global</mode>
        </account>
        """;

    when(accountDataService.hasAccountData(accountId)).thenReturn(false);
    when(proxyService.forwardRequest(eq(request), any()))
        .thenReturn(ResponseEntity.ok(xmlContent.getBytes()));

    // Mock cache save to throw exception
    doThrow(new IOException("Disk full"))
        .when(accountDataService)
        .saveFullAccountDataRaw(eq(accountId), anyString());

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Should still return data despite caching failure
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(accountId);

    // Verify cache save was attempted
    verify(accountDataService).saveFullAccountDataRaw(eq(accountId), anyString());
  }

  @Test
  void testPatch_SpotifyCredentialsReplaced_WhenMatchingAccountExists() throws IOException {
    // Given
    String accountId = "test-account-spotify";
    String spotifyUserId = "spotify-user-123";
    String originalToken = "old-token";
    String newRefreshToken = "new-refresh-token-abc";

    // Create a FullAccountResponse with a Spotify source
    FullAccountResponseApiDto response =
        createFullAccountWithSpotifySources(spotifyUserId, originalToken);

    // Mock SpotifyAccountService to return matching account
    SpotifyAccount spotifyAccount =
        new SpotifyAccount(
            spotifyUserId,
            "Test User",
            newRefreshToken,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(spotifyAccount));

    // Mock account data service
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isPresent();
    SourceApiDto spotifySource = result.get().getSources().getSource().getFirst();
    assertThat(spotifySource.getCredential().getValue()).isEqualTo(newRefreshToken);
  }

  @Test
  void testPatch_NonSpotifySourcesUnmodified() throws IOException {
    // Given
    String accountId = "test-account-mixed";
    String originalToken = "original-token";

    // Create response with non-Spotify source (sourceproviderid = "25")
    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);

    SourcesContainerApiDto sources = new SourcesContainerApiDto();
    List<SourceApiDto> sourceList = new ArrayList<>();

    SourceApiDto nonSpotifySource = createSource("25", "user123", originalToken);
    sourceList.add(nonSpotifySource);

    sources.setSource(sourceList);
    response.setSources(sources);

    // Mock SpotifyAccountService to return accounts
    SpotifyAccount spotifyAccount =
        new SpotifyAccount(
            "user123", "Test User", "new-token", OffsetDateTime.now(), OffsetDateTime.now(), null);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(spotifyAccount));

    // Mock account data service
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Non-Spotify source should NOT be modified
    assertThat(result).isPresent();
    SourceApiDto resultSource = result.get().getSources().getSource().getFirst();
    assertThat(resultSource.getCredential().getValue()).isEqualTo(originalToken);
  }

  @Test
  void testPatch_SpotifySourceWithNoMatchingAccount_Unchanged() throws IOException {
    // Given
    String accountId = "test-account-no-match";
    String spotifyUserId = "spotify-user-456";
    String originalToken = "original-token";

    FullAccountResponseApiDto response =
        createFullAccountWithSpotifySources(spotifyUserId, originalToken);

    // Mock SpotifyAccountService to return different account
    SpotifyAccount differentAccount =
        new SpotifyAccount(
            "different-user",
            "Different User",
            "different-token",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(differentAccount));

    // Mock account data service
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Credential should remain unchanged
    assertThat(result).isPresent();
    SourceApiDto spotifySource = result.get().getSources().getSource().getFirst();
    assertThat(spotifySource.getCredential().getValue()).isEqualTo(originalToken);
  }

  @Test
  void testPatch_HandlesNullSources() throws IOException {
    // Given
    String accountId = "test-account-null-sources";
    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);
    response.setSources(null);

    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Should not throw exception
    assertThat(result).isPresent();
    assertThat(result.get().getSources()).isNull();
  }

  @Test
  void testPatch_HandlesEmptySourceList() throws IOException {
    // Given
    String accountId = "test-account-empty-sources";
    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);

    SourcesContainerApiDto sources = new SourcesContainerApiDto();
    sources.setSource(new ArrayList<>());
    response.setSources(sources);

    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());
    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Should not throw exception
    assertThat(result).isPresent();
    assertThat(result.get().getSources().getSource()).isEmpty();
  }

  @Test
  void testPatch_HandlesNullCredential() throws IOException {
    // Given
    String accountId = "test-account-null-credential";
    String spotifyUserId = "spotify-user-789";

    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);

    SourcesContainerApiDto sources = new SourcesContainerApiDto();
    List<SourceApiDto> sourceList = new ArrayList<>();

    SourceApiDto spotifySource = createSource("15", spotifyUserId, "token");
    spotifySource.setCredential(null); // Null credential
    sourceList.add(spotifySource);

    sources.setSource(sourceList);
    response.setSources(sources);

    // Mock SpotifyAccountService to return matching account
    SpotifyAccount spotifyAccount =
        new SpotifyAccount(
            spotifyUserId,
            "Test User",
            "new-token",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            null);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(spotifyAccount));

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Should not throw exception, credential remains null
    assertThat(result).isPresent();
    assertThat(result.get().getSources().getSource().getFirst().getCredential()).isNull();
  }

  @Test
  void testPatch_MultipleSpotifySources_OnlyMatchingOnesUpdated() throws IOException {
    // Given
    String accountId = "test-account-multiple";

    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId(accountId);

    SourcesContainerApiDto sources = new SourcesContainerApiDto();
    List<SourceApiDto> sourceList = new ArrayList<>();

    // Add multiple Spotify sources
    SourceApiDto source1 = createSource("15", "user1", "token1");
    SourceApiDto source2 = createSource("15", "user2", "token2");
    SourceApiDto source3 = createSource("15", "user3", "token3");

    sourceList.add(source1);
    sourceList.add(source2);
    sourceList.add(source3);

    sources.setSource(sourceList);
    response.setSources(sources);

    // Mock SpotifyAccountService to return only matching accounts for user1 and user3
    SpotifyAccount account1 =
        new SpotifyAccount(
            "user1", "User 1", "new-token1", OffsetDateTime.now(), OffsetDateTime.now(), null);
    SpotifyAccount account3 =
        new SpotifyAccount(
            "user3", "User 3", "new-token3", OffsetDateTime.now(), OffsetDateTime.now(), null);
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of(account1, account3));

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then
    assertThat(result).isPresent();
    List<SourceApiDto> resultSources = result.get().getSources().getSource();
    assertThat(resultSources.get(0).getCredential().getValue()).isEqualTo("new-token1");
    assertThat(resultSources.get(1).getCredential().getValue()).isEqualTo("token2"); // Unchanged
    assertThat(resultSources.get(2).getCredential().getValue()).isEqualTo("new-token3");
  }

  @Test
  void testPatch_EmptySpotifyAccountList_NoChanges() throws IOException {
    // Given
    String accountId = "test-account-empty-spotify";
    String spotifyUserId = "spotify-user-999";
    String originalToken = "original-token";

    FullAccountResponseApiDto response =
        createFullAccountWithSpotifySources(spotifyUserId, originalToken);

    // Mock SpotifyAccountService to return empty list
    when(spotifyAccountService.listAllAccounts()).thenReturn(List.of());

    when(accountDataService.hasAccountData(accountId)).thenReturn(true);
    when(accountDataService.loadFullAccountData(accountId)).thenReturn(response);

    // When
    Optional<FullAccountResponseApiDto> result =
        fullAccountService.getFullAccount(accountId, request);

    // Then - Credential should remain unchanged
    assertThat(result).isPresent();
    SourceApiDto spotifySource = result.get().getSources().getSource().getFirst();
    assertThat(spotifySource.getCredential().getValue()).isEqualTo(originalToken);
  }

  // Helper methods

  private FullAccountResponseApiDto createFullAccountWithSpotifySources(
      String username, String credentialValue) {
    FullAccountResponseApiDto response = new FullAccountResponseApiDto();
    response.setId("test-account");

    SourcesContainerApiDto sources = new SourcesContainerApiDto();
    List<SourceApiDto> sourceList = new ArrayList<>();

    SourceApiDto spotifySource = createSource("15", username, credentialValue);
    sourceList.add(spotifySource);

    sources.setSource(sourceList);
    response.setSources(sources);

    return response;
  }

  private SourceApiDto createSource(
      String sourceproviderid, String username, String credentialValue) {
    SourceApiDto source = new SourceApiDto();
    source.setId("source-id");
    source.setType("Audio");
    source.setSourceproviderid(sourceproviderid);
    source.setUsername(username);
    source.setName("Test Source");
    source.setSourcename("Test Source Name");
    source.setSourceSettings(new Object());
    source.setCreatedOn(OffsetDateTime.now());
    source.setUpdatedOn(OffsetDateTime.now());

    CredentialApiDto credential = new CredentialApiDto();
    credential.setType("token");
    credential.setValue(credentialValue);
    source.setCredential(credential);

    return source;
  }
}
