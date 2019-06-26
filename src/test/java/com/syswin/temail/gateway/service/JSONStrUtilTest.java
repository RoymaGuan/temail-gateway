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

package com.syswin.temail.gateway.service;

import com.google.gson.Gson;
import com.syswin.temail.gateway.entity.Response;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

@Slf4j
public class JSONStrUtilTest {

  private static final Gson GSON = new Gson();

  private static String oriStr = null;

  @BeforeClass
  public static void init() {
    Map map = new HashMap();
    map.put("name", "StanLi");
    map.put("job", "writer");
    map.put("ags", "93");
    oriStr = GSON.toJson(map);
    log.info("initStr : {}", oriStr);
  }

  @Test
  public void trueIfOristr() {
    Assertions.assertThat(JSONStrUtil.isJSONStr(oriStr)).isTrue();
  }

  @Test
  public void falseIfChangeBegin() {
    Assertions.assertThat(JSONStrUtil.isJSONStr("s" + oriStr)).isFalse();
  }

  @Test
  public void falseIfChangeEnd() {
    Assertions.assertThat(JSONStrUtil.isJSONStr(oriStr + "s")).isFalse();
  }

  @Test
  public void falseIfBothChannge() {
    Assertions.assertThat(JSONStrUtil.isJSONStr("a" + oriStr + "s")).isFalse();
  }

  @Test
  public void trueEvenLineBreaker() {
    String string = "{\"name\":\"StanLi\", \r\n \"job\":\"writer\",\"ags\":\"93\"}";
    Assertions.assertThat(JSONStrUtil.isJSONStr(string)).isTrue();
  }

  @Test
  public void responseToJson() {
    Response response = Response.failed(HttpStatus.GATEWAY_TIMEOUT, "upstream request timeout");
    log.info(response.toString());
    log.info(GSON.toJson(response));
    Assertions.assertThat(JSONStrUtil.isJSONStr(GSON.toJson(response))).isTrue();
  }

  @Test
  public void testBytes() {
    Assertions.assertThat(JSONStrUtil.isJSONBytes(oriStr.getBytes())).isTrue();
    Assertions.assertThat(JSONStrUtil.isJSONBytes((oriStr + "d").getBytes())).isFalse();
    Assertions.assertThat(JSONStrUtil.isJSONBytes(("d" + oriStr + "d").getBytes())).isFalse();
  }


  @Test
  public void testBlankStr() {
    JSONStrUtil jsonStrUtil = new JSONStrUtil();
    Assert.assertFalse(JSONStrUtil.isJSONStr(null));
    Assert.assertFalse(JSONStrUtil.isJSONBytes(new byte[1]));
    Assert.assertFalse(JSONStrUtil.isJSONBytes(null));
  }

}