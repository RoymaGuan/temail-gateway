package com.syswin.temail.gateway.service;


import static com.syswin.temail.ps.server.utils.SignatureUtil.resetSignature;

import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogin;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.ps.server.entity.Session;
import com.syswin.temail.ps.server.service.AbstractSessionService;
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
  protected void loginExtAsync(CDTPPacket reqPacket, Function<CDTPPacket, Collection<Session>> successHandler,
      Consumer<CDTPPacket> failedHandler) {
    String temail = reqPacket.getHeader().getSender();
    String deviceId = reqPacket.getHeader().getDeviceId();
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
    String platform = getPlatform(reqPacket);
    remoteStatusService.addSession(temail, deviceId, platform, responseConsumer);
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

  private String getPlatform(CDTPPacket cdtpPacket) {
    try {
      byte[] data = cdtpPacket.getData();
      CDTPLogin cdtpLogin = CDTPLogin.parseFrom(data);
      return cdtpLogin.getPlatform();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    return null;
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
