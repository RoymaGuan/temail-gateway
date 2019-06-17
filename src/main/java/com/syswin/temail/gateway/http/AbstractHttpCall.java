package com.syswin.temail.gateway.http;

import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.concurrent.FutureCallback;

public interface AbstractHttpCall {

  void start();

  void close();

  void execute(HttpRequestBase requestBase, FutureCallback futureCallback);

}
