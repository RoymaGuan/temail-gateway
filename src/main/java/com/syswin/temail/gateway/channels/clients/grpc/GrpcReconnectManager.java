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
