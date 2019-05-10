package com.syswin.temail.gateway;

import java.util.UUID;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.gateway")
@RefreshScope
public class TemailGatewayProperties {

  private String verifyUrl;
  private String dispatchUrl;
  private String updateSocketStatusUrl;

  private String grpcServerHost;
  private String grpcServerPort;

  private Netty netty = new Netty();
  private HttpClient httpClient = new HttpClient();

  private Rocketmq rocketmq = new Rocketmq();
  private Instance instance = new Instance();

  @Data
  public static class Netty {
    private int port;
    private int readIdleTimeSeconds = 180;
    private boolean epollEnabled = false;
  }

  @Data
  @ConfigurationProperties(prefix = "app.gateway.rocketmq")
  public static class Rocketmq {
    private String namesrvAddr;
    private String consumerGroup;
    private String mqTopic;
  }


  @Data
  public static class Instance {
    private String hostOf;
    private String processId;
    private String mqTag;

    public Instance() {
      hostOf = LocalMachineUtil.getLocalIp();
      processId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
      mqTag = "temail-server-" + hostOf + "-" + processId;
    }

  }

  @Data
  @Getter
  public static class HttpClient {
    private int maxConnectionsPerRoute = 1000;
    private int maxConnectionsTotal = 3000;
  }
}

