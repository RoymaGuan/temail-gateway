package com.syswin.temail.gateway.notify;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

@Slf4j
class TemailServerMqListener implements MessageListenerConcurrently {

  private final MessageHandler messageHandler;

  TemailServerMqListener(MessageHandler messageHandler) {
    this.messageHandler = messageHandler;
  }

  @Override
  public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages,
      ConsumeConcurrentlyContext context) {
    try {
      for (MessageExt msg : messages) {
        messageHandler.onMessageReceived(new String(msg.getBody()));
      }
      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    } catch (Exception ex) {
      log.error("Fail to write MQ message to channels, message：{}", messages, ex);
      return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
  }

}
