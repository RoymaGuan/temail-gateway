package com.syswin.temail.gateway.channels.clients.grpc;

import com.syswin.temail.channel.grpc.servers.GatewayServer;
import com.syswin.temail.gateway.TemailGatewayProperties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * heart beat Util for grpc client
 */
@Slf4j
@Data
class GrpcHeartBeatManager {


  private final AtomicBoolean isHeartBeatKeeping = new AtomicBoolean(false);

  private final ScheduledExecutorService executorService;

  private final String instanceProcessId;

  private final GatewayServer gatewayServer;

  private final int heartBeatDelay = 20;

  private final GrpcClient grpcClient;

  private final String instanceIp;

  GrpcHeartBeatManager(GrpcClient grpcClient, TemailGatewayProperties temailGatewayProperties) {
    this.executorService = Executors.newSingleThreadScheduledExecutor();
    this.instanceProcessId = temailGatewayProperties.getInstance().getProcessId();
    this.instanceIp = temailGatewayProperties.getInstance().getHostOf();
    this.gatewayServer = GatewayServer.newBuilder().setProcessId(instanceProcessId).setIp(instanceIp).build();
    this.grpcClient = grpcClient;
  }

  /**
   * heart beat logic
   */
  void heartBeat() {
    // so the heart beat task will be submitted for only one time;
    if (isHeartBeatKeeping.compareAndSet(false, true)) {
      log.info("heart beat is beginning.");
      executorService.scheduleWithFixedDelay(() -> {
        try {
          if (grpcClient.serverHeartBeat(gatewayServer)) {
            log.info("heart beat success : {}-{}", gatewayServer.getIp(), gatewayServer.getProcessId());
          } else {
            log.error("heart beat fail, try again after {} seconds .", heartBeatDelay);
          }
        } catch (Exception e) {
          log.error("exception happened in heart beat.", e);
        }
      }, heartBeatDelay, heartBeatDelay, TimeUnit.SECONDS);
    } else {
      log.info("heart beat task has already been triggered, {} will leave!", Thread.currentThread().getId());
    }
  }
}
