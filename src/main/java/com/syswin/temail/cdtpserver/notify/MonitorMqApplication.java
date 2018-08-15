package com.syswin.temail.cdtpserver.notify;

import java.lang.invoke.MethodHandles;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.syswin.temail.cdtpserver.config.TemailServerConfig;

@Component
@Order(2)
public class MonitorMqApplication implements ApplicationRunner {
  private static final Logger LOGGER = LoggerFactory
      .getLogger(MethodHandles.lookup().lookupClass());
  
  /*@Value("${login.verifyUrl}")  
  private String  verifyUrl;*/
  
  @Resource
  private  TemailServerConfig  temailServerConfig;
  
  @Override
  public void run(ApplicationArguments args) throws Exception {
    LOGGER.info("启动线程监听MQ");
    
    System.out.println("verifyUrl:"+temailServerConfig.getVerifyUrl());
    
     MonitorMqRunnable  monitorMqRunnable =new   MonitorMqRunnable();
     monitorMqRunnable.setTemailServerConfig(temailServerConfig);
     Thread monitorThread = new Thread(monitorMqRunnable, "monitorThread") ;
     monitorThread.start();

  }

}
