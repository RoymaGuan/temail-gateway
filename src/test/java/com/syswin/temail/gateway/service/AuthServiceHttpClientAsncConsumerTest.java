package com.syswin.temail.gateway.service;

import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;

import java.util.Base64;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

public class AuthServiceHttpClientAsncConsumerTest extends AbstractAuthServiceConsumerTest {
  private final CloseableHttpAsyncClient asyncClient = HttpAsyncClientBuilder.create().build();

  protected AuthService getAuthService(String url) {
    asyncClient.start();
    return new AuthServiceHttpClientAsync(url,
        asyncClient,
        bytes -> new StringEntity(Base64.getUrlEncoder().encodeToString(bytes), APPLICATION_OCTET_STREAM));
  }
}