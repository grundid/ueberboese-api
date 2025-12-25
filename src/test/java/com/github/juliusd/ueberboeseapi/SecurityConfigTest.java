package com.github.juliusd.ueberboeseapi;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

class SecurityConfigTest extends TestBase {

  @Test
  void mgmtEndpoints_shouldRequireAuthentication() {
    given()
        .header("Content-Type", "application/json")
        .when()
        .post("/mgmt/spotify/init")
        .then()
        .statusCode(401);

    given()
        .header("Content-Type", "application/json")
        .when()
        .get("/mgmt/spotify/accounts")
        .then()
        .statusCode(401);
  }

  @Test
  void mgmtEndpoints_shouldRejectInvalidCredentials() {
    given()
        .auth()
        .basic("wrong_user", "wrong_pass")
        .header("Content-Type", "application/json")
        .when()
        .post("/mgmt/spotify/init")
        .then()
        .statusCode(401);

    given()
        .auth()
        .basic("admin", "wrong_password")
        .header("Content-Type", "application/json")
        .when()
        .post("/mgmt/spotify/init")
        .then()
        .statusCode(401);
  }

  @Test
  void streamingEndpoints_shouldNotRequireAuthentication() {
    given()
        .header("Content-Type", "application/xml")
        .when()
        .get("/streaming/sourceproviders")
        .then()
        .statusCode(200);
  }

  @Test
  void oauthEndpoints_shouldNotRequireAuthentication() {
    int statusCode =
        given()
            .header("Content-Type", "application/json")
            .when()
            .get("/oauth/token/refresh")
            .then()
            .extract()
            .statusCode();

    // Should not be 401 Unauthorized
    assert statusCode != 401 : "OAuth endpoints should not require authentication, but got 401";
  }

  @Test
  void rootEndpoint_shouldNotRequireAuthentication() {
    // Verify that root and other paths remain publicly accessible
    int statusCode = given().when().get("/").then().extract().statusCode();

    // Should not be 401 Unauthorized
    assert statusCode != 401 : "Root endpoint should not require authentication, but got 401";
  }

  @Test
  void actuatorEndpoints_shouldNotRequireAuthentication() {
    // Verify that actuator endpoints on management port remain publicly accessible
    // Note: Actuator runs on port 8081 (management port), not main app port
    // This test verifies the main app doesn't accidentally secure it
    int statusCode = given().when().get("/actuator/health").then().extract().statusCode();

    // Should not be 401 Unauthorized (might be 404 if not on same port in tests)
    assert statusCode != 401 : "Actuator endpoints should not require authentication, but got 401";
  }
}
