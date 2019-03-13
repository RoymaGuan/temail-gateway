package com.syswin.temail.gateway.service;

import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;
import com.syswin.temail.gateway.http.AbstractHttpCall;
import com.syswin.temail.gateway.http.AsyncHttpCall;
import java.util.Base64;
import org.apache.http.entity.StringEntity;

public class DispatchServiceHttpClientAsyncConsumerTest extends AbstractDispatchServiceConsumerTest {

  private final AbstractHttpCall asyncClient = new AsyncHttpCall();

  protected DispatchService getDispatchService(String url) {
    asyncClient.start();
    return new DispatchServiceHttpClientAsync(url,
        asyncClient,
        bytes -> new StringEntity(Base64.getUrlEncoder().encodeToString(bytes), APPLICATION_OCTET_STREAM));
  }
}
