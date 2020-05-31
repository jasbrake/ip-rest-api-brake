package com.trillion.ipserver.address;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class Address {
  public static String STATUS_AVAILABLE = "available";
  public static String STATUS_ACQUIRED  = "acquired";

  @Id
  private String ip;
  private String status;

  public Address() {}

  public Address(String ip) {
    this.ip = ip;
    this.status = STATUS_AVAILABLE;
  }

  public Address(String ip, String status) {
    this.ip = ip;
    this.status = status;
  }
}
