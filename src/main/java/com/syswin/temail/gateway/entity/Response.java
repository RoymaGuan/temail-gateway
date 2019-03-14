package com.syswin.temail.gateway.entity;

import static org.springframework.http.HttpStatus.OK;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Response<T> {

  private Integer code;
  private String message;
  private T data;

  private Response(HttpStatus status) {
    this.code = status.value();
  }

  private Response(HttpStatus status, String message) {
    this.code = status.value();
    this.message = message;
  }

  private Response(HttpStatus status, String message, T data) {
    this.code = status.value();
    this.message = message;
    this.data = data;
  }

  public static <T> Response<T> ok() {
    return new Response<>(OK);
  }

  public static <T> Response<T> ok(HttpStatus status) {
    return ok(status, null);
  }

  public static <T> Response<T> ok(T body) {
    return ok(OK, body);
  }

  public static <T> Response<T> ok(HttpStatus status, T body) {
    return new Response<>(status, null, body);
  }

  public static <T> Response<T> failed(HttpStatus status) {
    return new Response<>(status);
  }

  public static <T> Response<T> failed(HttpStatus status, String message) {
    return new Response<>(status, message);
  }

  public static <T> Response<T> failed(HttpStatus status, String message, T body) {
    return new Response<>(status, message, body);
  }

  public static <T> Response<T> response(HttpStatus status, String message, T body) {
    return new Response<>(status, message, body);
  }



}
