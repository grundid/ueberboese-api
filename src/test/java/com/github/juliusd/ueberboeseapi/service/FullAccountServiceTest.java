package com.github.juliusd.ueberboeseapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.juliusd.ueberboeseapi.ProxyService;
import com.github.juliusd.ueberboeseapi.XmlMessageConverterConfig;
import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
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
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    accountDataService = mock(AccountDataService.class);
    proxyService = mock(ProxyService.class);
    XmlMessageConverterConfig config = new XmlMessageConverterConfig();
    xmlMapper = config.customXmlMapper();
    request = mock(HttpServletRequest.class);

    fullAccountService = new FullAccountService(accountDataService, proxyService, xmlMapper);
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
}
