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

    if (!packet.isHeartbeat()) {
      log.info("To channel: {} write data：CommandSpace={},Command={},CDTPHeader={},Data={}",
          ctx.channel(),
          Integer.toHexString(packet.getCommandSpace()),
          Integer.toHexString(packet.getCommand()),
          packet.getHeader(),
          new String(packet.getData()));
    }
  }
}
