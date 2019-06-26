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
}
