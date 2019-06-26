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

import static com.syswin.temail.ps.common.Constants.CDTP_VERSION;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.CHANNEL_CODE;
import static com.syswin.temail.ps.common.entity.CommandSpaceType.SINGLE_MESSAGE_CODE;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import com.syswin.temail.ps.common.entity.CDTPPacketTrans;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogin;
import com.syswin.temail.ps.common.entity.CDTPProtoBuf.CDTPLogin.Builder;
import com.syswin.temail.ps.common.entity.CommandSpaceType;
import com.syswin.temail.ps.common.entity.CommandType;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketMaker {

  private static final Gson gson = new Gson();

  // 创建单聊消息体
  public static CDTPPacket singleChatPacket(String sender, String recipient, String message, String deviceId) {

    CDTPPacket packet = new CDTPPacket();
    packet.setCommandSpace(CommandSpaceType.SINGLE_MESSAGE_CODE);
    packet.setCommand(SingleCommandType.SEND_MESSAGE.getCode());
    packet.setVersion(CDTP_VERSION);

    CDTPHeader header = new CDTPHeader();
    header.setSignatureAlgorithm(1);
    header.setSignature("sign");
    header.setDataEncryptionMethod(0);
    header.setTimestamp(1535713173935L);
    header.setPacketId("pkgId");
    header.setDeviceId(deviceId);
    header.setSender(sender);
    header.setReceiver(recipient);
    header.setSenderPK("SenderPK123");
    header.setReceiverPK("ReceiverPK456");
    Map<String, Object> extraData = new HashMap<>();
    extraData.put("from", sender);
    extraData.put("to", recipient);
    extraData.put("storeType", "2");
    extraData.put("type", "0");
    extraData.put("msgId", "4298F38F87DC4775B264A3753E77B443");
    header.setExtraData(gson.toJson(extraData));
    packet.setHeader(header);

    packet.setData(message.getBytes());

    return packet;
  }

  public static CDTPPacket loginPacket(String sender, String deviceId) {
    CDTPPacket packet = new CDTPPacket();
    CDTPHeader header = new CDTPHeader();
    header.setDeviceId(deviceId);
    header.setSignatureAlgorithm(1);
    header.setTimestamp(1535713173935L);
    header.setDataEncryptionMethod(0);
    header.setPacketId("PacketId12345");
    header.setSender(sender);
    header.setSenderPK("SenderPK");
//    header.setReceiver("sean@t.email");
//    header.setReceiverPK("ReceiverPK");

    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(CommandType.LOGIN.getCode());
    packet.setVersion(CDTP_VERSION);
    packet.setHeader(header);

    Builder builder = CDTPLogin.newBuilder();

//    builder.setdevId("设备ID");
    builder.setPushToken("推送token");
    builder.setPlatform("ios/android/pc");
    builder.setOsVer("11.4");
    builder.setAppVer("1.0.0");
    builder.setLang("en、ch-zn...");
    builder.setTemail("请求发起方的temail地址");
    builder.setChl("渠道号");
    CDTPLogin cdtpLogin = builder.build();

    packet.setData(cdtpLogin.toByteArray());
    return packet;
  }


  public static CDTPPacket logOutPacket(String sender, String deviceId) {
    CDTPPacket packet = new CDTPPacket();
    CDTPHeader header = new CDTPHeader();
    header.setDeviceId(deviceId);
    header.setSignatureAlgorithm(1);
    header.setTimestamp(1535713173935L);
    header.setDataEncryptionMethod(0);
    header.setPacketId("PacketId12345");
    header.setSender(sender);
    header.setSenderPK("SenderPK");
//    header.setReceiver("sean@t.email");
//    header.setReceiverPK("ReceiverPK");

    packet.setCommandSpace(CHANNEL_CODE);
    packet.setCommand(CommandType.LOGOUT.getCode());
    packet.setVersion(CDTP_VERSION);
    packet.setHeader(header);

    Builder builder = CDTPLogin.newBuilder();

//    builder.setdevId("设备ID");
    builder.setPushToken("推送token");
    builder.setPlatform("ios/android/pc");
    builder.setOsVer("11.4");
    builder.setAppVer("1.0.0");
    builder.setLang("en、ch-zn...");
    builder.setTemail("请求发起方的temail地址");
    builder.setChl("渠道号");
    CDTPLogin cdtpLogin = builder.build();

    packet.setData(cdtpLogin.toByteArray());
    return packet;
  }

  public static Response ackPayload() {
    Map<String, Object> msgData = new HashMap<>();
    msgData.put("msgId", "消息ID");
    msgData.put("seqId", "消息序列号");
    return Response.ok(msgData);
  }

  public static CDTPPacketTrans mqMsgPayload(String recipient, String message) {
    Response<String> body = Response.ok(message);
    CDTPPacket payload = new CDTPPacket();
    payload.setCommandSpace(SINGLE_MESSAGE_CODE);
    payload.setCommand(SINGLE_MESSAGE_CODE);
    payload.setVersion(CDTP_VERSION);
    CDTPHeader header = new CDTPHeader();
    header.setReceiver(recipient);
    header.setDeviceId("deviceId1");
    header.setSender("jack@.email");
    header.setSignature(UUID.randomUUID().toString());
    payload.setHeader(header);
    payload.setData(Base64.getUrlEncoder().encode(gson.toJson(body).getBytes()));
    return new CDTPPacketTrans(payload);
  }

}
