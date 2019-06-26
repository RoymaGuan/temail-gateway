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

import static com.syswin.temail.gateway.client.PacketMaker.singleChatPacket;
import static com.syswin.temail.gateway.client.SingleCommandType.SEND_MESSAGE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
import static java.util.Collections.singletonMap;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import au.com.dius.pact.consumer.ConsumerPactTestMk2;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public abstract class AbstractDispatchServiceConsumerTest extends ConsumerPactTestMk2 {

  private static final String ackMessage = "Sent ackMessage";
  private final String path = "/dispatch";
  private final Gson gson = new Gson();
  private final String sender = "jack@t.email";
  private final String receiver = "sean@t.email";
  private final String message = "hello world";
  private final String deviceId = "deviceId_5514";
  private final CDTPPacket packet = singleChatPacket(sender, receiver, message, deviceId);
  private final PacketEncoder packetEncoder = new PacketEncoder();
  private volatile Response resultResponse = null;
  private Throwable exception;

  @Override
  public RequestResponsePact createPact(PactDslWithProvider pactDslWithProvider) {
    Map<String, String> headers = new HashMap<>();
    headers.put(CONTENT_TYPE, APPLICATION_OCTET_STREAM_VALUE);

    return pactDslWithProvider
        .given("dispatch user request")
        .uponReceiving("dispatch user request for response")
        .method("POST")
        .body(Base64.getUrlEncoder().encodeToString(packetEncoder.encode(packet)))
        .headers(headers)
        .path(path)
        .willRespondWith()
        .status(200)
        .headers(singletonMap(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE))
        .body(gson.toJson(Response.ok(OK, ackPayload())))
        .toPact();
  }

  @Override
  public void runTest(MockServer mockServer) {
    String url = mockServer.getUrl() + path;
    DispatchService dispatchService = getDispatchService(url);
    dispatchService.dispatch(packetEncoder.encode(packet), new ResponseConsumer(), new ErrorConsumer());

    waitAtMost(2, SECONDS).until(() -> resultResponse != null);
    log.info("result code is {},  msg  is {}", resultResponse.getCode(), resultResponse.getMessage());

    assertThat(resultResponse.getCode()).isEqualTo(OK.value());

    String errorUrl = "http://localhost:99";
    DispatchService errorDispatchService = getDispatchService(errorUrl);
    errorDispatchService.dispatch(packetEncoder.encode(packet), new ResponseConsumer(), new ErrorConsumer());

    waitAtMost(2, SECONDS).until(() -> exception != null);
  }

  protected abstract DispatchService getDispatchService(String url);

  @Override
  protected String providerName() {
    return "temail-dispatcher";
  }

  @Override
  protected String consumerName() {
    return "temail-gateway";
  }

  @NotNull
  private CDTPPacket ackPayload() {
    CDTPPacket payload = new CDTPPacket();
    payload.setCommandSpace(SINGLE_MESSAGE_CODE);
    payload.setCommand(SEND_MESSAGE.getCode());
    payload.setData(gson.toJson(Response.ok(ackMessage)).getBytes());
    return payload;
  }

  private class ResponseConsumer implements Consumer<byte[]> {

    @Override
    public void accept(byte[] bytes) {
      resultResponse = gson.fromJson(new String(bytes), Response.class);
    }
  }

  private class ErrorConsumer implements Consumer<Throwable> {

    @Override
    public void accept(Throwable t) {
      exception = t;
    }
  }
}
