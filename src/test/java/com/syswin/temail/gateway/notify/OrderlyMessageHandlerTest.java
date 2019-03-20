package com.syswin.temail.gateway.notify;

import com.syswin.temail.gateway.client.PacketMaker;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.server.service.channels.strategy.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

@Slf4j
public class OrderlyMessageHandlerTest {

  private OrderlyMessageHandler orderlyMessageHandler;
  private final Random random = new Random(27);
  private final ExecutorService executorService = Executors.newFixedThreadPool(1);
  private final ChannelManager channelManager = Mockito.mock(ChannelManager.class);

  private final CDTPPacket cdtpPacket = PacketMaker.singleChatPacket(
      "we", "are", "hello", "20e9u20u9202j");

  private final String packetMsg = "{\"commandSpace\":3,\"command\":3,\"version\":1,\"header\":{\"deviceId\":\"4baea9ea\",\"signatureAlgorithm\":0,\"signature\":\"MIGHAkE3uwcpeLg5I_a-ux5jjY4zruwp2RUKXJRwGzlZjBKqrIDfhwIOXTZ6oGa9nqwUSSxl5Zlm42u-EiOfOYX0FyNHrgJCAOTtGsBcF3Vo2RiyxNxltkoShdKYH86XBeQdCOa6NWFB5947RXlSr9bnGme12PoPtaECrcGPTW2KASIS79IraHoB\",\"dataEncryptionMethod\":0,\"timestamp\":1553070258,\"packetId\":\"0CD76A5A-59DF-BC8B-1F39-4AB13734\",\"sender\":\"zhang040302@systoontest.com\",\"senderPK\":\"MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQB8GeiGxbFGB9LX0H6WcWohiBrJQSmfjMF6adzzWKIKJX5m6F_3OdgTd_Jeq-Vwps4QJ6eCRiAVv2BAXWgWQlhcwcAqYb8Qogaqgtcp_Vpwn-hb09tBn6cWLgyhvroj0Zr3y3iDhZ907WSXncVyVVCyyfKBQZHZX8Li1W-8SQ695Qcn3Y\",\"receiver\":\"zhang040302@systoontest.com\",\"receiverPK\":\"MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQB8GeiGxbFGB9LX0H6WcWohiBrJQSmfjMF6adzzWKIKJX5m6F_3OdgTd_Jeq-Vwps4QJ6eCRiAVv2BAXWgWQlhcwcAqYb8Qogaqgtcp_Vpwn-hb09tBn6cWLgyhvroj0Zr3y3iDhZ907WSXncVyVVCyyfKBQZHZX8Li1W-8SQ695Qcn3Y\",\"targetAddress\":\"msgseal.systoontest.com:8099\"},\"data\":\"{\\\"eventSeqId\\\":592,\\\"eventType\\\":-1,\\\"from\\\":\\\"qazwsx@systoontest.com\\\",\\\"to\\\":\\\"zhang040302@systoontest.com\\\",\\\"timestamp\\\":1553070259491}\"} ";

  private AtomicInteger packetAriteBackTimes = new AtomicInteger(0);
  private final Channel channel1 = Mockito.mock(Channel.class);
  private final Channel channel2 = Mockito.mock(Channel.class);
  private final Channel channel3 = Mockito.mock(Channel.class);
  private final ChannelFuture channelFuture = Mockito.mock(ChannelFuture.class);
  private final List<Channel> channels = ImmutableList.of(channel1, channel2, channel3);

  @Before
  public void init() {
    Mockito.when(channelManager.getChannelsExceptSenderN(Mockito.anyString(),
        Mockito.anyString(), Mockito.anyString())).thenReturn(channels);
    Mockito.when(channel1.writeAndFlush(Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
      TimeUnit.MILLISECONDS.sleep(random.nextInt(10));
      return channelFuture;
    });
    Mockito.when(channel2.writeAndFlush(Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
      TimeUnit.MILLISECONDS.sleep(random.nextInt(10));
      return channelFuture;
    });
    Mockito.when(channel3.writeAndFlush(Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
      TimeUnit.MILLISECONDS.sleep(random.nextInt(10));
      return channelFuture;
    });
    this.orderlyMessageHandler = new OrderlyMessageHandler(channelManager, p -> {
      log.info("ready to consume packet: {}", p.toString());
      this.packetAriteBackTimes.incrementAndGet();
    });
  }

  @Test
  public void oneTaskExecuteWell() throws InterruptedException {
    this.orderlyMessageHandler.onMessageReceived(packetMsg);

    Awaitility.waitAtMost(2, TimeUnit.SECONDS).until(()
        -> this.packetAriteBackTimes.get() == 1 * channels.size());

    Awaitility.waitAtMost(2, TimeUnit.SECONDS).until(()
        -> this.orderlyMessageHandler.isPendingQueueIsEmpty() == true);

  }

  @Test
  public void manyTasksExecuteWell() {
    int totalTasks = 1000;
    this.executorService.submit(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < totalTasks; i++) {
          orderlyMessageHandler.onMessageReceived(packetMsg);
        }
      }
    });

    Awaitility.waitAtMost((long) (totalTasks * 20 * channels.size() / 1.5), TimeUnit.MILLISECONDS).until(()
        -> this.packetAriteBackTimes.get() == totalTasks * channels.size());

    Awaitility.waitAtMost((long) (totalTasks * 20 * channels.size() / 1.5), TimeUnit.MILLISECONDS).until(()
        -> this.orderlyMessageHandler.isPendingQueueIsEmpty() == true);

  }

}