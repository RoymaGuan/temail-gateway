package com.syswin.temail.cdtpserver.handler;

import io.netty.channel.socket.SocketChannel;

import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.google.protobuf.ByteString;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.entity.TransferCDTPPackage;
import com.syswin.temail.cdtpserver.utils.ConstantsAttributeKey;
import com.syswin.temail.cdtpserver.utils.HttpAsyncClient;

public class RequestHandler  extends BaseHandler{

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  
  //@Value("{dispatch.url}")
  private String dispatchUrl = "http://172.31.245.225:8888/dispatch";
  
  public RequestHandler(SocketChannel socketChannel, CDTPPackage cdtpPackage) {
    super(socketChannel, cdtpPackage);
  }

  @Override
  public void process() {
  
    TransferCDTPPackage  transferCDTPPackage = new  TransferCDTPPackage();
    
    BeanUtils.copyProperties(getCdtpPackage(), transferCDTPPackage);
    transferCDTPPackage.setData(getCdtpPackage().getData().toString(Charset.defaultCharset()));
    
    //transferCDTPPackage.setData(getCdtpPackage().getData().toString());
    HttpAsyncClient.post(dispatchUrl, transferCDTPPackage);
    LOGGER.info("execute dispath commond, the temail key is {}",this.getSocketChannel().attr(ConstantsAttributeKey.TEMAIL_KEY).get()); 
  }

}