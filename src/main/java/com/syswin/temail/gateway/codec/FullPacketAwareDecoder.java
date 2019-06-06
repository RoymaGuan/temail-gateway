package com.syswin.temail.gateway.codec;

import static com.syswin.temail.ps.common.Constants.LENGTH_FIELD_LENGTH;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CommandSpaceType;
import com.syswin.temail.ps.common.entity.CommandType;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FullPacketAwareDecoder extends RawPacketDecoder {

  @Override
  protected void readData(ByteBuf byteBuf, CDTPPacket packet, int packetLength, int headerLength) {
    // copy all bytes to data
    byte[] data = null;
    if (packet.getCommandSpace() == CommandSpaceType.CHANNEL_CODE && packet.getCommand() == CommandType.LOGIN
        .getCode()) { //登录请求，则data里面存放CDTPLogin
      data = new byte[byteBuf.readableBytes()];
    } else {
      data = new byte[packetLength + LENGTH_FIELD_LENGTH];
      byteBuf.resetReaderIndex();
    }
    byteBuf.readBytes(data);
    packet.setData(data);
  }
}
