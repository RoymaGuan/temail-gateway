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
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

public class AuthServiceHttpClientAsync implements AuthService {

  private final HttpAsyncClient asyncClient;
  private final String authUrl;
  private final Function<byte[], HttpEntity> httpEntitySupplier;
  private final Gson gson = new Gson();

  public AuthServiceHttpClientAsync(String authUrl, HttpAsyncClient asyncClient) {
    this(authUrl, asyncClient, bytes -> new ByteArrayEntity(bytes, APPLICATION_OCTET_STREAM));
  }

  AuthServiceHttpClientAsync(String authUrl,
      HttpAsyncClient asyncClient,
      Function<byte[], HttpEntity> httpEntitySupplier) {
    this.asyncClient = asyncClient;
    this.authUrl = authUrl;
    this.httpEntitySupplier = httpEntitySupplier;
  }

  @Override
  public void validSignature(byte[] payload, Consumer<Response> successConsumer,
      Consumer<Response> failedConsumer) {

    HttpEntity bodyEntity = httpEntitySupplier.apply(payload);
    HttpPost request = new HttpPost(authUrl);
    request.setEntity(bodyEntity);

    asyncClient.execute(request, new FutureCallback<HttpResponse>() {
      @Override
      public void completed(HttpResponse result) {
        try {
          String responseJson = new String(EntityUtils.toByteArray(result.getEntity()), StandardCharsets.UTF_8);
          Response response = gson.fromJson(responseJson, Response.class);
          int statusCode = result.getStatusLine().getStatusCode();
          if (statusCode >= 200 && statusCode <= 299) {
            successConsumer.accept(response);
          } else {
            failedConsumer.accept(response);
          }
        } catch (IOException e) {
          failedConsumer.accept(Response.failed(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
      }

      @Override
      public void failed(Exception e) {
        failedConsumer.accept(Response.failed(HttpStatus.BAD_REQUEST, e.getMessage()));
      }

      @Override
      public void cancelled() {
        throw new UnsupportedOperationException("unsupported operation");
      }
    });
  }
}
