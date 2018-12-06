package com.syswin.temail.gateway.notify;

import static com.syswin.temail.gateway.client.PacketMaker.mqMsgPayload;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.MessagePactProviderRule;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.model.v3.messaging.MessagePact;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.server.service.channels.strategy.ChannelManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;


@Slf4j
public class MessageHandlerConsumerTest {

  @Rule
  public final MessagePactProviderRule mockProvider = new MessagePactProviderRule(this);

  private final ChannelPromise promise = Mockito.mock(ChannelPromise.class);
  private final Channel channel = Mockito.mock(Channel.class);
  private final ChannelManager channelHolder = Mockito.mock(ChannelManager.class);

  private final String recipient = "sean@t.email";
  private final CDTPPacketTrans payload = mqMsgPayload(recipient, "bonjour");
  private byte[] currentMessage;

  @Pact(provider = "temail-dispatcher-mq", consumer = "temail-gateway-mq")
  public MessagePact createPact(MessagePactBuilder builder) {
    PactDslJsonBody header = packetJson(payload, bodyJson(payload));

    Map<String, String> metadata = new HashMap<>();
    metadata.put("contentType", "application/json");

    return builder.given("Notification service is available")
        .expectsToReceive("online notification")
        .withMetadata(metadata)
        .withContent(header)
        .toPact();
  }

  @Test
  @PactVerification({"Able to process online notification message"})
  public void test() {
    when(channel.voidPromise()).thenReturn(promise);
    when(channelHolder.getChannelsExceptSender(recipient,"deviceId1")).thenReturn(singletonList(channel));

    MessageHandler messageHandler = new MessageHandler(channelHolder);

    String msg = new String(currentMessage);
    messageHandler.onMessageReceived(msg);

    verify(channel).writeAndFlush(argThat(matchesPayload(payload)), same(promise));
  }

  public void setMessage(byte[] messageContents) {
    currentMessage = messageContents;
  }

  private ArgumentMatcher<CDTPPacket> matchesPayload(CDTPPacketTrans payload) {
    return packet -> {

      assertThat(packet).isEqualToIgnoringGivenFields(payload, "header", "data");
      assertThat(new String(packet.getData())).isEqualTo(payload.getData());
      assertThat(packet.getHeader()).isEqualToIgnoringGivenFields(payload.getHeader(), "packetId", "signature");
      assertThat(packet.getHeader().getSignature()).isNull();
      assertThat(packet.getHeader().getPacketId()).isNotEmpty();
      return true;
    };
  }

  private PactDslJsonBody packetJson(CDTPPacketTrans cdtpPacketTrans, PactDslJsonBody body) {
    PactDslJsonBody header = new PactDslJsonBody("header", "", body);

    setStringIfNotNull(header, "deviceId", cdtpPacketTrans.getHeader().getDeviceId());
    header.numberValue("signatureAlgorithm", cdtpPacketTrans.getHeader().getSignatureAlgorithm());
    setStringIfNotNull(header, "signature", cdtpPacketTrans.getHeader().getSignature());
    header.numberValue("dataEncryptionMethod", cdtpPacketTrans.getHeader().getDataEncryptionMethod());
    header.numberValue("timestamp", cdtpPacketTrans.getHeader().getTimestamp());
    setStringIfNotNull(header, "packetId", cdtpPacketTrans.getHeader().getPacketId());
    setStringIfNotNull(header, "sender", cdtpPacketTrans.getHeader().getSender());
    setStringIfNotNull(header, "senderPK", cdtpPacketTrans.getHeader().getSenderPK());
    setStringIfNotNull(header, "receiver", cdtpPacketTrans.getHeader().getReceiver());
    setStringIfNotNull(header, "receiverPK", cdtpPacketTrans.getHeader().getReceiverPK());
    setStringIfNotNull(header, "at", cdtpPacketTrans.getHeader().getAt());
    setStringIfNotNull(header, "topic", cdtpPacketTrans.getHeader().getTopic());
    setStringIfNotNull(header, "extraData", cdtpPacketTrans.getHeader().getExtraData());
    return header;
  }

  private void setStringIfNotNull(PactDslJsonBody header, String key, String value) {
    if (value != null) {
      header.stringValue(key, value);
    }
  }

  private PactDslJsonBody bodyJson(CDTPPacketTrans cdtpPacketTrans) {
    PactDslJsonBody body = new PactDslJsonBody();
    body.numberValue("commandSpace", cdtpPacketTrans.getCommandSpace());
    body.numberValue("command", cdtpPacketTrans.getCommand());
    body.numberValue("version", cdtpPacketTrans.getVersion());
    body.stringValue("data", cdtpPacketTrans.getData());
    return body;
  }
}