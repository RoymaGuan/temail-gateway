package com.syswin.temail.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemailAccoutLocation {

  //账户信息
  private String account;

  // 移动端设备id
  private String devId;

  //平台,pc,android，ios
  private String platform;

  // 持有客户端链句柄的服务实例宿主机地址
  private String hostOf;

  // 持有客户端链句柄的服务实例的进程号
  private String processId;

  // 持有客户端链句柄的服务实例监听的消息队列topic
  private String mqTopic;

  // 持有客户端链句柄的服务实例监听的消息队列mqTag
  private String mqTag;

}
