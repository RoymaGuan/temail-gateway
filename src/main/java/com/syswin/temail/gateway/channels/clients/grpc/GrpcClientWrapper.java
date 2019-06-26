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

import com.syswin.temail.channel.grpc.servers.ChannelLocation;
import com.syswin.temail.channel.grpc.servers.ChannelLocations;
import com.syswin.temail.channel.grpc.servers.GatewayServer;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.channels.ChannelsSyncClient;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * in wrapper we add reconnect and heartBeat logic to keep original client clean.<p> be aware of keeping client fail
 * fast when channel-server is not available and may be we can update the channels in batch for this fail fast
 * afterwards ...
 */
@Slf4j
@Data
//@Component
public class GrpcClientWrapper implements GrpcClient, ChannelsSyncClient {

  private final GrpcClient alwaysFailGrpcClient = new AlwaysFailGrpcClient();

  private final AtomicReference<GrpcClient> grpcClientReference;

  private final TemailGatewayProperties temailGatewayProperties;

  private final GrpcHeartBeatManager grpcHeartBeatManager;

  private final GrpcReconnectManager grpcReconnectManager;

  private final GatewayServer curServerInfo;

  private final GrpcClient grpcClient;


  @Autowired
  public GrpcClientWrapper(TemailGatewayProperties temailGatewayProperties) {
    log.info("Grpc hosts is {}, port is {}", temailGatewayProperties.getGrpcServerHost(),
        temailGatewayProperties.getGrpcServerPort());
    this.temailGatewayProperties = temailGatewayProperties;
    this.grpcReconnectManager = new GrpcReconnectManager(this, temailGatewayProperties);
    this.grpcHeartBeatManager = new GrpcHeartBeatManager(this, temailGatewayProperties);
    this.grpcClient = new GrpcClientImpl(temailGatewayProperties.getGrpcServerHost(),
        Integer.valueOf(temailGatewayProperties.getGrpcServerPort()));
    this.grpcClientReference = new AtomicReference<>(grpcClient);
    this.curServerInfo = GatewayServer.newBuilder()
        .setProcessId(temailGatewayProperties.getInstance().getProcessId())
        .setIp(temailGatewayProperties.getInstance().getHostOf())
        .build();
  }


  @Override
  public void initClient() {
    this.serverRegistry(curServerInfo);
    grpcHeartBeatManager.heartBeat();
  }


  @Override
  public void destroyClient() {
    this.serverOffLine(curServerInfo);
    this.closeConnection();
  }


  @Override
  public boolean retryConnection(GatewayServer gatewayServer) {
    try {
      return grpcClient.retryConnection(gatewayServer);
    } catch (Exception e) {
      log.error("Exception happened while reconnect to grpcServer with: {}.",
          gatewayServer.toString(), e);
      return false;
    }
  }


  @Override
  public void closeConnection() {
    this.grpcClient.closeConnection();
  }


  @Override
  public boolean serverRegistry(GatewayServer gatewayServer) {
    try {
      return grpcClientReference.get().serverRegistry(gatewayServer);
    } catch (Exception e) {
      log.error("Exception happened while try to registry: {} to grpcServer.",
          gatewayServer.toString() ,e);
      reconnect();
      return false;
    }
  }


  @Override
  public boolean serverOffLine(GatewayServer gatewayServer) {
    try {
      return grpcClientReference.get().serverOffLine(gatewayServer);
    } catch (Exception e) {
      //even fail, the server offLine will be executed by channel server
      //when heart beat timeout so do not try to reconnect again.
      log.error("Exception happened while offLine gatewayServer: {}.",
          gatewayServer.toString() ,e);
      return false;
    }
  }


  @Override
  public boolean serverHeartBeat(GatewayServer gatewayServer) {
    try {
      return grpcClientReference.get().serverHeartBeat(gatewayServer);
    } catch (Exception e) {
      log.error("Exception happened while send heartBeat: {} to grpcServer.",
          gatewayServer.toString(), e);
      reconnect();
      return false;
    }
  }


  @Override
  public boolean syncChannelLocations(ChannelLocations channelLocations) {
    try {
      log.info("sync channel Locations success : {}", channelLocations.toString());
      return grpcClientReference.get().syncChannelLocations(channelLocations);
    } catch (Exception e) {
      log.error("Exception happened while try to sync channelLocations: {} ",
          channelLocations.toString(), e);
      reconnect();
      return false;
    }
  }


  @Override
  public boolean removeChannelLocations(ChannelLocations channelLocations) {
    try {
      log.info("remove channel Locations success : {} - success. ", channelLocations.toString());
      return grpcClientReference.get().removeChannelLocations(channelLocations);
    } catch (Exception e) {
      log.error("Exception happened while try to remove channelLocations: {} ",
          channelLocations.toString(), e);
      reconnect();
      return false;
    }
  }

  /**
   * reconnect client by trying to registry current server.
   */
  void reconnect() {
    if (grpcClientReference.compareAndSet(grpcClient, alwaysFailGrpcClient)) {
      log.info("Grpc client is unavailable, try to reconnect!");
      grpcReconnectManager.reconnect(
          () -> grpcClientReference.compareAndSet(alwaysFailGrpcClient, grpcClient));
    }
  }


  @Override
  public boolean syncChannelLocations(TemailAccoutLocations channelLocations) {
    ChannelLocations.Builder builder = ChannelLocations.newBuilder();
    extractGrpcLocations(channelLocations, builder);
    return this.syncChannelLocations(builder.build());
  }


  @Override
  public boolean removeChannelLocations(TemailAccoutLocations channelLocations) {
    ChannelLocations.Builder builder = ChannelLocations.newBuilder();
    extractGrpcLocations(channelLocations, builder);
    return this.removeChannelLocations(builder.build());
  }


  private void extractGrpcLocations(TemailAccoutLocations channelLocations,
      ChannelLocations.Builder builder) {
    channelLocations.getStatuses().forEach(lc ->
        builder.addChannelLocationList(
            ChannelLocation.newBuilder()
                .setAccount(lc.getAccount())
                .setDevId(lc.getDevId())
                .setPlatform(lc.getPlatform())
                .setHostOf(lc.getHostOf())
                .setProcessId(lc.getProcessId())
                .setMqTopic(lc.getMqTopic())
                .setMqTag(lc.getMqTag())
                .build()));
  }
}
