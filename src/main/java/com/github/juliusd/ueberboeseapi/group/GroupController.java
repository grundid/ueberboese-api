package com.github.juliusd.ueberboeseapi.group;

import com.github.juliusd.ueberboeseapi.device.DeviceRepository;
import com.github.juliusd.ueberboeseapi.generated.GroupApi;
import com.github.juliusd.ueberboeseapi.generated.dtos.ErrorResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.GroupRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.dtos.GroupResponseApiDto;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class GroupController implements GroupApi {

  private final DeviceRepository deviceRepository;
  private final GroupService groupService;
  private final GroupMapper groupMapper;

  @Override
  public ResponseEntity<GroupResponseApiDto> createDeviceGroup(
      String accountId, GroupRequestApiDto groupRequestApiDto) {
    try {
      log.info("Creating group for accountId: {}", accountId);
      DeviceGroup group = groupService.createGroup(accountId, groupRequestApiDto);
      GroupResponseApiDto response = groupMapper.toResponseDto(group);

      return ResponseEntity.status(201)
          .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
          .body(response);

    } catch (DeviceAlreadyInGroupException e) {
      log.warn("Cannot create group: {}", e.getMessage());
      var errorResponse = new ErrorResponseApiDto().message(e.getMessage()).statusCode("4041");

      return (ResponseEntity<GroupResponseApiDto>)
          (ResponseEntity<?>)
              ResponseEntity.status(400)
                  .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
                  .body(errorResponse);
    }
  }

  @Override
  public ResponseEntity<GroupResponseApiDto> getDeviceGroup(String accountId, String deviceId) {
    log.info("Getting group for accountId: {}, deviceId: {}", accountId, deviceId);

    // Validate device exists
    if (deviceRepository.findById(deviceId).isEmpty()) {
      log.warn("Device {} does not exist", deviceId);
      var errorResponse =
          new ErrorResponseApiDto().message("Device does not exist").statusCode("4012");

      return (ResponseEntity<GroupResponseApiDto>)
          (ResponseEntity<?>)
              ResponseEntity.status(400)
                  .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
                  .body(errorResponse);
    }

    var groupOpt = groupService.getGroupByDeviceId(accountId, deviceId);

    if (groupOpt.isEmpty()) {
      // Return empty self-closing group element when device is not in a group
      byte[] emptyGroupXml =
          "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><group/>"
              .getBytes(StandardCharsets.UTF_8);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.parseMediaType("application/vnd.bose.streaming-v1.2+xml"));

      return (ResponseEntity<GroupResponseApiDto>)
          (ResponseEntity<?>) new ResponseEntity<>(emptyGroupXml, headers, HttpStatus.OK);
    }

    GroupResponseApiDto response = groupMapper.toResponseDto(groupOpt.get());
    return ResponseEntity.ok()
        .header("Content-Type", "application/vnd.bose.streaming-v1.2+xml")
        .body(response);
  }
}
