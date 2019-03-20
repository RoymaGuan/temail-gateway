package com.syswin.temail.gateway.notify;


import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.server.service.channels.strategy.ChannelManager;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConcurrentlyMessageHandler extends MessageHandlerTemplate {

  public ConcurrentlyMessageHandler(ChannelManager channelHolder) {
    super(channelHolder);
  }

  @Override
  public void writeBackPacket(CDTPPacket packet) {
    CDTPHeader header = packet.getHeader();
    String receiver = header.getReceiver();
    Iterable<Channel> channels = super.getChannelHolder()
        .getChannelsExceptSenderN(receiver, header.getSender(), header.getDeviceId());
    for (Channel channel : channels) {
      log.info("Wrote MQ message:{} to channel：{}", packet, channel);
      channel.writeAndFlush(packet, channel.voidPromise());
    }
  }

}
