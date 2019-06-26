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

package com.syswin.temail.gateway.service;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.nio.ByteBuffer;

public class PacketEncoder {

  public byte[] encode(CDTPPacket packet) {
    CDTPHeader header = packet.getHeader();

    byte[] headerBytes;
    if (header != null) {
      headerBytes = header.toProtobufHeader().toByteArray();
    } else {
      headerBytes = new byte[0];
    }

    ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
    byteBuffer
        .putInt(0)
        .putShort(packet.getCommandSpace())
        .putShort(packet.getCommand())
        .putShort(packet.getVersion())
        .putShort((short) headerBytes.length)
        .put(headerBytes)
        .put(packet.getData());

    int packetLength = byteBuffer.position();
    byteBuffer.putInt(0, packetLength - 4);
    byteBuffer.rewind();

    byte[] bytes = new byte[packetLength];
    byteBuffer.get(bytes);
    return bytes;
  }
}
