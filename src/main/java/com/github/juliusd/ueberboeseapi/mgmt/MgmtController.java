package com.github.juliusd.ueberboeseapi.mgmt;

import com.github.juliusd.ueberboeseapi.generated.dtos.FullAccountResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.AccountManagementApi;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ErrorApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ListSpeakers200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.SpeakerApiDto;
import com.github.juliusd.ueberboeseapi.service.AccountDataService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MgmtController implements AccountManagementApi {

  private final AccountDataService accountDataService;

  @Override
  public ResponseEntity<ListSpeakers200ResponseApiDto> listSpeakers(String accountId) {
    log.info("Listing speakers for accountId: {}", accountId);

    if (!accountDataService.hasAccountData(accountId)) {
      log.warn("Account data not found for accountId: {}", accountId);
      ErrorApiDto error = new ErrorApiDto();
      error.setError("Not found");
      error.setMessage("Account data not found for the specified account ID");
      return ResponseEntity.status(404).header("Content-Type", "application/json").body(null);
    }

    try {
      FullAccountResponseApiDto accountData = accountDataService.loadFullAccountData(accountId);

      List<SpeakerApiDto> speakers = new ArrayList<>();

      if (accountData.getDevices() != null && accountData.getDevices().getDevice() != null) {
        for (var device : accountData.getDevices().getDevice()) {
          if (device.getIpaddress() != null && !device.getIpaddress().isBlank()) {
            SpeakerApiDto speaker = new SpeakerApiDto();
            speaker.setIpAddress(device.getIpaddress());
            speakers.add(speaker);
          }
        }
      }

      ListSpeakers200ResponseApiDto response = new ListSpeakers200ResponseApiDto();
      response.setSpeakers(speakers);

      log.info("Successfully listed {} speaker(s) for accountId: {}", speakers.size(), accountId);
      return ResponseEntity.ok().header("Content-Type", "application/json").body(response);

    } catch (IOException e) {
      log.error("Failed to load account data for accountId: {}", accountId, e);
      throw new RuntimeException("Failed to load account data", e);
    }
  }

  /** Exception handler for RuntimeException - returns 500 Internal Server Error. */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorApiDto> handleRuntimeException(RuntimeException e) {
    log.error("Internal server error: {}", e.getMessage(), e);

    ErrorApiDto error = new ErrorApiDto();
    error.setError("Internal server error");
    error.setMessage("Failed to retrieve speakers");

    return ResponseEntity.status(500).header("Content-Type", "application/json").body(error);
  }
}
