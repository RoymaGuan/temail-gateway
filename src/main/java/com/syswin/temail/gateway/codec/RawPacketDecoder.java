package com.syswin.temail.gateway.codec;

import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf;
import com.syswin.temail.ps.common.exception.PacketException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class RawPacketDecoder extends ByteToMessageDecoder {

  @Override
  public void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
    CDTPPacket packet = new CDTPPacket();

    byteBuf.markReaderIndex();
    int packetLength = readPacketLength(byteBuf);

    readCommandSpace(byteBuf, packet);
    readCommand(byteBuf, packet);
    readVersion(byteBuf, packet);
    short headerLength = readHeader(byteBuf, packet);

    readData(byteBuf, packet, packetLength, headerLength);

    list.add(packet);
    if (!packet.isHeartbeat() && log.isDebugEnabled()) {
      log.info("From channel:{} read packet：CommandSpace={},Command={},CDTPHeader={},data={}",
          ctx.channel(),
          Integer.toHexString(packet.getCommandSpace()),
          Integer.toHexString(packet.getCommand()),
          packet.getHeader(),
          Arrays.toString(packet.getData() != null ? packet.getData() : new byte[]{}));
    }
  }

  private int readPacketLength(ByteBuf byteBuf) {
    return byteBuf.readInt();
  }

  private void readCommandSpace(ByteBuf byteBuf, CDTPPacket packet) {
    short commandSpace = byteBuf.readShort();
    if (commandSpace < 0) {
      throw new PacketException("CommandSpace must be positive, but actual was " + commandSpace);
    }
    packet.setCommandSpace(commandSpace);
  }

  private void readCommand(ByteBuf byteBuf, CDTPPacket packet) {
    short command = byteBuf.readShort();
    if (command <= 0) {
      throw new PacketException("Command must be positive, but actual was " + command);
    }
    packet.setCommand(command);
  }

  private void readVersion(ByteBuf byteBuf, CDTPPacket packet) {
    short version = byteBuf.readShort();
    packet.setVersion(version);
  }

  private short readHeader(ByteBuf byteBuf, CDTPPacket packet) {
    short headerLength = byteBuf.readShort();
    CDTPProtoBuf.CDTPHeader cdtpHeader;
    if (headerLength < 0) {
      throw new PacketException("Negative headerLength: " + headerLength);
    }

    if (headerLength > 0) {
      if (byteBuf.readableBytes() < headerLength) {
        throw new PacketException("The packet length is less than headerLength, headerLength=" + headerLength
            + ", packetLength=" + byteBuf.readableBytes());
      }
      byte[] headerBytes = new byte[headerLength];
      byteBuf.readBytes(headerBytes);
      try {
        cdtpHeader = CDTPProtoBuf.CDTPHeader.parseFrom(headerBytes);
      } catch (InvalidProtocolBufferException e) {
        log.error("Failed to decode packet", e);
        throw new PacketException("Packet decode error" + e.getMessage());
      }
      packet.setHeader(new CDTPHeader(cdtpHeader));
    }
    return headerLength;
  }

  protected abstract void readData(ByteBuf byteBuf, CDTPPacket packet, int packetLength, int headerLength);
}
