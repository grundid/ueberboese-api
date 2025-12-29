package com.github.juliusd.ueberboeseapi.mgmt;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

import com.github.juliusd.ueberboeseapi.TestBase;
import com.github.juliusd.ueberboeseapi.service.DeviceTrackingService;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DirtiesContext
class MgmtControllerTest extends TestBase {

  @MockitoBean private DeviceTrackingService deviceTrackingService;

  @Test
  void listSpeakers_shouldReturnListOfSpeakers() {
    // Given
    String accountId = "6921042";
    List<DeviceTrackingService.DeviceInfo> devices = createDeviceInfoList();

    when(deviceTrackingService.getAllDevices()).thenReturn(devices);

    // When
    Response response =
        given()
            .auth()
            .basic("admin", "test-password-123")
            .accept(ContentType.JSON)
            .when()
            .get("/mgmt/accounts/{accountId}/speakers", accountId);

    // Then
    response
        .then()
        .statusCode(200)
        .contentType("application/json")
        .body("speakers", hasSize(2))
        .body("speakers[0].ipAddress", equalTo("192.168.1.100"))
        .body("speakers[1].ipAddress", equalTo("192.168.1.101"));
  }

  @Test
  void listSpeakers_shouldReturnEmptyListWhenNoDevices() {
    // Given
    String accountId = "6921042";

    when(deviceTrackingService.getAllDevices()).thenReturn(new ArrayList<>());

    // When
    Response response =
        given()
            .auth()
            .basic("admin", "test-password-123")
            .accept(ContentType.JSON)
            .when()
            .get("/mgmt/accounts/{accountId}/speakers", accountId);

    // Then
    response.then().statusCode(200).contentType("application/json").body("speakers", hasSize(0));
  }

  @Test
  void listSpeakers_shouldReturn500WhenRuntimeExceptionOccurs() {
    // Given
    String accountId = "6921042";

    when(deviceTrackingService.getAllDevices())
        .thenThrow(new RuntimeException("Service unavailable"));

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .accept(ContentType.JSON)
        .when()
        .get("/mgmt/accounts/{accountId}/speakers", accountId)
        .then()
        .statusCode(500)
        .contentType("application/json")
        .body("error", equalTo("Internal server error"))
        .body("message", equalTo("Failed to retrieve speakers"));
  }

  @Test
  void listSpeakers_shouldRequireAuthentication() {
    // Given
    String accountId = "6921042";

    // When / Then - No authentication
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/mgmt/accounts/{accountId}/speakers", accountId)
        .then()
        .statusCode(401);
  }

  @Test
  void listSpeakers_shouldRejectInvalidCredentials() {
    // Given
    String accountId = "6921042";

    // When / Then - Wrong password
    given()
        .auth()
        .basic("admin", "wrong-password")
        .accept(ContentType.JSON)
        .when()
        .get("/mgmt/accounts/{accountId}/speakers", accountId)
        .then()
        .statusCode(401);
  }

  // Helper methods to create test data

  private static List<DeviceTrackingService.DeviceInfo> createDeviceInfoList() {
    List<DeviceTrackingService.DeviceInfo> devices = new ArrayList<>();
    OffsetDateTime now = OffsetDateTime.now();

    devices.add(new DeviceTrackingService.DeviceInfo("device1", "192.168.1.100", now, now));
    devices.add(new DeviceTrackingService.DeviceInfo("device2", "192.168.1.101", now, now));

    return devices;
  }
}
