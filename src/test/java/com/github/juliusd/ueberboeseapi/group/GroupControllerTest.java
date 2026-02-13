package com.github.juliusd.ueberboeseapi.group;

import static com.github.juliusd.ueberboeseapi.device.Device.builder;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isSimilarTo;

import com.github.juliusd.ueberboeseapi.TestBase;
import com.github.juliusd.ueberboeseapi.device.DeviceRepository;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.xmlunit.placeholder.PlaceholderDifferenceEvaluator;

class GroupControllerTest extends TestBase {

  @Autowired private DeviceGroupRepository deviceGroupRepository;
  @Autowired private DeviceRepository deviceRepository;

  @Test
  void createDeviceGroup_shouldReturnCreated() {
    // language=XML
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <group>
          <masterDeviceId>587A628A4042</masterDeviceId>
          <name>Living Room Stereo</name>
          <roles>
            <groupRole>
              <deviceId>587A628A4042</deviceId>
              <role>LEFT</role>
            </groupRole>
            <groupRole>
              <deviceId>44EAD8A18888</deviceId>
              <role>RIGHT</role>
            </groupRole>
          </roles>
        </group>""";

    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
            .body(requestXml)
            .when()
            .post("/streaming/account/6921042/group/")
            .then()
            .statusCode(201)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .extract()
            .body()
            .asString();

    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <group id="${xmlunit.isNumber}">
          <masterDeviceId>587A628A4042</masterDeviceId>
          <name>Living Room Stereo</name>
          <roles>
            <groupRole>
              <deviceId>587A628A4042</deviceId>
              <role>LEFT</role>
            </groupRole>
            <groupRole>
              <deviceId>44EAD8A18888</deviceId>
              <role>RIGHT</role>
            </groupRole>
          </roles>
        </group>""";

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void createDeviceGroup_shouldReturn400_whenDeviceAlreadyInGroup() {
    // Given - existing group
    givenGroupInDB("6921042", "587A628A4042", "587A628A4042", "44EAD8A18888");

    // language=XML
    String requestXml =
        """
        <?xml version="1.0" encoding="UTF-8" ?>
        <group>
          <masterDeviceId>NEWDEVICE</masterDeviceId>
          <name>Another Group</name>
          <roles>
            <groupRole>
              <deviceId>587A628A4042</deviceId>
              <role>LEFT</role>
            </groupRole>
            <groupRole>
              <deviceId>NEWDEVICE</deviceId>
              <role>RIGHT</role>
            </groupRole>
          </roles>
        </group>""";

    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .header("Content-type", "application/vnd.bose.streaming-v1.2+xml")
            .body(requestXml)
            .when()
            .post("/streaming/account/6921042/group/")
            .then()
            .statusCode(400)
            .extract()
            .body()
            .asString();

    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <status>
          <message>Device 587A628A4042 already belongs to a group</message>
          <status-code>4041</status-code>
        </status>""";

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void getDeviceGroup_shouldReturnGroup() {
    // Given - existing group
    givenGroupInDB("6921042", "587A628A4042", "587A628A4042", "44EAD8A18888");

    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .when()
            .get("/streaming/account/6921042/device/587A628A4042/group/")
            .then()
            .statusCode(200)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .extract()
            .body()
            .asString();

    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <group id="${xmlunit.isNumber}">
          <masterDeviceId>587A628A4042</masterDeviceId>
          <name>Test Group</name>
          <roles>
            <groupRole>
              <deviceId>587A628A4042</deviceId>
              <role>LEFT</role>
            </groupRole>
            <groupRole>
              <deviceId>44EAD8A18888</deviceId>
              <role>RIGHT</role>
            </groupRole>
          </roles>
        </group>""";

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void getDeviceGroup_shouldReturnGroupByRightDevice() {
    // Given - existing group
    givenGroupInDB("6921042", "587A628A4042", "587A628A4042", "44EAD8A18888");

    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .when()
            .get("/streaming/account/6921042/device/44EAD8A18888/group/")
            .then()
            .statusCode(200)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .extract()
            .body()
            .asString();

    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <group id="${xmlunit.isNumber}">
          <masterDeviceId>587A628A4042</masterDeviceId>
          <name>Test Group</name>
          <roles>
            <groupRole>
              <deviceId>587A628A4042</deviceId>
              <role>LEFT</role>
            </groupRole>
            <groupRole>
              <deviceId>44EAD8A18888</deviceId>
              <role>RIGHT</role>
            </groupRole>
          </roles>
        </group>""";

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void getDeviceGroup_shouldReturnEmptyGroup_whenDeviceNotInGroup() {
    // Given - device exists but is not in a group
    givenDeviceInDB("TEST123", "Test Device", "6921042");

    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .when()
            .get("/streaming/account/6921042/device/TEST123/group/")
            .then()
            .statusCode(200)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .extract()
            .body()
            .asString();

    // Should return empty group element (matches real Bose API behavior)
    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <group/>
        """;

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  @Test
  void getDeviceGroup_shouldReturn400_whenDeviceDoesNotExist() {
    String actualXml =
        given()
            .header("Accept", "application/vnd.bose.streaming-v1.2+xml")
            .when()
            .get("/streaming/account/6921042/device/NONEXISTENT/group/")
            .then()
            .statusCode(400)
            .contentType("application/vnd.bose.streaming-v1.2+xml")
            .extract()
            .body()
            .asString();

    // language=XML
    String expectedXml =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <status>
          <message>Device does not exist</message>
          <status-code>4012</status-code>
        </status>""";

    assertThat(
        actualXml,
        isSimilarTo(expectedXml)
            .ignoreWhitespace()
            .withDifferenceEvaluator(new PlaceholderDifferenceEvaluator()));
  }

  private void givenGroupInDB(
      String accountId, String masterDeviceId, String leftDeviceId, String rightDeviceId) {
    // Create devices first if they don't exist
    if (deviceRepository.findById(masterDeviceId).isEmpty()) {
      givenDeviceInDB(masterDeviceId, "Master Device", accountId);
    }
    if (deviceRepository.findById(leftDeviceId).isEmpty()) {
      givenDeviceInDB(leftDeviceId, "Left Device", accountId);
    }
    if (deviceRepository.findById(rightDeviceId).isEmpty()) {
      givenDeviceInDB(rightDeviceId, "Right Device", accountId);
    }

    var now = OffsetDateTime.now().withNano(0);
    DeviceGroup group =
        DeviceGroup.builder()
            .accountId(accountId)
            .masterDeviceId(masterDeviceId)
            .name("Test Group")
            .leftDeviceId(leftDeviceId)
            .rightDeviceId(rightDeviceId)
            .createdOn(now)
            .updatedOn(now)
            .build();
    deviceGroupRepository.save(group);
  }

  private void givenDeviceInDB(String deviceId, String name, String margeAccountId) {
    var now = OffsetDateTime.now().withNano(0);
    var device =
        builder()
            .deviceId(deviceId)
            .name(name)
            .margeAccountId(margeAccountId)
            .ipAddress("192.168.1.100")
            .firstSeen(now)
            .lastSeen(now)
            .updatedOn(now)
            .version(null)
            .build();
    deviceRepository.save(device);
  }
}
