package com.github.juliusd.ueberboeseapi.service;

import com.github.juliusd.ueberboeseapi.generated.dtos.DeviceEventsRequestApiDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for storing and retrieving device events in-memory.
 *
 * <p>This service maintains an in-memory storage of events received from Bose SoundTouch devices,
 * organized by device ID.
 */
@Service
@Slf4j
public class EventStorageService {

  // In-memory storage of events by device ID
  private final Map<String, List<DeviceEventsRequestApiDto>> eventsByDevice =
      new ConcurrentHashMap<>();

  /**
   * Store an event for a specific device.
   *
   * @param deviceId The device ID
   * @param event The event data to store
   */
  public void storeEvent(String deviceId, DeviceEventsRequestApiDto event) {
    eventsByDevice.computeIfAbsent(deviceId, k -> new ArrayList<>()).add(event);
    log.debug(
        "Stored event for device: {}. Total events: {}",
        deviceId,
        eventsByDevice.get(deviceId).size());
  }

  /**
   * Get all events for a specific device.
   *
   * @param deviceId The device ID
   * @return List of events for the device (empty list if no events found)
   */
  public List<DeviceEventsRequestApiDto> getEventsForDevice(String deviceId) {
    return eventsByDevice.getOrDefault(deviceId, new ArrayList<>());
  }

  /**
   * Get the total number of events stored for a specific device.
   *
   * @param deviceId The device ID
   * @return Number of events stored
   */
  public int getEventCount(String deviceId) {
    return eventsByDevice.getOrDefault(deviceId, new ArrayList<>()).size();
  }

  /** Clear all stored events. Used for testing. */
  public void clearAllEvents() {
    log.debug("Clearing all stored events");
    eventsByDevice.clear();
  }

  /**
   * Clear events for a specific device.
   *
   * @param deviceId The device ID
   */
  public void clearEventsForDevice(String deviceId) {
    log.debug("Clearing events for device: {}", deviceId);
    eventsByDevice.remove(deviceId);
  }
}
