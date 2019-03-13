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
