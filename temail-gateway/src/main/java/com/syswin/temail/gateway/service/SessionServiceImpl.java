package com.syswin.temail.gateway.service;


import static com.syswin.temail.ps.common.utils.SignatureUtil.resetSignature;

import com.google.protobuf.InvalidProtocolBufferException;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogin;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import com.syswin.temail.ps.server.entity.Session;
import com.syswin.temail.ps.server.service.AbstractSessionService;
import java.util.Collection;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class SessionServiceImpl extends AbstractSessionService {

  private final LoginService loginService;

  private final Consumer<Boolean> responseConsumer = ignored -> {
  };

  private final RemoteStatusService remoteStatusService;

  public SessionServiceImpl(RestTemplate restTemplate, String verifyUrl,
      RemoteStatusService remoteStatusService) {
    this.loginService = new LoginService(restTemplate, verifyUrl);
    this.remoteStatusService = remoteStatusService;
  }

  @Override
  protected boolean loginExt(CDTPPacket reqPacket, CDTPPacket respPacket) {
    String temail = reqPacket.getHeader().getSender();
    String deviceId = reqPacket.getHeader().getDeviceId();
    try {
      if (!StringUtils.hasText(temail) || !StringUtils.hasText(deviceId)) {
        loginFailure(reqPacket, respPacket,
            Response.failed(HttpStatus.BAD_REQUEST, "temail或者deviceId为空！"));
        return false;
      }
      try {
        CDTPLogin cdtpLogin = CDTPLogin.parseFrom(reqPacket.getData());
        log.debug("暂没有用的调试信息", cdtpLogin);
      } catch (InvalidProtocolBufferException e) {
        loginFailure(reqPacket, respPacket,
            Response.failed(HttpStatus.BAD_REQUEST, e.getMessage()));
        return false;
      }
      ResponseEntity<Response> responseEntity = loginService.validSignature(reqPacket);
      Response response = responseEntity.getBody();
      if (responseEntity.getStatusCode().is2xxSuccessful()) {
        loginSuccess(reqPacket, respPacket, response);
        return true;
      } else {
        loginFailure(reqPacket, respPacket, response);
        return false;
      }
    } finally {
      resetSignature(respPacket);
    }
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


  private void loginSuccess(CDTPPacket reqPacket, CDTPPacket respPacket, Response response) {
    String temail = reqPacket.getHeader().getSender();
    String deviceId = reqPacket.getHeader().getDeviceId();
    remoteStatusService.addSession(temail, deviceId, responseConsumer);
    // 返回成功的消息
    CDTPLoginResp.Builder builder = CDTPLoginResp.newBuilder();
    builder.setCode(response == null ? HttpStatus.OK.value() : response.getCode());
    if (response != null && response.getMessage() != null) {
      builder.setDesc(response.getMessage());
    }
    respPacket.setData(builder.build().toByteArray());
  }


  private void loginFailure(CDTPPacket reqPacket, CDTPPacket respPacket, Response response) {
    // 登录失败返回错误消息，然后检查当前通道是否有登录用户，没有则关闭
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
  }
}
