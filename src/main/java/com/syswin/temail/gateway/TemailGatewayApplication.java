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


//@SpringBootApplication
public class TemailGatewayApplication /*extends SpringBootServletInitializer */{

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
