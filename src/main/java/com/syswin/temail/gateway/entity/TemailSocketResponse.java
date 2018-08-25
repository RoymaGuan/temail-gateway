package com.syswin.temail.gateway.entity;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@Data
public class TemailSocketResponse {

  private String result;

  private String msg;

}