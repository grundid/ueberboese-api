package com.github.juliusd.ueberboeseapi;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.juliusd.ueberboeseapi.generated.ExperimentalApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import com.github.juliusd.ueberboeseapi.service.AccountDataService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "ueberboese.experimental.enabled", havingValue = "true")
public class UeberboeseExperimentalController implements ExperimentalApi {

  private static final Logger logger =
      LoggerFactory.getLogger(UeberboeseExperimentalController.class);

  private final AccountDataService accountDataService;
  private final ProxyService proxyService;
  private final XmlMapper xmlMapper;

  @Autowired private HttpServletRequest request;

  public UeberboeseExperimentalController(
      AccountDataService accountDataService, ProxyService proxyService, XmlMapper xmlMapper) {
    this.accountDataService = accountDataService;
    this.proxyService = proxyService;
    this.xmlMapper = xmlMapper;
  }

  @Override
  public ResponseEntity<FullAccountResponseApiDto> getFullAccount(String accountId) {
    logger.debug("Getting full account data for accountId: {}", accountId);

    // Check if cached data exists
    if (accountDataService.hasAccountData(accountId)) {
      try {
        FullAccountResponseApiDto response = accountDataService.loadFullAccountData(accountId);
        logger.info("Successfully loaded account data from cache for accountId: {}", accountId);

        return ResponseEntity.ok()
            .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
            .header("METHOD_NAME", "getFullAccount")
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            .header(
                "Access-Control-Allow-Headers",
                "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization")
            .header("Access-Control-Expose-Headers", "Authorization")
            .body(response);
      } catch (IOException e) {
        logger.error(
            "Failed to load account data from cache for accountId: {}, error: {}",
            accountId,
            e.getMessage());
        return ResponseEntity.status(502)
            .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
            .build();
      }
    }

    // Cache miss - forward request to proxy
    logger.info("Cache miss for accountId: {}, forwarding request to proxy", accountId);
    ResponseEntity<byte[]> proxyResponse = proxyService.forwardRequest(request, null);

    // Check if proxy response is successful
    if (!proxyResponse.getStatusCode().is2xxSuccessful() || proxyResponse.getBody() == null) {
      logger.warn(
          "Proxy request failed for accountId: {}, status: {}",
          accountId,
          proxyResponse.getStatusCode());
      return ResponseEntity.status(502)
          .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
          .build();
    }

    // Try to parse and cache the response
    try {
      String xmlContent = new String(proxyResponse.getBody());
      FullAccountResponseApiDto parsedResponse =
          xmlMapper.readValue(xmlContent, FullAccountResponseApiDto.class);

      // Cache the response for future use
      try {
        accountDataService.saveFullAccountDataRaw(accountId, xmlContent);
        logger.info("Successfully cached account data for accountId: {}", accountId);
      } catch (Exception saveException) {
        logger.error(
            "Failed to cache account data for accountId: {}, continuing with response. Error: {}",
            accountId,
            saveException.getMessage());
      }

      return ResponseEntity.status(proxyResponse.getStatusCode())
          .headers(proxyResponse.getHeaders())
          .body(parsedResponse);
    } catch (Exception parseException) {
      logger.error(
          "Failed to parse proxy response for accountId: {}. Error: {}",
          accountId,
          parseException.getMessage());
      return ResponseEntity.status(502)
          .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
          .build();
    }
  }
}
