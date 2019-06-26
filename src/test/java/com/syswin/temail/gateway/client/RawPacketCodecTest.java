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

package com.syswin.temail.gateway.client;

import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.gateway.codec.FullPacketAwareDecoder;
import com.syswin.temail.gateway.codec.RawPacketDecoder;
import com.syswin.temail.gateway.codec.RawPacketEncoder;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogin;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginOrBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class RawPacketCodecTest {

  private final ChannelId channelId = Mockito.mock(ChannelId.class);
  private final Channel channel = Mockito.mock(Channel.class);
  private final ChannelHandlerContext context = Mockito.mock(ChannelHandlerContext.class);
  private final String sender = "sender@t.email";
  private final String deviceId = uniquify("deviceId");

  private final List<Object> packets = new ArrayList<>();
  private final RawPacketDecoder decoder = new FullPacketAwareDecoder();
  private final RawPacketEncoder encoder = new RawPacketEncoder();
  private final CDTPPacket packet = PacketMaker.singleChatPacket(sender, "recipient", "hello world", deviceId);
  private final ByteBuf buffer = Unpooled.buffer();

  @Before
  public void setUp() {
    when(context.channel()).thenReturn(channel);
    when(channel.id()).thenReturn(channelId);
  }

  @Test
  public void shouldDecodePacketWithFullPayloadBytes() {
    ByteBuf bufferIncludeLength = Unpooled.buffer();

    encoder.encode(context, packet, buffer);
    bufferIncludeLength.writeInt(buffer.readableBytes());
    bufferIncludeLength.writeBytes(buffer.retain());

    decoder.decode(context, bufferIncludeLength, packets);

    assertThat(packets).isNotEmpty();
    CDTPPacket decodedPacket = ((CDTPPacket) packets.get(0));
    assertThat(decodedPacket.getCommandSpace()).isEqualTo(packet.getCommandSpace());
    assertThat(decodedPacket.getCommand()).isEqualTo(packet.getCommand());
    assertThat(decodedPacket.getHeader().getDeviceId()).isEqualTo(packet.getHeader().getDeviceId());
    assertThat(decodedPacket.getHeader().getSender()).isEqualTo(packet.getHeader().getSender());

    packets.clear();
    buffer.clear();

    // data contains full packet payload
    buffer.writeBytes(decodedPacket.getData());

    decoder.decode(context, buffer, packets);

    assertThat(packets).isNotEmpty();
    assertThat(packets.get(0)).isEqualToIgnoringGivenFields(packet, "data");
  }

  @Test
  public void decodeLoginPacket() {
    CDTPPacket cdtpPacket = PacketMaker.loginPacket("zhangnsa", "deviceId");
    ByteBuf bufferIncludeLength = Unpooled.buffer();

    encoder.encode(context, cdtpPacket, buffer);
    bufferIncludeLength.writeInt(buffer.readableBytes());
    bufferIncludeLength.writeBytes(buffer.retain());

    decoder.decode(context, bufferIncludeLength, packets);
    CDTPPacket p = (CDTPPacket) packets.get(0);
    String platform = getPlatform(p);
    assertThat("ios/android/pc".equals(platform));
  }


  //com.syswin.temail.gateway.service.SessionServiceImpl.getPlatform
  private String getPlatform(CDTPPacket cdtpPacket) {
    byte[] data = cdtpPacket.getData();
    ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
    byteBuf.skipBytes(10);// Length(int) + COMMAND_SPACE(short)+COMMOAND(short)+VERSION(short)
    short e = byteBuf.readShort();
    byteBuf.skipBytes(e);
    byte[] cdtpLoginBytes = new byte[byteBuf.readableBytes()];
    byteBuf.readBytes(cdtpLoginBytes);
    try {
      CDTPLogin login = CDTPLogin.parseFrom(cdtpLoginBytes);
      return login.getPlatform();
    } catch (InvalidProtocolBufferException ex) {
      ex.printStackTrace();
    }
    return null;
  }

}


