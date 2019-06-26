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

package com.syswin.temail.gateway.http;

import com.syswin.temail.gateway.TemailGatewayProperties;
import java.io.IOException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

public class AsyncHttpCall implements AbstractHttpCall {

  private TemailGatewayProperties temailGatewayProperties;

  private CloseableHttpAsyncClient httpAsyncClient;

  public AsyncHttpCall(TemailGatewayProperties temailGatewayProperties) {
    this.temailGatewayProperties = temailGatewayProperties;
    this.httpAsyncClient = HttpAsyncClientBuilder.create()
        .setMaxConnPerRoute(temailGatewayProperties.getHttpClient().getMaxConnectionsPerRoute())
        .setMaxConnTotal(temailGatewayProperties.getHttpClient().getMaxConnectionsTotal())
        .build();
    ;
  }

  public AsyncHttpCall() {
    this.temailGatewayProperties = temailGatewayProperties;
    this.httpAsyncClient = HttpAsyncClientBuilder.create()
        .setMaxConnPerRoute(100)
        .setMaxConnTotal(500)
        .build();
    ;
  }

  @Override
  public void start() {
    httpAsyncClient.start();
  }

  @Override
  public void close() {
    try {
      httpAsyncClient.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void execute(HttpRequestBase requestBase, FutureCallback futureCallback) {
    httpAsyncClient.execute(requestBase, futureCallback);
  }
}
