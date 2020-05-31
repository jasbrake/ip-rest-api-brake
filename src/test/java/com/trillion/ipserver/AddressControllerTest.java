package com.trillion.ipserver;

import com.trillion.ipserver.address.Address;
import com.trillion.ipserver.address.AddressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AddressControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private AddressRepository repository;

  @Test
  void testAddRange() throws Exception {
    List<Address> addresses = List.of(
        new Address("1.1.1.0", Address.STATUS_AVAILABLE),
        new Address("1.1.1.1", Address.STATUS_ACQUIRED)
    );
    doReturn(addresses).when(repository).saveAllNew(ArgumentMatchers.any());

    mockMvc.perform(post("/addresses")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"range\": \"1.1.1.0/31\" }"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].ip", is("1.1.1.0")))
        .andExpect(jsonPath("$[0].status", is(Address.STATUS_AVAILABLE)))
        .andExpect(jsonPath("$[1].ip", is("1.1.1.1")))
        .andExpect(jsonPath("$[1].status", is(Address.STATUS_ACQUIRED)));
  }

  @Test
  void testAddRange_InvalidPost() throws Exception {
    mockMvc.perform(post("/addresses")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testAddRange_InvalidRange() throws Exception {
    mockMvc.perform(post("/addresses")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{ \"range\": \"xyz\" }"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testAll_Empty() throws Exception {
    doReturn(List.of()).when(repository).findAll();
    mockMvc.perform(get("/addresses"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void testAll() throws Exception {
    List<Address> addresses = List.of(
        new Address("1.1.1.0", Address.STATUS_AVAILABLE),
        new Address("1.1.1.1", Address.STATUS_ACQUIRED)
    );
    doReturn(addresses).when(repository).findAll();

    mockMvc.perform(get("/addresses")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].ip", is("1.1.1.0")))
        .andExpect(jsonPath("$[0].status", is(Address.STATUS_AVAILABLE)))
        .andExpect(jsonPath("$[1].ip", is("1.1.1.1")))
        .andExpect(jsonPath("$[1].status", is(Address.STATUS_ACQUIRED)));
  }

  @Test
  void testGetAddress() throws Exception {
    Address addr = new Address("1.1.1.0", Address.STATUS_AVAILABLE);
    doReturn(Optional.of(addr)).when(repository).findById(ArgumentMatchers.anyString());
    mockMvc.perform(get("/addresses/1.1.1.0")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("{ \"ip\": \"1.1.1.0\", \"status\": \"available\" }"));
  }

  @Test
  void testGetAddress_NotFound() throws Exception {
    doReturn(Optional.empty()).when(repository).findById(ArgumentMatchers.anyString());
    mockMvc.perform(get("/addresses/1.1.1.0")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void testAcquire() throws Exception {
    Address addr = new Address("1.1.1.0", Address.STATUS_AVAILABLE);
    doReturn(Optional.of(addr)).when(repository).findById(ArgumentMatchers.anyString());
    when(repository.save(ArgumentMatchers.any(Address.class))).then(i -> i.getArgument(0));
    mockMvc.perform(post("/addresses/1.1.1.0/acquire")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("{ \"ip\": \"1.1.1.0\", \"status\": \"acquired\" }"));
  }

  @Test
  void testAcquire_Conflict() throws Exception {
    Address addr = new Address("1.1.1.0", Address.STATUS_ACQUIRED);
    doReturn(Optional.of(addr)).when(repository).findById(ArgumentMatchers.anyString());
    mockMvc.perform(post("/addresses/1.1.1.0/acquire")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isConflict());
  }

  @Test
  void testAcquire_NotFound() throws Exception {
    doReturn(Optional.empty()).when(repository).findById(ArgumentMatchers.anyString());
    mockMvc.perform(post("/addresses/1.1.1.0/acquire")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void testRelease() throws Exception {
    Address addr = new Address("1.1.1.0", Address.STATUS_ACQUIRED);
    doReturn(Optional.of(addr)).when(repository).findById(ArgumentMatchers.anyString());
    when(repository.save(ArgumentMatchers.any(Address.class))).then(i -> i.getArgument(0));
    mockMvc.perform(put("/addresses/1.1.1.0/release")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("{ \"ip\": \"1.1.1.0\", \"status\": \"available\" }"));
  }

  @Test
  void testRelease_NotFound() throws Exception {
    doReturn(Optional.empty()).when(repository).findById(ArgumentMatchers.anyString());
    mockMvc.perform(put("/addresses/1.1.1.0/release")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
