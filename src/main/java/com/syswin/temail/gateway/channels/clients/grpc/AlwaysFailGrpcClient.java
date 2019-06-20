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
    log.info("AlwaysFailGrpcClient register a gatewayServer: {}, result: false！", gatewayServer.toString());
    return false;
  }

  @Override
  public boolean serverOffLine(GatewayServer gatewayServer) {
    log.info("AlwaysFailGrpcClient offLine a gatewayServer: {}, result: false!", gatewayServer.toString());
    return false;
  }

  @Override
  public boolean serverHeartBeat(GatewayServer gatewayServer) {
    log.info("AlwaysFailGrpcClient send a heartBeat with: {}, result: false!", gatewayServer.toString());
    return false;
  }

  @Override
  public boolean syncChannelLocations(ChannelLocations channelLocations) {
    log.info("AlwaysFailGrpcClient sync channelLocations: {}, result: false!", channelLocations.toString());
    return false;
  }

  @Override
  public boolean removeChannelLocations(ChannelLocations channelLocations) {
    log.info("AlwaysFailGrpcClient remove channelLocations: {}, result: false!", channelLocations);
    return false;
  }

}
