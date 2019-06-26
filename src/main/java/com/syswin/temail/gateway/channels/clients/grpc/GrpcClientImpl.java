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
