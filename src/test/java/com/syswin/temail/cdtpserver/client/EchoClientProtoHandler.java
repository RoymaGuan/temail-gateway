package com.syswin.temail.cdtpserver.client;

import java.nio.charset.Charset;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.syswin.temail.cdtpserver.entity.TemailInfo;
import com.syswin.temail.cdtpserver.entity.CDTPPackageProto.CDTPPackage;
import com.syswin.temail.cdtpserver.utils.CommandEnum;
import com.syswin.temail.cdtpserver.utils.ConstantsAttributeKey;

/**
 * Created by weis on 18/8/3.
 */
@ChannelHandler.Sharable
public class EchoClientProtoHandler extends ChannelInboundHandlerAdapter{
    int  counter = 0;
    private Gson gson = new Gson();
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //当被通知Channel是活跃的时候，发送一条消息
//        ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
      CDTPPackage.Builder builder = CDTPPackage.newBuilder();
      builder.setAlgorithm(1);
      //builder.setCommand(2);
      builder.setCommand(CommandEnum.connect.getCode());
      builder.setPkgId("pckAgeId1234");
      builder.setVersion(3);
      
      TemailInfo temailInfo = new TemailInfo();
      temailInfo.setTemail("sean@t.email");
      temailInfo.setDevId("devId");
      Gson gson = new Gson();
      String gsonString = gson.toJson(temailInfo);
      
      builder.setData(ByteString.copyFrom(gsonString, Charset.defaultCharset()));     
      CDTPPackage ctPackage = builder.build();
      
      ctx.writeAndFlush(ctPackage);
      }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
      System.out.println("client  attrKey:"+ctx.channel().attr(ConstantsAttributeKey.TEMAIL_KEY).get());
      if(counter ==0){
      //if(counter <=2){
        if(msg instanceof CDTPPackage){
          System.out.println("msg:"+msg);
          counter++;
          CDTPPackage.Builder builder = CDTPPackage.newBuilder();
          builder.setAlgorithm(11);
          builder.setCommand(CommandEnum.ping.getCode());
          /*if(counter==2){
            builder.setCommand(CommandEnum.disconnect.getCode());
          }
          else{
            builder.setCommand(CommandEnum.ping.getCode());
          }*/
          
          builder.setVersion(13);
          
          CDTPPackage ctPackage = builder.build();
          //System.out.println("yyyyyy");
          ctx.writeAndFlush(ctPackage);
        } 
      }
      else{
        System.out.println("no send pinginfo ");
      }
           

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
