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
