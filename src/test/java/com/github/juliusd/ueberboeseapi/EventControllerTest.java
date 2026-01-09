package com.github.juliusd.ueberboeseapi;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.juliusd.ueberboeseapi.service.EventStorageService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EventControllerTest extends TestBase {

  @Autowired private EventStorageService eventStorageService;

  @BeforeEach
  void clearEvents() {
    eventStorageService.clearAllEvents();
  }

  @Test
  void submitDeviceEvents_shouldAcceptValidEvent() {
    // Given
    String deviceId = "587A628A4042";
    String requestJson =
        """
        {
          "envelope": {
            "monoTime": 94118263,
            "payloadProtocolVersion": "3.1",
            "payloadType": "scmudc",
            "protocolVersion": "1.0",
            "time": "2026-01-09T08:02:32.874426+00:00",
            "uniqueId": "587A628A4042"
          },
          "payload": {
            "deviceInfo": {
              "boseID": "6921042",
              "deviceID": "587A628A4042",
              "deviceType": "SoundTouch 20",
              "serialNumber": "P123456789101123456789",
              "softwareVersion": "27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29",
              "systemSerialNumber": "069236P81556160AE"
            },
            "events": [
              {
                "data": {
                  "source-state": "SPOTIFY"
                },
                "monoTime": 94118263,
                "time": "2026-01-09T08:02:32.873379+00:00",
                "type": "source-state-changed"
              }
            ]
          }
        }
        """;

    // When / Then
    given()
        .header("Authorization", "Bearer mockAuthToken123")
        .header("Content-Type", "text/json; charset=utf-8")
        .body(requestJson)
        .when()
        .post("/v1/scmudc/{deviceId}", deviceId)
        .then()
        .statusCode(200);
  }

  @Test
  void submitDeviceEvents_shouldStoreMultipleEvents() {
    // Given
    String deviceId = "587A628A4042";
    String requestJson1 =
        """
        {
          "envelope": {
            "monoTime": 94118263,
            "payloadProtocolVersion": "3.1",
            "payloadType": "scmudc",
            "protocolVersion": "1.0",
            "time": "2026-01-09T08:02:32.874426+00:00",
            "uniqueId": "587A628A4042"
          },
          "payload": {
            "deviceInfo": {
              "boseID": "6921042",
              "deviceID": "587A628A4042",
              "deviceType": "SoundTouch 20",
              "serialNumber": "P123456789101123456789",
              "softwareVersion": "27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29",
              "systemSerialNumber": "069236P81556160AE"
            },
            "events": [
              {
                "data": {
                  "source-state": "SPOTIFY"
                },
                "monoTime": 94118263,
                "time": "2026-01-09T08:02:32.873379+00:00",
                "type": "source-state-changed"
              }
            ]
          }
        }
        """;

    String requestJson2 =
        """
        {
          "envelope": {
            "monoTime": 94118440,
            "payloadProtocolVersion": "3.1",
            "payloadType": "scmudc",
            "protocolVersion": "1.0",
            "time": "2026-01-09T08:02:33.050499+00:00",
            "uniqueId": "587A628A4042"
          },
          "payload": {
            "deviceInfo": {
              "boseID": "6921042",
              "deviceID": "587A628A4042",
              "deviceType": "SoundTouch 20",
              "serialNumber": "P123456789101123456789",
              "softwareVersion": "27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29",
              "systemSerialNumber": "069236P81556160AE"
            },
            "events": [
              {
                "data": {
                  "play-state": "STOP_STATE"
                },
                "monoTime": 94118439,
                "time": "2026-01-09T08:02:33.049407+00:00",
                "type": "play-state-changed"
              }
            ]
          }
        }
        """;

    // When
    given()
        .header("Authorization", "Bearer mockAuthToken123")
        .header("Content-Type", "text/json; charset=utf-8")
        .body(requestJson1)
        .post("/v1/scmudc/{deviceId}", deviceId);

    given()
        .header("Authorization", "Bearer mockAuthToken123")
        .header("Content-Type", "text/json; charset=utf-8")
        .body(requestJson2)
        .post("/v1/scmudc/{deviceId}", deviceId);

    // Then - retrieve events via management endpoint
    var response =
        given()
            .auth()
            .basic("admin", "test-password-123")
            .accept(ContentType.JSON)
            .when()
            .get("/mgmt/devices/{deviceId}/events", deviceId)
            .then()
            .statusCode(200)
            .extract()
            .response();

    assertThat(response.jsonPath().getList("events")).hasSize(2);
    assertThat(response.jsonPath().getString("events[0].type")).isEqualTo("source-state-changed");
    assertThat(response.jsonPath().getString("events[1].type")).isEqualTo("play-state-changed");
  }

  @Test
  void getDeviceEvents_shouldReturnEmptyListForUnknownDevice() {
    // Given
    String deviceId = "UNKNOWN_DEVICE";

    // When / Then
    given()
        .auth()
        .basic("admin", "test-password-123")
        .accept(ContentType.JSON)
        .when()
        .get("/mgmt/devices/{deviceId}/events", deviceId)
        .then()
        .statusCode(200)
        .body("events.size()", org.hamcrest.Matchers.is(0));
  }

  @Test
  void submitDeviceEvents_shouldHandleMultipleEventsInOneRequest() {
    // Given
    String deviceId = "587A628A4042";
    String requestJson =
        """
        {
          "envelope": {
            "monoTime": 94118263,
            "payloadProtocolVersion": "3.1",
            "payloadType": "scmudc",
            "protocolVersion": "1.0",
            "time": "2026-01-09T08:02:32.874426+00:00",
            "uniqueId": "587A628A4042"
          },
          "payload": {
            "deviceInfo": {
              "boseID": "6921042",
              "deviceID": "587A628A4042",
              "deviceType": "SoundTouch 20",
              "serialNumber": "P123456789101123456789",
              "softwareVersion": "27.0.6.46330.5043500 epdbuild.trunk.hepdswbld04.2022-08-04T11:20:29",
              "systemSerialNumber": "069236P81556160AE"
            },
            "events": [
              {
                "data": {
                  "source-state": "SPOTIFY"
                },
                "monoTime": 94118263,
                "time": "2026-01-09T08:02:32.873379+00:00",
                "type": "source-state-changed"
              },
              {
                "data": {
                  "art-status": "SHOW_DEFAULT_IMAGE",
                  "art-uri": ""
                },
                "monoTime": 94118288,
                "time": "2026-01-09T08:02:32.898378+00:00",
                "type": "art-changed"
              },
              {
                "data": {
                  "play-state": "BUFFERING_STATE"
                },
                "monoTime": 94118323,
                "time": "2026-01-09T08:02:32.931545+00:00",
                "type": "play-state-changed"
              }
            ]
          }
        }
        """;

    // When
    given()
        .header("Authorization", "Bearer mockAuthToken123")
        .header("Content-Type", "text/json; charset=utf-8")
        .body(requestJson)
        .post("/v1/scmudc/{deviceId}", deviceId);

    // Then
    var response =
        given()
            .auth()
            .basic("admin", "test-password-123")
            .accept(ContentType.JSON)
            .when()
            .get("/mgmt/devices/{deviceId}/events", deviceId)
            .then()
            .statusCode(200)
            .extract()
            .response();

    // Should return all 3 events as pure events (without envelope/deviceInfo wrapper)
    assertThat(response.jsonPath().getList("events")).hasSize(3);
    assertThat(response.jsonPath().getString("events[0].type")).isEqualTo("source-state-changed");
    assertThat(response.jsonPath().getString("events[1].type")).isEqualTo("art-changed");
    assertThat(response.jsonPath().getString("events[2].type")).isEqualTo("play-state-changed");
  }

  @Test
  void getDeviceEvents_shouldRequireAuthentication() {
    // Given
    String deviceId = "587A628A4042";

    // When / Then - No authentication
    given()
        .accept(ContentType.JSON)
        .when()
        .get("/mgmt/devices/{deviceId}/events", deviceId)
        .then()
        .statusCode(401);
  }
}
