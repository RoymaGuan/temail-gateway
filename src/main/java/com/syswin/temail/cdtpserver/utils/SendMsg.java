package com.syswin.temail.cdtpserver.utils;

import com.syswin.temail.cdtpserver.entity.CDTPPackageProto;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by weis on 18/8/9.
 */
public class SendMsg {

    /**
     * 发送心跳命令
     * @param socketChannel
     * @param command 心跳类型   ping   pong
     */
    public static void sendHeartbeat(SocketChannel socketChannel,CommandEnum command){
        CDTPPackageProto.CDTPPackage.Builder builder = CDTPPackageProto.CDTPPackage.newBuilder();
        builder.setCommand(command.getCode());//设置心跳命令

        CDTPPackageProto.CDTPPackage cdtpPackage = builder.build();

        socketChannel.writeAndFlush(cdtpPackage);
    }


}
