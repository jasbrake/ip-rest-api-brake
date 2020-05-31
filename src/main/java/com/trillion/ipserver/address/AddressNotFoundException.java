package com.trillion.ipserver.address;

public class AddressNotFoundException extends RuntimeException {
  AddressNotFoundException(String ip) {
    super(String.format("Could not find address with IP %s", ip));
  }
}
