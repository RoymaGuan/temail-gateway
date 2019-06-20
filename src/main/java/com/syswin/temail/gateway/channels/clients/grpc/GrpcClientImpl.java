package com.syswin.temail.gateway.channels.clients.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocations;
import com.syswin.temail.channel.grpc.servers.GatewayRegistrySyncServerGrpc;
import com.syswin.temail.channel.grpc.servers.GatewayServer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.UUID;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
class GrpcClientImpl implements GrpcClient {

  private final GatewayRegistrySyncServerGrpc.GatewayRegistrySyncServerBlockingStub serverBlockingStub;

  private final ManagedChannel channel;

  private String generation = UUID.randomUUID().toString();

  private String targetAddress = "";

  public GrpcClientImpl(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    this.targetAddress = host+":"+port;
  }

  private GrpcClientImpl(ManagedChannelBuilder<?> channelBuilder) {
    this.channel = channelBuilder.build();
    this.serverBlockingStub = GatewayRegistrySyncServerGrpc.newBlockingStub(channel);
  }

  @Override
  public boolean retryConnection(GatewayServer gatewayServer) {
    boolean registry = this.serverRegistry(gatewayServer);
    log.info("GrpcClientImpl reconnect to grpcServer-{} with: {}, result: {}.",
        this.targetAddress, gatewayServer.toBuilder(), registry);
    return registry;
  }

  @Override
  public void closeConnection() {
    channel.shutdown();
  }

  @Override
  public boolean serverRegistry(GatewayServer gatewayServer) {
    boolean isSuccess = serverBlockingStub.serverRegistry(gatewayServer).getIsSuccess();
    log.info("GrpcClientImpl registry server: {} to: {}, result: {}.",
        gatewayServer.toString(), this.targetAddress, isSuccess);
    return isSuccess;
  }

  @Override
  public boolean serverOffLine(GatewayServer gatewayServer) {
    boolean isSuccess = serverBlockingStub.serverOffLine(gatewayServer).getIsSuccess();
    log.info("GrpcClientImpl offLine gatewayServer: {} to: {}, result: .},",
        gatewayServer.toString(), this.targetAddress, isSuccess);
    return isSuccess;
  }

  @Override
  public boolean serverHeartBeat(GatewayServer gatewayServer) {
    boolean isSuccess = serverBlockingStub.serverHeartBeat(gatewayServer).getIsSuccess();
    log.info("GrpcClientImpl send heartBeat: {} to: {}, result: {}.",
        gatewayServer.toString(), this.targetAddress, isSuccess);
    return isSuccess;
  }

  @Override
  public boolean syncChannelLocations(ChannelLocations channelLocations) {
    boolean isSuccess = serverBlockingStub.syncChannelLocations(channelLocations).getIsSuccess();
    log.info("GrpcClientImpl sync channelLocations: {} to: {}, result: {}.",
        channelLocations.toString(), this.targetAddress, isSuccess);
    return isSuccess;
  }

  @Override
  public boolean removeChannelLocations(ChannelLocations channelLocations) {
    boolean isSuccess = serverBlockingStub.removeChannelLocations(channelLocations).getIsSuccess();
    log.info("GrpcClientImpl remove channelLocations: {} to: {}, result: {}.",
        channelLocations.toString(), this.targetAddress, isSuccess);
    return isSuccess;
  }

}
