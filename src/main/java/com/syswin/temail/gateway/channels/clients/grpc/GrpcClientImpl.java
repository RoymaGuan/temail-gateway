package com.syswin.temail.gateway.channels.clients.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocations;
import com.syswin.temail.channel.grpc.servers.GatewayRegistrySyncServerGrpc;
import com.syswin.temail.channel.grpc.servers.GatewayServer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.UUID;
import lombok.Data;

@Data
class GrpcClientImpl implements GrpcClient {

  private final GatewayRegistrySyncServerGrpc.GatewayRegistrySyncServerBlockingStub serverBlockingStub;

  private final ManagedChannel channel;

  private String generation = UUID.randomUUID().toString();

  public GrpcClientImpl(String host, int port) {
    this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
  }

  private GrpcClientImpl(ManagedChannelBuilder<?> channelBuilder) {
    this.channel = channelBuilder.build();
    this.serverBlockingStub = GatewayRegistrySyncServerGrpc.newBlockingStub(channel);
  }

  @Override
  public boolean retryConnection(GatewayServer gatewayServer) {
    return this.serverRegistry(gatewayServer);
  }

  @Override
  public void closeConnection() {
    channel.shutdown();
  }

  @Override
  public boolean serverRegistry(GatewayServer gatewayServer) {
    return serverBlockingStub.serverRegistry(gatewayServer).getIsSuccess();
  }

  @Override
  public boolean serverOffLine(GatewayServer gatewayServer) {
    return serverBlockingStub.serverOffLine(gatewayServer).getIsSuccess();
  }

  @Override
  public boolean serverHeartBeat(GatewayServer gatewayServer) {
    return serverBlockingStub.serverHeartBeat(gatewayServer).getIsSuccess();
  }

  @Override
  public boolean syncChannelLocations(ChannelLocations channelLocations) {
    return serverBlockingStub.syncChannelLocations(channelLocations).getIsSuccess();
  }

  @Override
  public boolean removeChannelLocations(ChannelLocations channelLocations) {
    return serverBlockingStub.removeChannelLocations(channelLocations).getIsSuccess();
  }

  @Override
  public void newGeneration() {
    this.generation = UUID.randomUUID().toString();
  }

  @Override
  public String getGeneration() {
    return this.generation;
  }

}
