package com.syswin.temail.gateway.notify;

import com.syswin.temail.gateway.TemailGatewayProperties;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

/**
 * @author 姚华成
 * @date 2018-11-07
 */
public class RocketGatewayMqConsumeRunner implements GatewayMQConsumeRunner, ApplicationRunner, Ordered {

  private final RocketMqConsumer consumer;

  public RocketGatewayMqConsumeRunner(TemailGatewayProperties properties,
      MessageHandlerTemplate messageHandlerTemplate) {
    consumer = new RocketMqConsumer(properties,
        new TemailServerMqListener(messageHandlerTemplate));
  }

  @Override
  public void run(ApplicationArguments args) throws MQClientException {
    consumer.start();
    Runtime.getRuntime().addShutdownHook(new Thread(consumer::stop));
  }

  @Override
  public int getOrder() {
    return 2;
  }
}
