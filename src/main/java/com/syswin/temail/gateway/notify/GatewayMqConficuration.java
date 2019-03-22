package com.syswin.temail.gateway.notify;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.ps.server.service.channels.strategy.ChannelManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 姚华成
 * @date 2018-11-07
 */
@Slf4j
@Configuration
public class GatewayMqConficuration {

  private DefaultMQPushConsumer consumer;

  @Bean
  DefaultMQPushConsumer defaultMQPushConsumer(TemailGatewayProperties properties,
      ChannelManager channelManager) throws MQClientException {
    String consumerGroup = properties.getRocketmq().getConsumerGroup() + "-"
        + properties.getInstance().getHostOf() + "-"
        + properties.getInstance().getProcessId();

    this.consumer = new DefaultMQPushConsumer(consumerGroup);
    TemailGatewayProperties.Rocketmq rocketmq = properties.getRocketmq();
    consumer.setNamesrvAddr(rocketmq.getNamesrvAddr());
    consumer.subscribe(rocketmq.getMqTopic(), properties.getInstance().getMqTag());
    consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
    consumer.setMessageListener(new TemailServerMqListener(new OrderlyMessageHandler(channelManager, t->{})));
    consumer.start();

    log.info("MQ consumer listener is initialized. Listening on mqTopic:{}, mqTag:{}",
        rocketmq.getMqTopic(), properties.getInstance().getMqTag());
    Runtime.getRuntime().addShutdownHook(new Thread(consumer::shutdown));
    return consumer;
  }

  //@Bean
  //MqProducerConfig producerConfig(String group) {
  //  return new MqProducerConfig(group);
  //}

  //@Bean
  //MqConsumerConfig consumerConfig(TemailGatewayProperties properties, ChannelManager channelManager) {
  //  OrderlyMessageHandler orderlyMessageHandler = new OrderlyMessageHandler(channelManager,t->{});
  //  TemailGatewayProperties.Rocketmq rocketmq = properties.getRocketmq();
  //  return MqConsumerConfig.create().group(rocketmq.getConsumerGroup()).topic(rocketmq.getMqTopic())
  //      .tag(properties.getInstance().getMqTag()).sequential().implementation(MqImplementation.ROCKET_MQ)
  //      .listener(orderlyMessageHandler::onMessageReceived).build();
  //}

}
