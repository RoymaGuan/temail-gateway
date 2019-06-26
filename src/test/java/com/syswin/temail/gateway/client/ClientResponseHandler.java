/*
 * MIT License
 *
 * Copyright (c) 2019 Syswin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.syswin.temail.gateway.client;

import com.syswin.temail.ps.common.entity.CDTPPacket;
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
    log.debug("receive packet: {} from server", packet);
    result = packet;
    receivedMessages.offer(packet);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) {
    CDTPPacket packet = loginSupplier.get();
    log.debug("Channel active, sending login pack {}", packet);
    ctx.writeAndFlush(packet);
  }

  public BlockingQueue<CDTPPacket> receivedMessages() {
    return receivedMessages;
  }
}
