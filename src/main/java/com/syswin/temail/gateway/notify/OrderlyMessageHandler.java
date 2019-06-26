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

package com.syswin.temail.gateway.notify;

import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.server.service.channels.strategy.ChannelManager;
import io.netty.channel.Channel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class OrderlyMessageHandler extends MessageHandlerTemplate {

  private final BlockingQueue<CDTPPacket> packetLocalCache = new LinkedBlockingQueue<>();

  private final ExecutorService consumerPacketService = Executors.newFixedThreadPool(1);

  private final AtomicBoolean isTaskStartUp = new AtomicBoolean(false);

  private final Consumer<CDTPPacket> callBackAfterWrite;

  public OrderlyMessageHandler(ChannelManager channelHolder, Consumer<CDTPPacket> callBackAfterWrite) {
    super(channelHolder);
    this.callBackAfterWrite = callBackAfterWrite;
    this.beginWriteBackTask();
  }

  private void beginWriteBackTask() {
    if (isTaskStartUp.compareAndSet(false, true)) {
      this.consumerPacketService.submit(new Runnable() {
        @Override
        public void run() {
          log.info("Start packet async write back process.");
          while (!Thread.currentThread().isInterrupted()) {
            try {
              CDTPPacket packet = packetLocalCache.take();
              CDTPHeader header = packet.getHeader();
              String receiver = header.getReceiver();
              Iterable<Channel> channels = getChannelHolder()
                  .getChannelsExceptSenderN(receiver, header.getSender(),
                      header.getDeviceId());

              for (Channel channel : channels) {
                log.info("Write MQ message:{} to channel：{}", packet, channel);
                channel.writeAndFlush(packet, channel.voidPromise());
                if (callBackAfterWrite != null) {
                  callBackAfterWrite.accept(packet);
                }
              }
            } catch (InterruptedException e) {
              log.error("Fail to orderly consume packet! ", e);
              Thread.currentThread().interrupt();
            } catch (Exception e) {
              log.error("Fail to orderly consume packet! ", e);
            }
          }
        }
      });
    }
  }

  @Override
  public void writeBackPacket(CDTPPacket packet) {
    try {
      this.packetLocalCache.put(packet);
    } catch (InterruptedException e) {
      log.error("Fail to cache packet: {}", packet.toString());
    }
  }

  public boolean isPendingQueueIsEmpty() {
    return this.packetLocalCache.isEmpty();
  }


}
