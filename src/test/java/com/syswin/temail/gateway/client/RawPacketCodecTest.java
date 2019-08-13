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
//    byte[] data = cdtpPacket.getData();
    byte[] data = new byte[]{
        0, 0, 2, 89, 0, 0, 0, 101, 0, 2, 2, 33, 10, 54, 98, 55, 49, 99, 101, 49, 49, 50, 45, 99, 99, 98, 102, 45, 52,
        53, 51, 56, 45, 97, 53, 99, 49, 45, 49, 98, 97, 102, 101, 50, 50, 57, 48, 50, 51, 48, 51, 57, 46, 57, 55, 46,
        50, 52, 48, 46, 49, 57, 53, 58, 56, 48, 57, 57, 26, -72, 1, 77, 73, 71, 72, 65, 107, 69, 67, 108, 69, 56, 104,
        82, 84, 72, 121, 77, 89, 80, 117, 48, 50, 87, 99, 50, 106, 95, 71, 52, 108, 88, 101, 71, 89, 80, 122, 67, 72,
        85, 69, 87, 122, 101, 48, 104, 54, 50, 89, 84, 48, 89, 119, 67, 82, 104, 118, 77, 87, 119, 53, 75, 97, 101, 114,
        104, 51, 54, 48, 80, 48, 108, 109, 110, 71, 71, 82, 116, 73, 86, 70, 116, 102, 98, 81, 76, 113, 110, 66, 86,
        100, 86, 99, 67, 65, 74, 67, 65, 101, 50, 66, 88, 99, 75, 119, 51, 97, 70, 108, 119, 82, 69, 73, 78, 78, 81,
        122, 95, 80, 97, 109, 74, 74, 79, 68, 118, 65, 77, 73, 57, 115, 77, 118, 53, 107, 89, 56, 51, 100, 51, 86, 45,
        79, 65, 69, 85, 113, 98, 117, 110, 103, 66, 121, 88, 65, 105, 88, 89, 53, 86, 55, 65, 76, 67, 45, 87, 109, 71,
        105, 65, 52, 48, 57, 65, 90, 74, 74, 48, 100, 79, 53, 113, 119, 101, 69, 41, 90, 101, 82, 93, 0, 0, 0, 0, 50,
        36, 53, 98, 99, 56, 98, 54, 54, 57, 45, 57, 102, 102, 56, 45, 52, 57, 54, 53, 45, 97, 102, 101, 100, 45, 55,
        56, 99, 54, 49, 99, 57, 100, 53, 97, 48, 101, 58, 19, 103, 119, 98, 49, 49, 50, 52, 64, 112, 107, 116, 101, 115,
        116, 54, 46, 99, 111, 109, 66, -45, 1, 77, 73, 71, 98, 77, 66, 65, 71, 66, 121, 113, 71, 83, 77, 52, 57, 65,
        103, 69, 71, 66, 83, 117, 66, 66, 65, 65, 106, 65, 52, 71, 71, 65, 65, 81, 66, 117, 79, 87, 112, 102, 110, 72,
        51, 110, 118, 106, 111, 113, 81, 88, 82, 51, 119, 73, 117, 109, 105, 90, 101, 116, 67, 69, 78, 86, 70, 113, 66,
        118, 113, 98, 55, 78, 72, 78, 120, 101, 87, 122, 108, 83, 111, 99, 121, 85, 50, 104, 88, 88, 80, 80, 116, 53,
        108, 86, 103, 49, 78, 102, 89, 81, 114, 101, 113, 52, 79, 45, 50, 121, 104, 83, 99, 121, 70, 71, 113, 49, 56,
        71, 78, 113, 66, 69, 66, 98, 110, 114, 56, 55, 95, 108, 100, 53, 45, 54, 98, 103, 106, 116, 115, 107, 88, 74,
        100, 75, 106, 71, 77, 101, 114, 99, 120, 97, 54, 49, 78, 82, 54, 48, 121, 69, 79, 95, 55, 56, 116, 112, 90, 69,
        110, 117, 119, 84, 80, 81, 68, 116, 109, 122, 85, 101, 106, 88, 81, 53, 70, 103, 115, 77, 89, 119, 88, 66, 45,
        87, 110, 70, 73, 53, 120, 117, 81, 49, 89, 52, 53, 121, 56, 85, 100, 111, 114, 18, 51, 57, 46, 57, 55, 46, 50,
        52, 48, 46, 49, 57, 53, 58, 56, 48, 57, 57, 18, 7, 97, 110, 100, 114, 111, 105, 100, 26, 5, 49, 46, 48, 46, 48,
        34, 5, 49, 46, 48, 46, 48, 42, 2, 122, 104, 50, 19, 103, 119, 98, 49, 49, 50, 52, 64, 112, 107, 116, 101, 115,
        116, 54, 46, 99, 111, 109};
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


