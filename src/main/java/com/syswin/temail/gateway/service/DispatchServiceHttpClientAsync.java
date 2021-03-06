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

import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;
import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

@Slf4j
public class DispatchServiceHttpClientAsync implements DispatchService {

  private final Gson gson = new Gson();
  private final HttpAsyncClient asyncClient;
  private final String dispatchUrl;
  private final Function<byte[], HttpEntity> httpEntitySupplier;

  DispatchServiceHttpClientAsync(String dispatchUrl,
      HttpAsyncClient asyncClient,
      Function<byte[], HttpEntity> httpEntitySupplier) {

    this.asyncClient = asyncClient;
    this.dispatchUrl = dispatchUrl;
    this.httpEntitySupplier = httpEntitySupplier;
  }

  public DispatchServiceHttpClientAsync(String dispatchUrl, HttpAsyncClient asyncClient) {
    this(dispatchUrl, asyncClient, bytes -> new ByteArrayEntity(bytes, APPLICATION_OCTET_STREAM));
  }

  @Override
  public void dispatch(byte[] payload, Consumer<byte[]> consumer,
      Consumer<? super Throwable> errorConsumer) {
    HttpEntity bodyEntity = httpEntitySupplier.apply(payload);
    HttpPost request = new HttpPost(dispatchUrl);
    request.setEntity(bodyEntity);
    asyncClient.execute(request, new FutureCallback<HttpResponse>() {
      @Override
      public void completed(HttpResponse result) {
        try {
          byte[] resp = EntityUtils.toByteArray(result.getEntity());
          log.debug("asyncClient fetch httpStatus:{}, response: {}",
              result.getStatusLine().getStatusCode(),
              new String(resp, StandardCharsets.UTF_8));

          if (JSONStrUtil.isJSONBytes(resp)) {
            log.debug("common consume response.");
            consumer.accept(resp);

          } else {
            //rebuild it to a json
            log.debug("rebuilt response to a json.");
            consumer.accept(gson.toJson(Response.failed(
                HttpStatus.valueOf(result.getStatusLine().getStatusCode()),
                new String(resp, StandardCharsets.UTF_8))).getBytes());

          }
        } catch (IOException e) {
          errorConsumer.accept(e);
        }
      }

      @Override
      public void failed(Exception ex) {
        errorConsumer.accept(ex);
      }

      @Override
      public void cancelled() {
        throw new UnsupportedOperationException("unsupported operation");
      }
    });
  }
}
