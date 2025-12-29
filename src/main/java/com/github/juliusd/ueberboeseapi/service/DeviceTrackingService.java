package com.github.juliusd.ueberboeseapi.service;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for tracking devices that report to the /streaming/support/power_on endpoint. Maintains a
 * HashMap of device IDs to device information including IP address and timestamps.
 */
@Service
@Slf4j
public class DeviceTrackingService {

  private final ConcurrentHashMap<String, DeviceInfo> devices = new ConcurrentHashMap<>();

  /**
   * Records a device power-on event. If this is the first time the device is seen, creates a new
   * entry with firstSeen timestamp. Otherwise updates the lastSeen timestamp.
   *
   * @param deviceId The device identifier from the power_on request
   * @param ipAddress The IP address of the device
   */
  public void recordDevicePowerOn(String deviceId, String ipAddress) {
    log.debug("Recording power_on for device: {} at IP: {}", deviceId, ipAddress);

    devices.compute(
        deviceId,
        (key, existingDevice) -> {
          if (existingDevice == null) {
            // First time seeing this device
            OffsetDateTime now = OffsetDateTime.now();
            log.info(
                "New device registered: {} at IP: {} (first seen: {})", deviceId, ipAddress, now);
            return new DeviceInfo(deviceId, ipAddress, now, now);
          } else {
            // Update existing device
            OffsetDateTime now = OffsetDateTime.now();
            log.debug(
                "Updating device: {} at IP: {} (last seen: {}, previous IP: {})",
                deviceId,
                ipAddress,
                now,
                existingDevice.getIpAddress());
            return new DeviceInfo(deviceId, ipAddress, existingDevice.getFirstSeen(), now);
          }
        });
  }

  /**
   * Returns all tracked devices.
   *
   * @return Collection of DeviceInfo objects for all devices that have reported to power_on
   */
  public Collection<DeviceInfo> getAllDevices() {
    log.debug("Retrieving all tracked devices (count: {})", devices.size());
    return devices.values();
  }

  /** Data class representing information about a tracked device. */
  @Data
  @AllArgsConstructor
  public static class DeviceInfo {
    private String deviceId;
    private String ipAddress;
    private OffsetDateTime firstSeen;
    private OffsetDateTime lastSeen;
  }
}
