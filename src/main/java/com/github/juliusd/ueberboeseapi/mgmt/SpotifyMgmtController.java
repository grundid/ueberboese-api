package com.github.juliusd.ueberboeseapi.mgmt;

import com.github.juliusd.ueberboeseapi.generated.mgmt.SpotifyManagementApi;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ConfirmSpotifyAuth200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ErrorApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.InitSpotifyAuth200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyManagementService;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SpotifyMgmtController implements SpotifyManagementApi {

  private final SpotifyManagementService spotifyManagementService;
  private final SpotifyMgmtProperties spotifyMgmtProperties;

  @Override
  public ResponseEntity<InitSpotifyAuth200ResponseApiDto> initSpotifyAuth() {
    log.info("Initializing Spotify OAuth flow");

    try {
      String redirectUri = spotifyMgmtProperties.redirectUri();
      String authUrl = spotifyManagementService.generateAuthorizationUrl(redirectUri);

      InitSpotifyAuth200ResponseApiDto response = new InitSpotifyAuth200ResponseApiDto();
      response.setRedirectUrl(URI.create(authUrl));

      log.info("Successfully initialized Spotify OAuth flow");
      return ResponseEntity.ok().header("Content-Type", "application/json").body(response);

    } catch (Exception e) {
      log.error("Failed to initialize Spotify OAuth flow: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  public ResponseEntity<ConfirmSpotifyAuth200ResponseApiDto> confirmSpotifyAuth(String code) {
    log.info("Confirming Spotify OAuth authentication");

    if (code == null || code.isBlank()) {
      log.warn("Missing or empty authorization code");
      throw new IllegalArgumentException("Authorization code is required");
    }

    try {
      String redirectUri = spotifyMgmtProperties.redirectUri();
      String accountId = spotifyManagementService.exchangeCodeForTokens(code, redirectUri);

      ConfirmSpotifyAuth200ResponseApiDto response = new ConfirmSpotifyAuth200ResponseApiDto();
      response.setSuccess(true);
      response.setMessage("Spotify account connected successfully");
      response.setAccountId(accountId);

      log.info("Successfully confirmed Spotify authentication for accountId: {}", accountId);
      return ResponseEntity.ok().header("Content-Type", "application/json").body(response);

    } catch (SpotifyManagementService.SpotifyManagementException e) {
      log.error("Spotify authentication failed: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Failed to confirm Spotify authentication: {}", e.getMessage());
      throw new RuntimeException("Failed to confirm Spotify authentication", e);
    }
  }

  /** Exception handler for IllegalArgumentException - returns 400 Bad Request. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorApiDto> handleIllegalArgumentException(IllegalArgumentException e) {
    log.warn("Bad request: {}", e.getMessage());

    ErrorApiDto error = new ErrorApiDto();
    error.setError("Missing parameter");
    error.setMessage(e.getMessage());

    return ResponseEntity.badRequest().header("Content-Type", "application/json").body(error);
  }

  /** Exception handler for SpotifyManagementException - returns 401 Unauthorized. */
  @ExceptionHandler(SpotifyManagementService.SpotifyManagementException.class)
  public ResponseEntity<ErrorApiDto> handleSpotifyManagementException(
      SpotifyManagementService.SpotifyManagementException e) {
    log.error("Spotify authentication error: {}", e.getMessage());

    ErrorApiDto error = new ErrorApiDto();
    error.setError("Authentication failed");
    error.setMessage(e.getMessage());

    return ResponseEntity.status(401).header("Content-Type", "application/json").body(error);
  }

  /** Generic exception handler - returns 500 Internal Server Error. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorApiDto> handleException(Exception e) {
    log.error("Internal server error: {}", e.getMessage(), e);

    ErrorApiDto error = new ErrorApiDto();
    error.setError("Internal server error");
    error.setMessage("Failed to process request");

    return ResponseEntity.status(500).header("Content-Type", "application/json").body(error);
  }
}
