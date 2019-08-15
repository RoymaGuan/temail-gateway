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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.seanyinx.github.unit.scaffolding.Randomness.uniquify;
import static com.syswin.temail.gateway.client.GatewayIntegrationTest.GATEWAY_PORT;
import static com.syswin.temail.gateway.client.GatewayIntegrationTest.MQ_SERVER_PORT;
import static com.syswin.temail.gateway.client.GatewayIntegrationTest.NAMESRV;
import static com.syswin.temail.gateway.client.GatewayIntegrationTest.SERVICE_PORT;
import static com.syswin.temail.gateway.client.PacketMaker.ackPayload;
import static com.syswin.temail.gateway.client.PacketMaker.loginPacket;
import static com.syswin.temail.gateway.client.PacketMaker.mqMsgPayload;
import static com.syswin.temail.gateway.client.PacketMaker.singleChatPacket;
import static com.syswin.temail.gateway.client.SingleCommandType.SEND_MESSAGE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.waitAtMost;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.syswin.temail.gateway.TemailGatewayApplication;
import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.channels.ChannelsSyncClient;
import com.syswin.temail.gateway.client.GatewayIntegrationTest.ChannelSyncConfig;
import com.syswin.temail.gateway.containers.RocketMqBrokerContainer;
import com.syswin.temail.gateway.containers.RocketMqNameServerContainer;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.gateway.entity.TemailAccoutLocation;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import com.syswin.temail.gateway.service.PacketEncoder;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLoginResp;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.Network;

@Slf4j
@SpringBootTest(classes = {TemailGatewayApplication.class, ChannelSyncConfig.class},
    properties = {
        "app.gateway.verifyUrl=http://localhost:" + SERVICE_PORT + "/verify",
        "app.gateway.dispatchUrl=http://localhost:" + SERVICE_PORT + "/dispatch",
        "app.gateway.updateSocketStatusUrl=http://localhost:" + SERVICE_PORT + "/updateStatus",
        "app.gateway.netty.port=" + GATEWAY_PORT,
        "spring.rocketmq.namesrv-addr=" + NAMESRV + ":" + MQ_SERVER_PORT,
    })
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
// TODO: 2018/11/16 somehow not passing on jenkins
@Ignore
public class GatewayIntegrationTest {

  static final int MQ_SERVER_PORT = 9876;
  static final int GATEWAY_PORT = 8099;
  static final int SERVICE_PORT = 8090;
  static final String NAMESRV = "namesrv";

  @ClassRule
  public static WireMockRule wireMockRule = new WireMockRule(SERVICE_PORT);
  private static final Gson gson = new Gson();
  private static final DefaultMQProducer mqProducer = new DefaultMQProducer(uniquify("test-producer-group"));

  private static final Network NETWORK = Network.newNetwork();
  private static final RocketMqNameServerContainer rocketMqNameSrv = new RocketMqNameServerContainer()
      .withNetwork(NETWORK)
      .withNetworkAliases(NAMESRV)
      .withFixedExposedPort(MQ_SERVER_PORT, MQ_SERVER_PORT);

  private static final RocketMqBrokerContainer rocketMqBroker = new RocketMqBrokerContainer()
      .withNetwork(NETWORK)
      .withEnv("NAMESRV_ADDR", "namesrv:" + MQ_SERVER_PORT)
      .withFixedExposedPort(10909, 10909)
      .withFixedExposedPort(10911, 10911);

  @ClassRule
  public static RuleChain rules = RuleChain.outerRule(rocketMqNameSrv).around(rocketMqBroker);

  private static final String sender = "jack@t.email";
  private static final String receive = "sean@t.email";
  private static final String deviceId = "deviceId";
  private static final String message = "hello world";
  private static final PacketEncoder encoder = new PacketEncoder();
  private static final CDTPPacket loginReqPacket = loginPacket(sender, deviceId);
  private static final CDTPPacket reqPacket = singleChatPacket(sender, receive, message, deviceId);

  private MockNettyClient client;

  @Resource
  private TemailGatewayProperties properties;

  @BeforeClass
  public static void beforeClass() throws MQClientException {
    stubFor(post(urlEqualTo("/verify"))
        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_OCTET_STREAM_VALUE))
        .withRequestBody(binaryEqualTo(encoder.encode(loginReqPacket)))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(OK.value())
                .withBody(gson.toJson(Response.ok())))
    );

    stubFor(any(urlEqualTo("/dispatch"))
        .withHeader(CONTENT_TYPE, equalTo(APPLICATION_OCTET_STREAM_VALUE))
        .withRequestBody(binaryEqualTo(encoder.encode(reqPacket)))
        .willReturn(
            aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(OK.value())
                .withBody(gson.toJson(ackPayload()))));

    stubFor(any(urlEqualTo("/updateStatus"))
        .willReturn(
            aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withStatus(SC_OK)
                .withBody(gson.toJson(Response.ok("Success")))));

    createMqTopic();
  }

  private static void createMqTopic() throws MQClientException {
    mqProducer.setNamesrvAddr(rocketMqNameSrv.getContainerIpAddress() + ":" + MQ_SERVER_PORT);
    mqProducer.start();

    // ensure topic exists before consumer connects, or no message will be received
    waitAtMost(10, SECONDS).until(() -> {
      try {
        mqProducer.createTopic(mqProducer.getCreateTopicKey(), "temail-gateway-notify", 1);
        return true;
      } catch (MQClientException e) {
        e.printStackTrace();
        return false;
      }
    });
  }

  @Before
  public void init() {
    client = new MockNettyClient("127.0.0.1", GATEWAY_PORT);
    client.start();
  }

  @Test
  public void fullCycle() throws Exception {
    // 登录
    CDTPPacket loginRespPacket = client.syncExecute(loginReqPacket);
    CDTPLoginResp cdtpLoginResp = CDTPLoginResp.parseFrom(loginRespPacket.getData());
    assertIs2xxSuccessful(cdtpLoginResp.getCode());

    // 单聊
    CDTPPacket respPacket = client.syncExecute(reqPacket);
    assertThat(respPacket.getCommandSpace()).isEqualTo(SINGLE_MESSAGE_CODE);
    assertThat(respPacket.getCommandSpace()).isEqualTo(SEND_MESSAGE.getCode());
    Response response = gson.fromJson(new String(respPacket.getData()), Response.class);
    assertIs2xxSuccessful(response.getCode());

    // 接收消息
    CDTPPacketTrans packet = mqMsgPayload(sender, message);
    mqProducer.send(new Message(properties.getRocketmq().getMqTopic(), properties.getInstance().getMqTag(),
        gson.toJson(packet).getBytes()), 3000);
    CDTPPacket newResult = client.getNewResult();
    assertThat(newResult.getCommandSpace()).isEqualTo(packet.getCommandSpace());
    assertThat(newResult.getCommand()).isEqualTo(packet.getCommand());
  }

  private void assertIs2xxSuccessful(int code) {
    assertThat(code).isBetween(200, 299);
  }

  @Configuration
  @Profile("dev")
  static class ChannelSyncConfig {

    @Bean
    ChannelsSyncClient client() {
      return new ChannelsSyncClient() {

        @Override
        public void initClient() {

        }

        @Override
        public void destroyClient() {

        }

        @Override
        public boolean syncChannelLocations(TemailAccoutLocations channelLocations) {
          return true;
        }

        @Override
        public boolean removeChannelLocations(TemailAccoutLocations channelLocations) {
          return true;
        }
      };
    }

  }
}
