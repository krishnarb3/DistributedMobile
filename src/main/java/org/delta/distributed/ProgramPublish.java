package org.delta.distributed;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProgramPublish {
  @JsonProperty("weight")
  private Integer weight;
  @JsonProperty("program")
  private String program;
  @JsonProperty("slice")
  private Integer slice;

  public ProgramPublish(Integer weight, String program) {
    this.weight = weight;
    this.program = program;
  }

  public Integer getWeight() {
    return weight;
  }

  public void setWeight(Integer weight) {
    this.weight = weight;
  }

  public String getProgram() {
    return program;
  }

  public void setProgram(String program) {
    this.program = program;
  }

  public Integer getSlice() {
    return slice;
  }

  public void setSlice(Integer slice) {
    this.slice = slice;
  }
}
