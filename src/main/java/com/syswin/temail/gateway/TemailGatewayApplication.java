package com.syswin.temail.gateway;

import com.syswin.temail.gateway.channels.ChannelsSyncClient;
import com.syswin.temail.gateway.channels.clients.grpc.GrpcClientWrapper;
import com.syswin.temail.gateway.codec.FullPacketAwareDecoder;
import com.syswin.temail.gateway.codec.RawPacketEncoder;
import com.syswin.temail.gateway.http.AbstractHttpCall;
import com.syswin.temail.gateway.http.AsyncHttpCall;
import com.syswin.temail.gateway.notify.RocketMqRunner;
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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;


@SpringBootApplication
public class TemailGatewayApplication {

  public static void main(String[] args) {
    SpringApplication.run(TemailGatewayApplication.class, args);
  }

  @Bean(initMethod = "start", destroyMethod = "close")
  AbstractHttpCall asyncClient(TemailGatewayProperties properties) {
    return new AsyncHttpCall(properties);
  }

  @Profile("!dev")
  @Bean(initMethod = "initClient", destroyMethod = "destroyClient")
  public ChannelsSyncClient initGrpcClient(TemailGatewayProperties properties) {
    return new GrpcClientWrapper(properties);
  }

  @Bean
  public AuthService loginService(TemailGatewayProperties properties, AbstractHttpCall asyncClient) {
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
  public DispatchService dispatchService(TemailGatewayProperties properties, AbstractHttpCall asyncClient) {
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

  @Bean
  public RocketMqRunner rocketMqRunner(TemailGatewayProperties properties, ChannelManager channelHolder) {
    return new RocketMqRunner(properties, channelHolder);
  }
}
