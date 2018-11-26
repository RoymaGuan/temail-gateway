package com.syswin.temail.gateway.client;

import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.CountDownLatch;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 姚华成
 * @date 2018-8-25
 */
@Slf4j
public class YHCClientResponseHandler extends SimpleChannelInboundHandler<CDTPPacket> {

  @Setter
  private CountDownLatch latch;
  private CDTPPacket result;
  @Getter
  private volatile boolean newResult;

  public CDTPPacket getResult() {
    newResult = false;
    return result;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, CDTPPacket packet) {
    log.info("packet received from server is：{}", packet);
    newResult = true;
    result = packet;
  }

  public void resetLatch(CountDownLatch initLatch) {
    this.latch = initLatch;
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    super.channelReadComplete(ctx);
    if (latch != null) {
      latch.countDown();
    }
  }

}
