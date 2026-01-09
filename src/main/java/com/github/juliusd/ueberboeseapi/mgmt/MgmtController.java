package com.github.juliusd.ueberboeseapi.mgmt;

import com.github.juliusd.ueberboeseapi.generated.dtos.DeviceEventsRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.AccountManagementApi;
import com.github.juliusd.ueberboeseapi.generated.mgmt.EventManagementApi;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.DeviceEventApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ErrorApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.EventDeviceInfoApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.EventEnvelopeApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.EventPayloadApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.GetDeviceEvents200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ListSpeakers200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.SpeakerApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.StoredDeviceEventApiDto;
import com.github.juliusd.ueberboeseapi.service.DeviceTrackingService;
import com.github.juliusd.ueberboeseapi.service.EventStorageService;
import java.time.OffsetDateTime;
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
public class MgmtController implements AccountManagementApi, EventManagementApi {

  private final DeviceTrackingService deviceTrackingService;
  private final EventStorageService eventStorageService;

  @Override
  public ResponseEntity<ListSpeakers200ResponseApiDto> listSpeakers(String accountId) {
    log.info("Listing speakers for accountId: {}", accountId);

    List<SpeakerApiDto> speakers = new ArrayList<>();

    // Get all devices from the device tracking service (ignoring accountId as per requirement)
    for (DeviceTrackingService.DeviceInfo deviceInfo : deviceTrackingService.getAllDevices()) {
      SpeakerApiDto speaker = new SpeakerApiDto();
      speaker.setIpAddress(deviceInfo.getIpAddress());
      speakers.add(speaker);
    }

    ListSpeakers200ResponseApiDto response = new ListSpeakers200ResponseApiDto();
    response.setSpeakers(speakers);

    log.info("Successfully listed {} speaker(s)", speakers.size());
    return ResponseEntity.ok().header("Content-Type", "application/json").body(response);
  }

  @Override
  public ResponseEntity<GetDeviceEvents200ResponseApiDto> getDeviceEvents(String deviceId) {
    log.info("Retrieving events for device: {}", deviceId);

    List<DeviceEventsRequestApiDto> events = eventStorageService.getEventsForDevice(deviceId);
    List<StoredDeviceEventApiDto> storedEvents = new ArrayList<>();

    // Convert from storage format to management API format
    for (DeviceEventsRequestApiDto event : events) {
      StoredDeviceEventApiDto storedEvent = new StoredDeviceEventApiDto();

      // Convert envelope
      EventEnvelopeApiDto envelope = new EventEnvelopeApiDto();
      envelope.setMonoTime(event.getEnvelope().getMonoTime());
      envelope.setPayloadProtocolVersion(event.getEnvelope().getPayloadProtocolVersion());
      envelope.setPayloadType(event.getEnvelope().getPayloadType());
      envelope.setProtocolVersion(event.getEnvelope().getProtocolVersion());
      envelope.setTime(event.getEnvelope().getTime());
      envelope.setUniqueId(event.getEnvelope().getUniqueId());
      storedEvent.setEnvelope(envelope);

      // Convert payload
      EventPayloadApiDto payload = new EventPayloadApiDto();

      // Convert device info
      EventDeviceInfoApiDto deviceInfo = new EventDeviceInfoApiDto();
      deviceInfo.setBoseID(event.getPayload().getDeviceInfo().getBoseID());
      deviceInfo.setDeviceID(event.getPayload().getDeviceInfo().getDeviceID());
      deviceInfo.setDeviceType(event.getPayload().getDeviceInfo().getDeviceType());
      deviceInfo.setSerialNumber(event.getPayload().getDeviceInfo().getSerialNumber());
      deviceInfo.setSoftwareVersion(event.getPayload().getDeviceInfo().getSoftwareVersion());
      deviceInfo.setSystemSerialNumber(event.getPayload().getDeviceInfo().getSystemSerialNumber());
      payload.setDeviceInfo(deviceInfo);

      // Convert events
      List<DeviceEventApiDto> deviceEvents = new ArrayList<>();
      for (com.github.juliusd.ueberboeseapi.generated.dtos.DeviceEventApiDto sourceEvent :
          event.getPayload().getEvents()) {
        DeviceEventApiDto deviceEvent = new DeviceEventApiDto();
        deviceEvent.setData(sourceEvent.getData());
        deviceEvent.setMonoTime(sourceEvent.getMonoTime());
        deviceEvent.setTime(sourceEvent.getTime());
        deviceEvent.setType(sourceEvent.getType());
        deviceEvents.add(deviceEvent);
      }
      payload.setEvents(deviceEvents);

      storedEvent.setPayload(payload);
      storedEvent.setReceivedAt(OffsetDateTime.now()); // Use current time as received time

      storedEvents.add(storedEvent);
    }

    GetDeviceEvents200ResponseApiDto response = new GetDeviceEvents200ResponseApiDto();
    response.setEvents(storedEvents);

    log.info("Retrieved {} events for device: {}", storedEvents.size(), deviceId);
    return ResponseEntity.ok().header("Content-Type", "application/json").body(response);
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
