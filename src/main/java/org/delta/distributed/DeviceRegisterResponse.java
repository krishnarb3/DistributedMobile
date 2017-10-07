package org.delta.distributed;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceRegisterResponse {
  @JsonProperty("status")
  private String response;

  public DeviceRegisterResponse(String message) {
    this.response = message;
  }

  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }
}
