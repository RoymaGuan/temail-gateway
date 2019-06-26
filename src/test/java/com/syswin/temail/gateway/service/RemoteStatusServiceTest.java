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


import static org.mockito.Mockito.when;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.TemailGatewayProperties.Rocketmq;
import com.syswin.temail.gateway.channels.ChannelsSyncClient;
import com.syswin.temail.gateway.entity.TemailAccoutLocation;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import com.syswin.temail.ps.server.entity.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


@Slf4j
public class RemoteStatusServiceTest {

  private final TemailGatewayProperties.Instance instance = new TemailGatewayProperties.Instance();

  private final TemailGatewayProperties properties = new TemailGatewayProperties();

  private final ChannelsSyncClient grpcClientWrapper = Mockito.mock(ChannelsSyncClient.class);

  private final RemoteStatusService remoteStatusService = new RemoteStatusService(properties, grpcClientWrapper);

  private final List<Session> sessions = new ArrayList<>();

  private final List<Boolean> results = new ArrayList<Boolean>();

  private TemailAccoutLocations temailAccoutLocationsSingle = null;

  private TemailAccoutLocations temailAccoutLocationsBatch = null;


  @Before
  public void init() {

    instance.setHostOf("localhost");
    instance.setProcessId("2e0wd0d2e2ie0iwhfoqie92h2u");
    properties.setInstance(instance);

    properties.setGrpcServerHost("localhost");
    properties.setGrpcServerPort("9110");

    Rocketmq rocketmq = new Rocketmq();
    rocketmq.setMqTopic("gateway-notification");
    properties.setRocketmq(rocketmq);

    sessions.add(new Session("sean@temail.com", "123"));
    temailAccoutLocationsSingle = buildTemailAccoutLocations(sessions);

    sessions.add(new Session("jean@temail.com", "123"));
    temailAccoutLocationsBatch = buildTemailAccoutLocations(sessions);

    when(grpcClientWrapper.removeChannelLocations(temailAccoutLocationsBatch)).thenReturn(true);
    when(grpcClientWrapper.removeChannelLocations(temailAccoutLocationsSingle)).thenReturn(true);
    when(grpcClientWrapper.syncChannelLocations(temailAccoutLocationsSingle)).thenReturn(true);

  }


  private TemailAccoutLocations buildTemailAccoutLocations(Collection<Session> sessions) {
    List<TemailAccoutLocation> statuses = new ArrayList<>(sessions.size());
    for (Session session : sessions) {
      statuses.add(buildAcctSts(session.getTemail(), session.getDeviceId()));
    }
    return new TemailAccoutLocations(statuses);
  }


  private TemailAccoutLocation buildAcctSts(String temail, String deviceId) {
    TemailGatewayProperties.Instance instance = properties.getInstance();
    return new TemailAccoutLocation(temail, deviceId, "",
        instance.getHostOf(), instance.getProcessId(),
        properties.getRocketmq().getMqTopic(), instance.getMqTag());
  }


  @Test
  public void testAddSession() {
    //for testing addSession
    remoteStatusService.reqUpdSts4Upd(temailAccoutLocationsSingle,
        RemoteStatusService.TemailAcctUptOptType.add, results::add);
    Awaitility.waitAtMost(1, TimeUnit.SECONDS).until(() -> {
      return results.remove(0);
    });

    //for testing removeSession
    remoteStatusService.reqUpdSts4Upd(temailAccoutLocationsBatch,
        RemoteStatusService.TemailAcctUptOptType.del, results::add);
    Awaitility.waitAtMost(1, TimeUnit.SECONDS).until(() -> {
      return results.remove(0);
    });

    //for testing removeSessions
    remoteStatusService.reqUpdSts4Upd(temailAccoutLocationsBatch,
        RemoteStatusService.TemailAcctUptOptType.del, results::add);
    Awaitility.waitAtMost(1, TimeUnit.SECONDS).until(() -> results.remove(0));

  }

  @Test
  public void testAddSessions() {
    remoteStatusService.addSession("zhangsan", "ios110", "ios", x -> {
    });
  }

  @Test
  public void testRemoveSession() {
    remoteStatusService.removeSessions(sessions, x -> {
    });

    remoteStatusService.removeSessions(new ArrayList<>(), x -> {
    });

    remoteStatusService.removeSession("zhangsan", "ios111", x -> {
    });

  }
}
