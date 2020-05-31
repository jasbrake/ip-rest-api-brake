package com.trillion.ipserver.address;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.trillion.ipserver.address.Address.STATUS_AVAILABLE;
import static com.trillion.ipserver.address.Address.STATUS_ACQUIRED;

@Slf4j
@RestController
public class AddressController {
  private final AddressRepository repository;

  public AddressController(AddressRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/addresses")
  List<Address> all() {
    log.debug("Fetching all addresses");
    return repository.findAll();
  }

  @PostMapping("/addresses")
  List<Address> addRange(@RequestBody Map<String, Object> body) {
    if (StringUtils.isEmpty(body.get("range"))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid POST body");
    }
    IPAddress ip = new IPAddressString(body.get("range").toString()).getAddress();
    if (ip == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid IP range");
    }
    List<Address> addresses = new ArrayList<>();
    ip.getIterable().forEach(a -> addresses.add(new Address(a.getLower().withoutPrefixLength().toString())));
    log.info("Adding new range {} with {} addresses", ip, addresses.size());
    return repository.saveAllNew(addresses);
  }

  @GetMapping("/addresses/{ip}")
  Address getAddress(@PathVariable String ip) {
    log.debug("Fetching address {}", ip);
    return repository.findById(ip)
        .orElseThrow(() -> new AddressNotFoundException(ip));
  }

  @PostMapping("/addresses/{ip}/acquire")
  Address acquire(@PathVariable String ip) {
    Address addr = repository.findById(ip)
        .orElseThrow(() -> new AddressNotFoundException(ip));

    if (!addr.getStatus().equals(STATUS_AVAILABLE)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(
          "Only addresses with status '%s' may be acquired", STATUS_AVAILABLE));
    }
    log.info("Address {} ACQUIRED: status set from '{}' to '{}'", addr.getIp(), addr.getStatus(), STATUS_ACQUIRED);
    addr.setStatus(STATUS_ACQUIRED);
    return repository.save(addr);
  }

  @PutMapping("/addresses/{ip}/release")
  Address release(@PathVariable String ip) {
    Address addr = repository.findById(ip)
        .orElseThrow(() -> new AddressNotFoundException(ip));

    log.info("Address {} RELEASED: status set from '{}' to '{}'", addr.getIp(), addr.getStatus(), STATUS_AVAILABLE);
    addr.setStatus(STATUS_AVAILABLE);
    return repository.save(addr);
  }
}
