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
