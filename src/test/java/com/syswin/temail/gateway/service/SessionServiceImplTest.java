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

import com.syswin.temail.gateway.client.PacketMaker;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogin;
import com.syswin.temail.ps.server.service.SessionService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;


@Slf4j
public class SessionServiceImplTest {


  private RemoteStatusService remoteStatusService;

  private AuthService authService;

  private SessionService sessionService;


  @Before
  public void setUp() {
    remoteStatusService = mock(RemoteStatusService.class);
    authService = mock(AuthService.class);
    authService.validSignature(new byte[10], x -> {
      log.info("valid success");
    }, x -> {
      log.error("valid failed");
    });
    sessionService = new SessionServiceImpl(authService, remoteStatusService);
  }


  @Test
  public void testLogin() {
    CDTPPacket cdtpPacket = PacketMaker.loginPacket("zhangsan", "ios110");
    Channel channel = Mockito.mock(Channel.class);
    sessionService.login(channel, cdtpPacket);

  }

  @Test
  public void testLoginByEmptyAccount() {
    CDTPPacket cdtpPacket = PacketMaker.loginPacket(null, "ios110");
    Channel channel = Mockito.mock(Channel.class);
    sessionService.login(channel, cdtpPacket);
  }


  @Test
  public void testDisconnectExt() {
    Channel channel = Mockito.mock(Channel.class);
    sessionService.disconnect(channel);
  }


  @Test
  public void testLogout() {
    CDTPPacket cdtpPacket = PacketMaker.logOutPacket("zhangsan", "111");
    Channel channel = Mockito.mock(Channel.class);
    sessionService.logout(channel, cdtpPacket);
  }



  private final byte[] data = new byte[]{0, 0, 2, -59, 0, 9, 0, 2, 0, 2, 2, 81, 10, 64, 52, 65, 66,
      65, 54, 70, 69, 53, 45, 56, 49, 57, 69, 45, 52, 57, 56, 50, 45, 57, 51, 57, 51, 45, 48, 56,
      48, 54, 57, 49, 68, 50, 49, 55, 52, 56, 109, 115, 103, 115, 101, 97, 108, 46, 115, 121, 115,
      116, 111, 111, 110, 116, 101, 115, 116, 46, 99, 111, 109, 58, 56, 48, 57, 57, 26, -73, 1, 77,
      73, 71, 71, 65, 107, 70, 50, 54, 99, 76, 81, 103, 45, 76, 115, 52, 49, 73, 111, 52, 95, 82,
      110, 75, 86, 108, 85, 72, 57, 54, 103, 56, 121, 82, 75, 67, 54, 82, 78, 83, 95, 80, 85, 49,
      122, 54, 95, 119, 111, 116, 54, 77, 53, 77, 111, 52, 66, 45, 105, 119, 86, 75, 100, 56, 119,
      111, 67, 51, 108, 66, 98, 52, 45, 71, 72, 116, 109, 83, 81, 85, 118, 102, 118, 100, 66, 83,
      118, 50, 119, 112, 102, 84, 81, 74, 66, 77, 84, 119, 105, 77, 57, 84, 70, 121, 81, 49, 68,
      110, 119, 87, 121, 119, 57, 78, 100, 67, 53, 119, 111, 122, 114, 77, 85, 73, 97, 106, 85, 52,
      119, 95, 108, 108, 103, 87, 51, 109, 45, 84, 97, 51, 116, 111, 51, 122, 81, 97, 74, 54, 120,
      50, 104, 98, 104, 74, 108, 87, 88, 105, 121, 107, 54, 87, 99, 52, 118, 105, 98, 122, 116, 90,
      100, 112, 45, 122, 80, 97, 121, 50, 65, 103, 82, 89, 41, -105, -110, -1, 92, 0, 0, 0, 0, 50,
      36, 52, 65, 66, 65, 54, 70, 69, 53, 45, 56, 49, 57, 69, 45, 52, 57, 56, 50, 45, 57, 51, 57,
      51, 45, 48, 56, 48, 54, 57, 49, 68, 50, 49, 55, 52, 56, 58, 23, 97, 97, 97, 49, 49, 49, 49,
      64, 115, 121, 115, 116, 111, 111, 110, 116, 101, 115, 116, 46, 99, 111, 109, 66, -45, 1, 77,
      73, 71, 98, 77, 66, 65, 71, 66, 121, 113, 71, 83, 77, 52, 57, 65, 103, 69, 71, 66, 83, 117,
      66, 66, 65, 65, 106, 65, 52, 71, 71, 65, 65, 81, 65, 68, 76, 107, 114, 68, 71, 98, 55, 89, 86,
      117, 73, 118, 82, 86, 57, 53, 72, 112, 106, 103, 67, 120, 110, 116, 90, 72, 49, 121, 68, 118,
      51, 111, 118, 110, 110, 56, 114, 109, 82, 85, 104, 57, 71, 71, 108, 80, 53, 119, 99, 104, 68,
      68, 98, 72, 73, 119, 86, 52, 99, 89, 48, 49, 115, 102, 65, 78, 100, 110, 103, 48, 53, 103,
      104, 66, 84, 106, 55, 53, 56, 45, 73, 84, 87, 88, 116, 81, 66, 80, 120, 87, 80, 67, 88, 108,
      121, 79, 71, 57, 70, 77, 97, 117, 85, 116, 110, 45, 106, 84, 90, 55, 87, 109, 81, 88, 69, 75,
      112, 118, 107, 95, 76, 79, 69, 67, 67, 102, 72, 72, 108, 84, 95, 110, 87, 112, 56, 51, 110,
      69, 85, 70, 102, 98, 53, 118, 48, 104, 54, 102, 114, 83, 66, 101, 67, 53, 115, 83, 50, 55, 85,
      51, 71, 112, 107, 109, 100, 108, 87, 81, 71, 48, 101, 105, 101, 69, 74, 23, 97, 97, 97, 49,
      49, 49, 49, 64, 115, 121, 115, 116, 111, 111, 110, 116, 101, 115, 116, 46, 99, 111, 109, 114,
      28, 109, 115, 103, 115, 101, 97, 108, 46, 115, 121, 115, 116, 111, 111, 110, 116, 101, 115,
      116, 46, 99, 111, 109, 58, 56, 48, 57, 57, 123, 10, 9, 34, 113, 117, 101, 114, 121, 34, 32,
      58, 32, 10, 9, 123, 10, 9, 9, 34, 116, 101, 109, 97, 105, 108, 34, 32, 58, 32, 34, 97, 97, 97,
      49, 49, 49, 49, 64, 115, 121, 115, 116, 111, 111, 110, 116, 101, 115, 116, 46, 99, 111, 109,
      34, 44, 10, 9, 9, 34, 117, 115, 101, 114, 75, 101, 121, 34, 32, 58, 32, 34, 100, 105, 115,
      112, 108, 97, 121, 95, 110, 97, 109, 101, 34, 44, 10, 9, 9, 34, 118, 101, 114, 115, 105, 111,
      110, 34, 32, 58, 32, 48, 10, 9, 125, 10, 125, 10};


  private final CDTPPacket cdtpLogin = new CDTPPacket();

  @Test
  public void go() {
    cdtpLogin.setData(data);
    //this.getPlatform(cdtpLogin);
  }

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
    } catch (Exception ex) {
      log.error("parse platform error !!! , the packet is {}", cdtpPacket.getHeader().toString(),
          ex);
    }
    return null;
  }


}