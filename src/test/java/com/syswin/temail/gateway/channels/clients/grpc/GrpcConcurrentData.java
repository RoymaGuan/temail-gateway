package com.syswin.temail.gateway.channels.clients.grpc;

import com.syswin.temail.channel.grpc.servers.ChannelLocations;
import com.syswin.temail.channel.grpc.servers.GatewayServer;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class GrpcConcurrentData {

  public GatewayServer gatewayServer;

  public GrpcClientWrapper grpcClientWrapper;

  public TemailGatewayProperties temailGatewayProperties;

  public List<ChannelLocations> channelLocations = new ArrayList<>();

  public List<TemailAccoutLocations> temailAccoutLocations = new ArrayList<>();

  public void init4Test() {
    this.temailGatewayProperties = new TemailGatewayProperties();
    this.temailGatewayProperties.setGrpcServerHost("channel.msgseal.com");
    this.temailGatewayProperties.setGrpcServerPort("9110");
    this.temailGatewayProperties.getInstance().setHostOf(this.getGatewayServer().getIp());
    this.temailGatewayProperties.getInstance().setProcessId(this.gatewayServer.getProcessId());
    this.grpcClientWrapper = new GrpcClientWrapper(temailGatewayProperties);
  }

  public void changeClientStatusUnavailible() {
    grpcClientWrapper.reconnect(grpcClientWrapper.getGrpcClient().getGeneration());
  }

}
