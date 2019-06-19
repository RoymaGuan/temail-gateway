package com.syswin.temail.gateway.notify;

import static com.syswin.temail.ps.server.utils.SignatureUtil.resetSignature;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.server.service.channels.strategy.ChannelManager;
import java.util.UUID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class MessageHandlerTemplate {

  private final ChannelManager channelHolder;

  private final Gson gson;

  public MessageHandlerTemplate(ChannelManager channelHolder) {
    this.channelHolder = channelHolder;
    this.gson = new GsonBuilder()
        .registerTypeAdapter(CDTPPacket.class, new PacketDeserializer())
        .create();
  }

  public void onMessageReceived(String message) {
    try {
      log.debug("Received message: {} from MQ.", message);
      CDTPPacket packet = gson.fromJson(message, CDTPPacket.class);
      CDTPHeader header = packet.getHeader();

      // 对于通知消息，重新生成packetId，避免跟请求的返回消息重复而产生错误
      String newPacketId = UUID.randomUUID().toString();
      log.info("update packet id . packetId is {} , new packet id is {}", header.getPacketId(), newPacketId);
      header.setPacketId(newPacketId);
      resetSignature(packet);
      writeBackPacket(packet);

    } catch (JsonSyntaxException e) {
      log.error("Failed to parse MQ message：{}", message, e);
    }
  }

  public abstract void writeBackPacket(CDTPPacket packet);

}
