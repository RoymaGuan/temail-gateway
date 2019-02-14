package com.syswin.temail.gateway;

import java.util.UUID;
import javax.annotation.Resource;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@ConfigurationProperties(prefix = "app.gateway")
@Component
public class TemailGatewayProperties {

  private String verifyUrl;
  private String dispatchUrl;
  private String updateSocketStatusUrl;

  private String grpcServerHost;
  private String grpcServerPort;

  private Netty netty = new Netty();
  private HttpClient httpClient = new HttpClient();

  @Resource
  private Rocketmq rocketmq;
  private Instance instance = new Instance();

  @Data
  public static class Netty {

    private int port;
    private int readIdleTimeSeconds = 180;
    private boolean epollEnabled = false;
  }

  @Data
  @Component
  @ConfigurationProperties(prefix = "spring.rocketmq")
  public static class Rocketmq {

    private String namesrvAddr;
    private String consumerGroup;
    /**
     * 持有客户端链句柄的服务实例监听的消息队列topic
     */
    private String mqTopic;
  }


  @Data
  public static class Instance {

    /**
     * 持有客户端链句柄的服务实例宿主机地址
     */
    private String hostOf;
    /**
     * 持有客户端链句柄的服务实例的进程号
     */
    private String processId;
    /**
     * 持有客户端链句柄的服务实例监听的消息队列mqTag
     */
    private String mqTag;

    public Instance() {
      hostOf = LocalMachineUtil.getLocalIp();
      processId = UUID.randomUUID().toString().replace("-", "").toLowerCase();
      mqTag = "temail-server-" + hostOf + "-" + processId;
    }

  }

  @Getter
  public static class HttpClient {
    private int maxConnectionsPerRoute = 1000;
    private int maxConnectionsTotal = 3000;
  }
}
