package com.syswin.temail.gateway.codec;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RawPacketEncoder extends MessageToByteEncoder<CDTPPacket> {

  @Override
  public void encode(ChannelHandlerContext ctx, CDTPPacket packet, ByteBuf byteBuf) {
    CDTPHeader header = packet.getHeader();

    byte[] headerBytes;
    if (header != null) {
      headerBytes = header.toProtobufHeader().toByteArray();
    } else {
      headerBytes = new byte[0];
    }

    byteBuf.writeShort(packet.getCommandSpace());
    byteBuf.writeShort(packet.getCommand());
    byteBuf.writeShort(packet.getVersion());
    byteBuf.writeShort(headerBytes.length);
    byteBuf.writeBytes(headerBytes);
    byteBuf.writeBytes(packet.getData());

    if (!packet.isHeartbeat() && log.isDebugEnabled()) {
      log.debug("To channel: {} write data：CommandSpace={},Command={},CDTPHeader={},Data={}",
          ctx.channel(),
          Integer.toHexString(packet.getCommandSpace()),
          Integer.toHexString(packet.getCommand()),
          packet.getHeader(),
          new String(packet.getData()));
    }
  }
}
