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
import com.syswin.temail.gateway.entity.TemailAccoutLocation;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcConcurentDataUtil {

  private final Random random = new Random(2);

  private final Integer gateServerCount;    //GateServers count

  private final Integer lcCountPerServer;   //ChannelLocationses per gateServer

  private final Integer lcCountPerLocations;  //ChannelLocationes per ChannelLocations

  @Getter
  public List<GrpcConcurrentData> grpcTestUnits;

  public GrpcConcurentDataUtil(Integer gateServerCount,
      Integer lcCountPerServer, Integer lcCountPerLocations) {
    this.gateServerCount = Optional.ofNullable(gateServerCount).orElse(10);
    this.lcCountPerServer = Optional.ofNullable(lcCountPerServer).orElse(200);
    this.lcCountPerLocations = Optional.ofNullable(lcCountPerLocations).orElse(1 + random.nextInt(4));
    this.grpcTestUnits = this.geneData();
  }

  public List<GrpcConcurrentData> geneData() {
    grpcTestUnits = new ArrayList<>();
    for (Integer i = 0; i < gateServerCount; i++) {
      GrpcConcurrentData grpcTestUnit = new GrpcConcurrentData();
      GatewayServer gatewayServer = GatewayServer.newBuilder()
          .setIp(CommonDataGeneUtil.extractIp())
          .setProcessId(CommonDataGeneUtil.extractUUID())
          .build();

      for (Integer j = 0; j < lcCountPerServer; j++) {
        ChannelLocations.Builder channelLocationsBuilder = ChannelLocations.newBuilder();

        for (Integer k = 0; k < lcCountPerLocations; k++) {
          channelLocationsBuilder.addChannelLocationList(ChannelLocation.newBuilder()
              .setDevId(CommonDataGeneUtil.extractChar(CommonDataGeneUtil.ExtractType.UPPER, 10))
              .setAccount(CommonDataGeneUtil.extractChar(CommonDataGeneUtil.ExtractType.LOWER, 5) + "@temail")
              .setHostOf(gatewayServer.getIp())
              .setProcessId(gatewayServer.getProcessId())
              .setMqTag("maTag-" + CommonDataGeneUtil.extractChar(CommonDataGeneUtil.ExtractType.LOWER, 5))
              .setMqTopic("maTopic-" + CommonDataGeneUtil.extractChar(CommonDataGeneUtil.ExtractType.LOWER, 5))
              .setProcessId(CommonDataGeneUtil.extractUUID())
              .build());
        }
        ChannelLocations channelLocations = channelLocationsBuilder.build();
        grpcTestUnit.getChannelLocations().add(channelLocations);
        grpcTestUnit.getTemailAccoutLocations().add(transfrom(channelLocations));
      }
      grpcTestUnit.setGatewayServer(gatewayServer);
      grpcTestUnits.add(grpcTestUnit);
    }
    return grpcTestUnits;
  }

  public TemailAccoutLocations transfrom(ChannelLocations channelLocations) {
    List<TemailAccoutLocation> accoutLocations = new ArrayList<>();
    TemailAccoutLocations temailAccoutLocations = new TemailAccoutLocations(accoutLocations);
    for (ChannelLocation channelLocation : channelLocations.getChannelLocationListList()) {
      TemailAccoutLocation temailAccoutLocation = new TemailAccoutLocation();
      temailAccoutLocation.setAccount(channelLocation.getAccount());
      temailAccoutLocation.setDevId(channelLocation.getDevId());
      temailAccoutLocation.setHostOf(channelLocation.getHostOf());
      temailAccoutLocation.setMqTag(channelLocation.getMqTag());
      temailAccoutLocation.setMqTopic(channelLocation.getMqTopic());
      temailAccoutLocation.setProcessId(channelLocation.getProcessId());
    }
    return temailAccoutLocations;
  }

}
