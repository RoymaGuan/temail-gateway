package com.syswin.temail.gateway.http;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;

public interface AbstractHttpCall {

  public void start();

  public void close();

  public void execute(HttpRequestBase requestBase, FutureCallback futureCallback);

}
