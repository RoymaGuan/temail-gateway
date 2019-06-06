package com.syswin.temail.gateway.codec;

import static com.syswin.temail.ps.common.Constants.LENGTH_FIELD_LENGTH;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FullPacketAwareDecoder extends RawPacketDecoder {

  @Override
  protected void readData(ByteBuf byteBuf, CDTPPacket packet, int packetLength, int headerLength) {
    // copy all bytes to data
    byte[] data = new byte[packetLength + LENGTH_FIELD_LENGTH];
    byteBuf.resetReaderIndex();
    byteBuf.readBytes(data);
    packet.setData(data);
  }
}
