package com.syswin.temail.gateway.service;

import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;
import com.syswin.temail.gateway.http.AbstractHttpCall;
import com.syswin.temail.gateway.http.AsyncHttpCall;
import java.util.Base64;
import org.apache.http.entity.StringEntity;

public class AuthServiceHttpClientAsncConsumerTest extends AbstractAuthServiceConsumerTest {
  private final AbstractHttpCall asyncClient = new AsyncHttpCall();

  protected AuthService getAuthService(String url) {
    asyncClient.start();
    return new AuthServiceHttpClientAsync(url,
        asyncClient,
        bytes -> new StringEntity(Base64.getUrlEncoder().encodeToString(bytes), APPLICATION_OCTET_STREAM));
  }
}