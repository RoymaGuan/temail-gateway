package com.syswin.temail.gateway.channels.clients.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocations;
import com.syswin.temail.channel.grpc.servers.GatewayServer;

public interface GrpcClient {

  /**
   * disconnect grpc connections
   */
  void closeConnection();

  /**
   * detect connection state between grpc client and grpc server
   */
  boolean retryConnection(GatewayServer gatewayServer);

  /**
   * server registry
   */
  boolean serverRegistry(GatewayServer gatewayServer);

  /**
   * server offLine
   */
  boolean serverOffLine(GatewayServer gatewayServer);

  /**
   * server heartBeat
   */
  boolean serverHeartBeat(GatewayServer gatewayServer);

  /**
   * add chanel locations
   */
  boolean syncChannelLocations(ChannelLocations channelLocations);

  /**
   * remove channel locations
   */
  boolean removeChannelLocations(ChannelLocations channelLocations);

  /**
   * called when a new connection was build
   */
  void newGeneration();

  /**
   * returns the generation of cur connection
   * @return
   */
  String getGeneration();



}
