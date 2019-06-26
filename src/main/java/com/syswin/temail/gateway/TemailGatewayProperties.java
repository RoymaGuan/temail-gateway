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

