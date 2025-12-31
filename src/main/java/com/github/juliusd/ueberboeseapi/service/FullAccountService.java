package com.github.juliusd.ueberboeseapi.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.juliusd.ueberboeseapi.ProxyService;
import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Service for managing full account data operations. Handles caching, proxy forwarding, and XML
 * parsing for account data requests.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FullAccountService {

  private final AccountDataService accountDataService;
  private final ProxyService proxyService;
  private final XmlMapper xmlMapper;

  /**
   * Retrieves full account data for the given account ID. First checks the cache, and if not found,
   * forwards the request to the proxy service.
   *
   * @param accountId The account ID to retrieve data for
   * @param request The HTTP servlet request (needed for proxy forwarding)
   * @return Optional containing the account data if successful, empty otherwise
   */
  public Optional<FullAccountResponseApiDto> getFullAccount(
      String accountId, HttpServletRequest request) {
    log.info("Getting full account data for accountId: {}", accountId);

    // Check if cached data exists
    if (accountDataService.hasAccountData(accountId)) {
      try {
        FullAccountResponseApiDto response = accountDataService.loadFullAccountData(accountId);
        log.info("Successfully loaded account data from cache for accountId: {}", accountId);
        return Optional.of(response);
      } catch (IOException e) {
        log.error(
            "Failed to load account data from cache for accountId: {}, error: {}",
            accountId,
            e.getMessage());
        return Optional.empty();
      }
    }

    // Cache miss - forward request to proxy
    log.info("Cache miss for accountId: {}, forwarding request to proxy", accountId);
    ResponseEntity<byte[]> proxyResponse = proxyService.forwardRequest(request, null);

    // Check if proxy response is successful
    if (!proxyResponse.getStatusCode().is2xxSuccessful() || proxyResponse.getBody() == null) {
      log.warn(
          "Proxy request failed for accountId: {}, status: {}",
          accountId,
          proxyResponse.getStatusCode());
      return Optional.empty();
    }

    // Try to parse and cache the response
    try {
      String xmlContent = new String(proxyResponse.getBody());
      FullAccountResponseApiDto parsedResponse =
          xmlMapper.readValue(xmlContent, FullAccountResponseApiDto.class);

      // Cache the response for future use
      try {
        accountDataService.saveFullAccountDataRaw(accountId, xmlContent);
        log.info("Successfully cached account data for accountId: {}", accountId);
      } catch (Exception saveException) {
        log.error(
            "Failed to cache account data for accountId: {}, continuing with response. Error: {}",
            accountId,
            saveException.getMessage());
      }

      return Optional.of(parsedResponse);
    } catch (Exception parseException) {
      log.error(
          "Failed to parse proxy response for accountId: {}. Error: {}",
          accountId,
          parseException.getMessage());
      return Optional.empty();
    }
  }
}
