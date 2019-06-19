package com.syswin.temail.gateway.channels.clients.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocations;
import com.syswin.temail.channel.grpc.servers.GatewayServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlwaysFailGrpcClient implements GrpcClient {

  @Override
  public void closeConnection() {
  }

  @Override
  public boolean retryConnection(GatewayServer gatewayServer) {
    return false;
  }

  @Override
  public boolean serverRegistry(GatewayServer gatewayServer) {
    log.info("AlwaysFailGrpcClient.serverRegistry() is beging executed!");
    return false;
  }

  @Override
  public boolean serverOffLine(GatewayServer gatewayServer) {
    log.info("AlwaysFailGrpcClient.serverOffLine() is beging executed!");
    return false;
  }

  @Override
  public boolean serverHeartBeat(GatewayServer gatewayServer) {
    log.info("AlwaysFailGrpcClient.serverHeartBeat() is beging executed!");
    return false;
  }

  @Override
  public boolean syncChannelLocations(ChannelLocations channelLocations) {
    log.info("AlwaysFailGrpcClient.syncChannelLocations() is beging executed!");
    return false;
  }

  @Override
  public boolean removeChannelLocations(ChannelLocations channelLocations) {
    log.info("AlwaysFailGrpcClient.removeChannelLocations() is beging executed!");
    return false;
  }

  @Override
  public void newGeneration() {

  }

  @Override
  public String getGeneration() {
    return "";
  }

}
