/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
