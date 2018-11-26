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

  public void addSession(String temail, String deviceId, Consumer<Boolean> consumer) {
    updSessionByType(temail, deviceId, TemailAcctUptOptType.add, consumer);
  }

  public void removeSession(String temail, String deviceId, Consumer<Boolean> consumer) {
    updSessionByType(temail, deviceId, TemailAcctUptOptType.del, consumer);
  }

  private void updSessionByType(String temail, String deviceId, TemailAcctUptOptType optType,
      Consumer<Boolean> consumer) {
    reqUpdSts4Upd(
        new TemailAccoutLocations(singletonList(buildAcctSts(temail, deviceId))),
        optType,
        consumer);
  }

  void removeSessions(Collection<Session> sessions, Consumer<Boolean> consumer) {
    if(sessions == null && sessions.size() == 0) return;
    List<TemailAccoutLocation> statuses = new ArrayList<>(sessions.size());
    for (Session session : sessions) {
      statuses.add(buildAcctSts(session.getTemail(), session.getDeviceId()));
    }
    reqUpdSts4Upd(new TemailAccoutLocations(statuses), TemailAcctUptOptType.del, consumer);
  }

  private TemailAccoutLocation buildAcctSts(String temail, String deviceId) {
    Instance instance = properties.getInstance();
    return new TemailAccoutLocation(temail, deviceId,
        instance.getHostOf(), instance.getProcessId(),
        properties.getRocketmq().getMqTopic(), instance.getMqTag());
  }

  public void reqUpdSts4Upd(TemailAccoutLocations temailAccoutLocations,
      TemailAcctUptOptType type, Consumer<Boolean> consumer) {
    if (type == TemailAcctUptOptType.add) {
      // TODO: 2018/10/10 no exception thrown
      boolean addResult = channelsSyncClient.syncChannelLocations(temailAccoutLocations);
      consumer.accept(addResult);
      if (!addResult) {
        pendingTaskQueue.addTask(new Pair(type, temailAccoutLocations));
      }
    } else {
      boolean remResult = channelsSyncClient.removeChannelLocations(temailAccoutLocations);
      consumer.accept(remResult);
      if (!remResult) {
        pendingTaskQueue.addTask(new Pair(type, temailAccoutLocations));
      }
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
