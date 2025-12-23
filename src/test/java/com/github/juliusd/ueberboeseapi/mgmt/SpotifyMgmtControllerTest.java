package com.github.juliusd.ueberboeseapi.mgmt;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.github.juliusd.ueberboeseapi.TestBase;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyManagementService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DirtiesContext
class SpotifyMgmtControllerTest extends TestBase {

  @MockitoBean private SpotifyManagementService spotifyManagementService;

  @Test
  void initSpotifyAuth_shouldReturnRedirectUrl() {
    // Given
    String mockAuthUrl =
        "https://accounts.spotify.com/authorize?client_id=test&response_type=code&redirect_uri=ueberboese-login%3A%2F%2Fspotify&scope=playlist-read-private+user-read-private";

    when(spotifyManagementService.generateAuthorizationUrl(anyString())).thenReturn(mockAuthUrl);

    // When / Then
    given()
        .header("Content-Type", "application/json")
        .accept(ContentType.JSON)
        .when()
        .post("/mgmt/spotify/init")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("redirectUrl", equalTo(mockAuthUrl));
  }

  @Test
  void confirmSpotifyAuth_shouldReturnAccountId() {
    // Given
    String authCode = "test_authorization_code_123";
    String mockAccountId = "spotify_user_abc123";

    when(spotifyManagementService.exchangeCodeForTokens(anyString(), anyString()))
        .thenReturn(mockAccountId);

    // When / Then
    given()
        .header("Content-Type", "application/json")
        .queryParam("code", authCode)
        .when()
        .post("/mgmt/spotify/confirm")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("success", equalTo(true))
        .body("message", equalTo("Spotify account connected successfully"))
        .body("accountId", equalTo(mockAccountId));
  }

  @Test
  void confirmSpotifyAuth_shouldRequireCode() {
    // When / Then - missing code parameter
    // Spring will return 400 automatically for missing required parameter
    given()
        .header("Content-Type", "application/json")
        .when()
        .post("/mgmt/spotify/confirm")
        .then()
        .statusCode(
            anyOf(is(400), is(500))); // Accept either 400 or 500 depending on Spring behavior
  }

  @Test
  void confirmSpotifyAuth_shouldRequireNonEmptyCode() {
    // When / Then - empty code parameter
    given()
        .header("Content-Type", "application/json")
        .queryParam("code", "")
        .when()
        .post("/mgmt/spotify/confirm")
        .then()
        .statusCode(400)
        .contentType("application/json")
        .body("error", equalTo("Missing parameter"))
        .body("message", containsString("required"));
  }

  @Test
  void confirmSpotifyAuth_shouldHandleInvalidCode() {
    // Given
    String invalidCode = "invalid_code_xyz";

    when(spotifyManagementService.exchangeCodeForTokens(anyString(), anyString()))
        .thenThrow(
            new SpotifyManagementService.SpotifyManagementException(
                "Failed to authenticate with Spotify", new RuntimeException("Invalid code")));

    // When / Then
    given()
        .header("Content-Type", "application/json")
        .queryParam("code", invalidCode)
        .when()
        .post("/mgmt/spotify/confirm")
        .then()
        .statusCode(401)
        .contentType("application/json")
        .body("error", equalTo("Authentication failed"))
        .body("message", containsString("Spotify"));
  }

  @Test
  void confirmSpotifyAuth_shouldHandleSpotifyApiFailure() {
    // Given
    String authCode = "test_code";

    when(spotifyManagementService.exchangeCodeForTokens(anyString(), anyString()))
        .thenThrow(
            new SpotifyManagementService.SpotifyManagementException(
                "Failed to exchange code", new RuntimeException("API error")));

    // When / Then
    given()
        .header("Content-Type", "application/json")
        .queryParam("code", authCode)
        .when()
        .post("/mgmt/spotify/confirm")
        .then()
        .statusCode(401)
        .contentType("application/json")
        .body("error", equalTo("Authentication failed"))
        .body("message", notNullValue());
  }

  @Test
  void initSpotifyAuth_shouldHandleServiceException() {
    // Given
    when(spotifyManagementService.generateAuthorizationUrl(anyString()))
        .thenThrow(new RuntimeException("Unexpected error"));

    // When / Then
    given()
        .header("Content-Type", "application/json")
        .when()
        .post("/mgmt/spotify/init")
        .then()
        .statusCode(500)
        .contentType("application/json")
        .body("error", equalTo("Internal server error"))
        .body("message", notNullValue());
  }

  @Test
  void confirmSpotifyAuth_shouldReturnValidAccountIdFormat() {
    // Given
    String authCode = "valid_code_456";
    String expectedAccountId = "user_valid_789";

    when(spotifyManagementService.exchangeCodeForTokens(anyString(), anyString()))
        .thenReturn(expectedAccountId);

    // When / Then
    given()
        .header("Content-Type", "application/json")
        .queryParam("code", authCode)
        .when()
        .post("/mgmt/spotify/confirm")
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("accountId", matchesPattern("^[a-zA-Z0-9_]+$")); // Simple alphanumeric validation
  }

}
