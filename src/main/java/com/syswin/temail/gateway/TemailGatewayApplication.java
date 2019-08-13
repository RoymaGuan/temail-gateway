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

package com.syswin.temail.gateway;

import com.syswin.temail.gateway.channels.ChannelsSyncClient;
import com.syswin.temail.gateway.channels.clients.grpc.GrpcClientWrapper;
import com.syswin.temail.gateway.codec.FullPacketAwareDecoder;
import com.syswin.temail.gateway.codec.RawPacketEncoder;
import com.syswin.temail.gateway.service.AuthService;
import com.syswin.temail.gateway.service.AuthServiceHttpClientAsync;
import com.syswin.temail.gateway.service.DispatchService;
import com.syswin.temail.gateway.service.DispatchServiceHttpClientAsync;
import com.syswin.temail.gateway.service.RemoteStatusService;
import com.syswin.temail.gateway.service.RequestServiceImpl;
import com.syswin.temail.gateway.service.SessionServiceImpl;
import com.syswin.temail.ps.server.GatewayServer;
import com.syswin.temail.ps.server.service.AbstractSessionService;
import com.syswin.temail.ps.server.service.RequestService;
import com.syswin.temail.ps.server.service.channels.strategy.ChannelManager;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.client.HttpAsyncClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;


public class TemailGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(TemailGatewayApplication.class, args);
  }


  @Bean(initMethod = "start", destroyMethod = "close")
  HttpAsyncClient asyncClient(TemailGatewayProperties properties) {
    return HttpAsyncClientBuilder.create()
        .setMaxConnPerRoute(properties.getHttpClient().getMaxConnectionsPerRoute())
        .setMaxConnTotal(properties.getHttpClient().getMaxConnectionsTotal())
        .build();
  }

  @Profile("!dev")
  @Bean(initMethod = "initClient", destroyMethod = "destroyClient")
  public ChannelsSyncClient initGrpcClient(TemailGatewayProperties properties) {
    return new GrpcClientWrapper(properties);
  }

  @Bean
  public AuthService loginService(TemailGatewayProperties properties, HttpAsyncClient asyncClient) {
    return new AuthServiceHttpClientAsync(properties.getVerifyUrl(), asyncClient);
  }

  @Bean
  public AbstractSessionService sessionService(TemailGatewayProperties properties, AuthService authService,
      ChannelsSyncClient channelsSyncClient) {
    return new SessionServiceImpl(authService,
        new RemoteStatusService(properties, channelsSyncClient));
  }

  @Bean
  ChannelManager channelHolder(AbstractSessionService sessionService) {
    return sessionService.getChannelHolder();
  }

  @Bean
  public DispatchService dispatchService(TemailGatewayProperties properties, HttpAsyncClient asyncClient) {
    return new DispatchServiceHttpClientAsync(properties.getDispatchUrl(), asyncClient);
  }

  @Bean
  public RequestService requestService(DispatchService dispatchService) {
    return new RequestServiceImpl(dispatchService);
  }

  @Bean
  TemailGatewayRunner gatewayRunner(TemailGatewayProperties properties,
      AbstractSessionService sessionService,
      RequestService requestService) {
    return new TemailGatewayRunner(
        new GatewayServer(
            sessionService,
            requestService,
            RawPacketEncoder::new,
            FullPacketAwareDecoder::new,
            properties.getNetty().getPort(),
            properties.getNetty().getReadIdleTimeSeconds(),
            properties.getNetty().isEpollEnabled()));
  }

}
