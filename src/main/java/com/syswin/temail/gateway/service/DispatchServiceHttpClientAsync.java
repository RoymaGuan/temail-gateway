package com.syswin.temail.gateway.service;

import static org.apache.http.entity.ContentType.APPLICATION_OCTET_STREAM;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.util.EntityUtils;

public class DispatchServiceHttpClientAsync implements DispatchService {

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
          consumer.accept(EntityUtils.toByteArray(result.getEntity()));
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
