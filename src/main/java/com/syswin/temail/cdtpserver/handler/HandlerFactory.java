package com.syswin.temail.cdtpserver.handler;

import javax.annotation.Resource;

import io.netty.channel.socket.SocketChannel;

import org.springframework.stereotype.Component;

import com.syswin.temail.cdtpserver.TemailServerProperties;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import com.syswin.temail.cdtpserver.entity.CommandEnum;

/**
 * Created by weis on 18/8/8.
 */
@Component
public class HandlerFactory {

    @Resource
    TemailServerProperties temailServerConfig;
    
    public BaseHandler getHandler(CDTPPackageProto.CDTPPackage cdtpPackage, SocketChannel socketChannel){         
        if(cdtpPackage.getCommand() == CommandEnum.connect.getCode()){
            return new LoginHandler(socketChannel,cdtpPackage, temailServerConfig);
        }
        else if(cdtpPackage.getCommand() == CommandEnum.disconnect.getCode()){
          return new DisconnectHandler(socketChannel,cdtpPackage, temailServerConfig);
        }
        else{
            return new RequestHandler(socketChannel, cdtpPackage, temailServerConfig); 
        }       
    }
}
