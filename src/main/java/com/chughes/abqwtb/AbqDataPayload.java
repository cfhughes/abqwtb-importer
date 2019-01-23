package com.chughes.abqwtb;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AbqDataPayload {

  @SerializedName("allroutes")
  @Expose
  private List<Vehicle> allroutes;

  public List<Vehicle> getAllroutes() {
    return allroutes;
  }
}
