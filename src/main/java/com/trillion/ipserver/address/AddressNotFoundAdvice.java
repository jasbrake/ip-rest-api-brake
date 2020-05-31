package com.trillion.ipserver.address;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AddressNotFoundAdvice {
  @ExceptionHandler(AddressNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  AddressNotFoundException addressNotFoundHandler(AddressNotFoundException ex) {
    return ex;
  }
}
