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


import static com.syswin.temail.ps.server.utils.SignatureUtil.resetSignature;

import com.syswin.temail.gateway.entity.AccountInfo;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogin;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.ps.common.entity.CommandSpaceType;
import com.syswin.temail.ps.common.entity.CommandType;
import com.syswin.temail.ps.server.entity.Session;
import com.syswin.temail.ps.server.service.AbstractSessionService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

@Slf4j
public class SessionServiceImpl extends AbstractSessionService {

  private final AuthService authService;

  private final Consumer<Boolean> responseConsumer = ignored -> {
  };

  private final RemoteStatusService remoteStatusService;

  public SessionServiceImpl(AuthService authService, RemoteStatusService remoteStatusService) {
    this.authService = authService;
    this.remoteStatusService = remoteStatusService;
  }

  @Override
  protected void loginExtAsync(CDTPPacket reqPacket,
      Function<CDTPPacket, Collection<Session>> successHandler,
      Consumer<CDTPPacket> failedHandler) {
    String temail = reqPacket.getHeader().getSender();
    String deviceId = reqPacket.getHeader().getDeviceId();
    log.debug("loin packet is {}. ", reqPacket);
    if (!StringUtils.hasText(temail) || !StringUtils.hasText(deviceId)) {
      CDTPPacket respPacket = loginFailure(reqPacket,
          Response.failed(HttpStatus.BAD_REQUEST, "temail or deviceId is null！"));
      failedHandler.accept(respPacket);
      return;
    }
    authService.validSignature(reqPacket.getData(),
        response -> {
          CDTPPacket respPacket = loginSuccess(reqPacket, response);
          Collection<Session> sessions = successHandler.apply(respPacket);
          remoteStatusService.removeSessions(sessions, t -> {
          });
        },
        response -> {
          CDTPPacket respPacket = loginFailure(reqPacket, response);
          resetSignature(respPacket);
          failedHandler.accept(respPacket);
        });
  }

  @Override
  protected void logoutExt(CDTPPacket reqPacket, CDTPPacket respPacket) {
    String temail = reqPacket.getHeader().getSender();
    String deviceId = reqPacket.getHeader().getDeviceId();
    remoteStatusService.removeSession(temail, deviceId, responseConsumer);
    super.logoutExt(reqPacket, respPacket);
    resetSignature(respPacket);
  }

  /**
   * 空闲或者异常退出
   *
   * @param sessions 用户连接通道
   */
  @Override
  protected void disconnectExt(Collection<Session> sessions) {
    remoteStatusService.removeSessions(sessions, responseConsumer);
  }

  private CDTPPacket loginSuccess(CDTPPacket reqPacket, Response response) {
    CDTPPacket respPacket = new CDTPPacket(reqPacket);
    String temail = reqPacket.getHeader().getSender();
    String deviceId = reqPacket.getHeader().getDeviceId();
    AccountInfo accountInfo = getLoginInfoFromCdtpPacket(reqPacket);
    remoteStatusService
        .addSession(new AccountInfo(temail, deviceId, accountInfo.getPlatform(), accountInfo.getAppVer()),
            responseConsumer);
    // 返回成功的消息
    CDTPLoginResp.Builder builder = CDTPLoginResp.newBuilder();
    builder.setCode(response == null ? HttpStatus.OK.value() : response.getCode());
    if (response != null && response.getMessage() != null) {
      builder.setDesc(response.getMessage());
    }
    respPacket.setData(builder.build().toByteArray());
    resetSignature(respPacket);
    return respPacket;
  }


  /**
   * @param cdtpPacket ths cdtp packet
   */
  private AccountInfo getLoginInfoFromCdtpPacket(CDTPPacket cdtpPacket) {
    AccountInfo accountInfo = new AccountInfo();
    if (cdtpPacket.getCommandSpace() == CommandSpaceType.CHANNEL_CODE &&
        cdtpPacket.getCommand() == CommandType.LOGIN.getCode()) {
      try {
        byte[] data = cdtpPacket.getData();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
        byteBuf.skipBytes(10);// Length(int) + COMMAND_SPACE(short)+COMMOAND(short)+VERSION(short)
        short e = byteBuf.readShort();
        byteBuf.skipBytes(e);
        byte[] cdtpLoginBytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(cdtpLoginBytes);
        CDTPLogin login = CDTPLogin.parseFrom(cdtpLoginBytes);
        String platform = login.getPlatform();
        if (platform != null) {
          accountInfo.setPlatform(platform);
        }
        String appVer = login.getAppVer();
        if (appVer != null) {
          accountInfo.setAppVer(appVer);
        }
        log.info("parsed login info is {}", accountInfo);
      } catch (Exception ex) {
        log.info("parse login info error !!! , the packet is {}", cdtpPacket,
            ex);
      }
    }
    return accountInfo;
  }

  private CDTPPacket loginFailure(CDTPPacket reqPacket, Response response) {
    CDTPPacket respPacket = new CDTPPacket(reqPacket);
    // 登录失败返回错误消息
    CDTPLoginResp.Builder builder = CDTPLoginResp.newBuilder();
    if (response != null) {
      if (response.getCode() != null) {
        builder.setCode(response.getCode());
      } else {
        builder.setCode(HttpStatus.FORBIDDEN.value());
      }
      if (response.getMessage() != null) {
        builder.setDesc(response.getMessage());
      }
    } else {
      builder.setCode(HttpStatus.FORBIDDEN.value());
    }
    respPacket.setData(builder.build().toByteArray());
    resetSignature(respPacket);
    return respPacket;
  }
}