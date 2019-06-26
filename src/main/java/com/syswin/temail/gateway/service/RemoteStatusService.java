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

import static java.util.Collections.singletonList;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.TemailGatewayProperties.Instance;
import com.syswin.temail.gateway.channels.ChannelsSyncClient;
import com.syswin.temail.gateway.entity.TemailAccoutLocation;
import com.syswin.temail.gateway.entity.TemailAccoutLocations;
import com.syswin.temail.ps.server.entity.Session;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

@Slf4j
public class RemoteStatusService {

  private final TemailGatewayProperties properties;

  //private final WebClient statusWebClient;

  private final ChannelsSyncClient channelsSyncClient;

  // a async queue used for retry failed task
  private final PendingTaskQueue<Pair> pendingTaskQueue = new PendingTaskQueue<>(
      5000,
      pair -> reqUpdSts4Upd(pair.getTemailAccoutLocations(), pair.getTemailAcctUptOptType(), ignored -> {
      })
  );

  public RemoteStatusService(TemailGatewayProperties properties, ChannelsSyncClient channelsSyncClient) {
    this.channelsSyncClient = channelsSyncClient;
    this.properties = properties;
    this.pendingTaskQueue.run();
  }

  public void addSession(String temail, String deviceId, String platform, Consumer<Boolean> consumer) {
    updSessionByType(temail, deviceId, platform, TemailAcctUptOptType.add, consumer);
  }

  public void removeSession(String temail, String deviceId, Consumer<Boolean> consumer) {
    updSessionByType(temail, deviceId, "", TemailAcctUptOptType.del, consumer);
  }

  private void updSessionByType(String temail, String deviceId, String platform, TemailAcctUptOptType optType,
      Consumer<Boolean> consumer) {
    reqUpdSts4Upd(
        new TemailAccoutLocations(singletonList(buildAcctSts(temail, deviceId, platform))),
        optType,
        consumer);
  }

  void removeSessions(Collection<Session> sessions, Consumer<Boolean> consumer) {
    if (sessions.isEmpty()) {
      return;
    }
    List<TemailAccoutLocation> statuses = new ArrayList<>(sessions.size());
    String platform = "";
    for (Session session : sessions) {
      statuses.add(buildAcctSts(session.getTemail(), session.getDeviceId(), platform));
    }
    reqUpdSts4Upd(new TemailAccoutLocations(statuses), TemailAcctUptOptType.del, consumer);
  }

  private TemailAccoutLocation buildAcctSts(String temail, String deviceId, String platform) {
    Instance instance = properties.getInstance();
    return new TemailAccoutLocation(temail, deviceId, platform,
        instance.getHostOf(), instance.getProcessId(),
        properties.getRocketmq().getMqTopic(), instance.getMqTag());
  }

  public boolean reqUpdSts4Upd(TemailAccoutLocations temailAccoutLocations,
      TemailAcctUptOptType type, Consumer<Boolean> consumer) {
    if (type == TemailAcctUptOptType.add) {
      boolean addResult = channelsSyncClient.syncChannelLocations(temailAccoutLocations);
      consumer.accept(addResult);
      if (!addResult) {
        pendingTaskQueue.addTask(new Pair(type, temailAccoutLocations));
      }
      return addResult;
    } else {
      boolean remResult = channelsSyncClient.removeChannelLocations(temailAccoutLocations);
      consumer.accept(remResult);
      if (!remResult) {
        pendingTaskQueue.addTask(new Pair(type, temailAccoutLocations));
      }
      return remResult;
    }
  }

  enum TemailAcctUptOptType {
    add(HttpMethod.POST),
    del(HttpMethod.PUT);

    private HttpMethod method;

    TemailAcctUptOptType(HttpMethod method) {
      this.method = method;
    }

    public HttpMethod getMethod() {
      return method;
    }
  }
}
