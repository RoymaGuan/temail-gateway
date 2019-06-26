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

import static com.syswin.temail.ps.common.Constants.LENGTH_FIELD_LENGTH;

import com.syswin.temail.gateway.codec.RawPacketDecoder;
import com.syswin.temail.gateway.codec.RawPacketEncoder;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;

class MockNettyClient {

  private YHCClientResponseHandler responseHandler;
  private final String host;
  private final int port;
  private Channel channel;

  public MockNettyClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void start() {
    responseHandler = new YHCClientResponseHandler();

    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap bootstrap = new Bootstrap();
    bootstrap.group(group)
        .channel(NioSocketChannel.class)
        .remoteAddress(new InetSocketAddress(host, port))
        .handler(new ChannelInitializer<SocketChannel>() {
          @Override
          protected void initChannel(SocketChannel socketChannel) {
            socketChannel.pipeline()
                .addLast("IdleHandler", new IdleStateHandler(0, 180, 0))
                .addLast("lengthFieldBasedFrameDecoder",
                    new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, LENGTH_FIELD_LENGTH, 0, 0))
                .addLast("lengthFieldPrepender",
                    new LengthFieldPrepender(LENGTH_FIELD_LENGTH, 0, false))
                .addLast(new PacketDecoder())
                .addLast(new RawPacketEncoder())
                .addLast(responseHandler);
          }
        });
    ChannelFuture future = bootstrap.connect().syncUninterruptibly();
    channel = future.channel();
    Runtime.getRuntime().addShutdownHook(new Thread(group::shutdownGracefully));
  }

  public CDTPPacket syncExecute(CDTPPacket reqPacket) {
    return syncExecute(reqPacket, 30, TimeUnit.SECONDS);
  }

  public CDTPPacket syncExecute(CDTPPacket reqPacket, long timeout, TimeUnit timeUnit) {
    try {
      CountDownLatch latch = new CountDownLatch(1);
      responseHandler.resetLatch(latch);
      channel.writeAndFlush(reqPacket);
      latch.await(timeout, timeUnit);
      return responseHandler.getResult();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public CDTPPacket getNewResult() {
    return getNewResult(30, TimeUnit.SECONDS);
  }

  public CDTPPacket getNewResult(long timeout, TimeUnit timeUnit) {
    Awaitility.await().atMost(timeout, timeUnit).until(() -> responseHandler.isNewResult());
    return responseHandler.getResult();
  }

  private static class PacketDecoder extends RawPacketDecoder {

    @Override
    protected void readData(ByteBuf byteBuf, CDTPPacket packet, int packetLength, int headerLength) {
      // copy only remaining bytes to data
      byte[] data = new byte[packetLength - headerLength - 8];
      byteBuf.readBytes(data);
      packet.setData(data);
    }
  }
}
