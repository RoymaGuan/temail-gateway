package com.syswin.temail.gateway.client;

import com.syswin.temail.gateway.entity.CDTPPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientResponseHandler extends SimpleChannelInboundHandler<CDTPPacket> {

  private final BlockingQueue<CDTPPacket> receivedMessages;
  private final Supplier<CDTPPacket> loginSupplier;

  @Getter
  private CDTPPacket result;

  public ClientResponseHandler(Supplier<CDTPPacket> loginSupplier) {
    this.loginSupplier = loginSupplier;
    this.receivedMessages = new LinkedBlockingQueue<>();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, CDTPPacket packet) {
    log.info("从服务器端收到的信息：{}", packet);
    result = packet;
    receivedMessages.offer(packet);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    CDTPPacket packet = loginSupplier.get();
    log.info("Channel active, sending login packet {}", packet);
    ctx.writeAndFlush(packet);
  }

  public BlockingQueue<CDTPPacket> receivedMessages() {
    return receivedMessages;
  }
}
