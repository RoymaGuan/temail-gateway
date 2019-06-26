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

package com.syswin.temail.gateway.channels.clients.grpc;

import com.syswin.temail.channel.grpc.servers.GatewayServer;
import com.syswin.temail.gateway.TemailGatewayProperties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * reconnet util for grpc client
 */
@Slf4j
@Data
public class GrpcReconnectManager {

  private final TemailGatewayProperties temailGatewayProperties;

  private final GrpcClientWrapper grpcClientWrapper;

  private final ExecutorService executorService;

  private final GatewayServer gatewayServer;

  private final int reconnectDelay = 5;

  public GrpcReconnectManager(GrpcClientWrapper grpcClientWrapper,
      TemailGatewayProperties temailGatewayProperties) {
    this.temailGatewayProperties = temailGatewayProperties;
    this.gatewayServer = GatewayServer.newBuilder()
        .setIp(temailGatewayProperties.getInstance().getHostOf())
        .setProcessId(temailGatewayProperties.getInstance().getProcessId()).build();
    this.executorService = Executors.newSingleThreadExecutor();
    this.grpcClientWrapper = grpcClientWrapper;
  }

  /**
   * be aware of only one reconnect task can be triggered in the same time
   */
  public void reconnect(Runnable onConnectedHandler) {
    executorService.submit(() -> {
      log.info("Reconnect logic will be executed.");
      while (!Thread.currentThread().isInterrupted()) {
        try {
          if (!grpcClientWrapper.getGrpcClient().retryConnection(gatewayServer)) {
            log.error("Reconnect fail, {} seconds try again! ", reconnectDelay);
            throw new IllegalStateException("reconnect fail.");
          }
          onConnectedHandler.run();
          log.info("Reconnect success, now exit the reconnect loop! ");
          break;
        } catch (Exception e) {
          log.warn("Reconnect fail, it will try again after {} seconds ! ", reconnectDelay, e);
          try {
            TimeUnit.SECONDS.sleep(reconnectDelay);
          } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
            log.warn("Reconnect loop is interrupted, now exit!", e1);
          }
        }
      }
    });
  }
}
