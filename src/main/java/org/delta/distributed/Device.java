package org.delta.distributed;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Device {
  @JsonProperty("deviceName")
  private String deviceName;

  public String getDeviceName() {
    return deviceName;
  }
  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }
}
