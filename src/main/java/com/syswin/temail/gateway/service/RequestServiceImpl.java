package com.syswin.temail.gateway.service;

import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandType.INTERNAL_ERROR;
import static com.syswin.temail.ps.server.utils.SignatureUtil.resetSignature;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPServerError;
import com.syswin.temail.ps.server.service.RequestService;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestServiceImpl implements RequestService {

  private final DispatchService dispatchService;

  public RequestServiceImpl(DispatchService dispatchService) {
    this.dispatchService = dispatchService;
  }

  @Override
  public void handleRequest(CDTPPacket reqPacket, Consumer<CDTPPacket> responseHandler) {
    dispatchService.dispatch(reqPacket.getData(),
        bytes -> {
          // 后台正常返回
          reqPacket.setData(bytes);
          resetSignature(reqPacket);
          responseHandler.accept(reqPacket);
        },
        t -> {
          log.error("fail to call dispatcher server. ", t);
          errorPacket(reqPacket, INTERNAL_ERROR.getCode(), t.getMessage());
          resetSignature(reqPacket);
          responseHandler.accept(reqPacket);
        });
  }

  private void errorPacket(CDTPPacket packet, int code, String message) {
    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(INTERNAL_ERROR.getCode());

    CDTPServerError.Builder builder = CDTPServerError.newBuilder();
    builder.setCode(code);
    builder.setDesc(message);
    packet.setData(builder.build().toByteArray());
  }
}
