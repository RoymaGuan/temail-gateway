package com.syswin.temail.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/**
 * account info
 */
public class AccountInfo {

  //账户信息
  private String account;

  // 移动端设备id
  private String devId;

  //平台,pc,android，ios
  private String platform;

  //版本号
  private String appVer;

}
