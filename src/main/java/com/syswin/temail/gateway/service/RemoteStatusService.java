package com.syswin.temail.gateway.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Resource;

import com.syswin.temail.gateway.TemailGatewayProperties;
import com.syswin.temail.gateway.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class RemoteStatusService {

  @Resource
  private final TemailGatewayProperties properties;

  @Resource
  private final WebClient statusWebClient;

  // a async queue used for retry failed task
  private final PendingTaskQueue<Pair> pendingTaskQueue = new PendingTaskQueue<Pair>(
      5000,
      pair -> reqUpdSts4Upd(pair.getTemailAcctStses(), pair.getTemailAcctUptOptType(), null)
  );

  @Autowired
  public RemoteStatusService(TemailGatewayProperties properties, WebClient statusWebClient) {
    this.properties = properties;
    this.statusWebClient = statusWebClient;
    this.pendingTaskQueue.run();
  }

  public void addSession(String temail, String deviceId, Consumer consumer) {
    updSessionByType(temail, deviceId, TemailAcctUptOptType.add, consumer);
  }

  public void removeSession(String temail, String deviceId, Consumer consumer) {
    updSessionByType(temail, deviceId, TemailAcctUptOptType.del, consumer);
  }

  public void updSessionByType(String temail, String deviceId, TemailAcctUptOptType optType,Consumer consumer) {
    reqUpdSts4Upd(new TemailAcctStses(
        new ArrayList<TemailAcctSts>() {{
          add(buildAcctSts(temail, deviceId));
        }}), optType, consumer);
  }

  public void removeSessions(Iterable<Session> sessions,Consumer consumer) {
    reqUpdSts4Upd(new TemailAcctStses(new ArrayList<TemailAcctSts>() {{
      for (Session session : sessions) {
        add(buildAcctSts(session.getTemail(), session.getDeviceId()));
      }
    }}), TemailAcctUptOptType.del,consumer);
  }

  private TemailAcctSts buildAcctSts(String temail, String deviceId) {
    return new TemailAcctSts(temail, deviceId,
        properties.getHostOf(), properties.getProcessId(),
        properties.getMqTopic(), properties.getMqTag());
  }

  public TemailAcctStses locateTemailAcctSts(String temail) {
    Response<TemailAcctStses> res =
        statusWebClient
            .method(HttpMethod.GET)
            .uri(properties.getUpdateSocketStatusUrl() + "/" + temail)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Response<TemailAcctStses>>() {
            }).block();
    return res.getData();
  }

  private void reqUpdSts4Upd(TemailAcctStses temailAcctStses, TemailAcctUptOptType type, Consumer consumer) {
    statusWebClient.method(type.getMethod())
        .uri(properties.getUpdateSocketStatusUrl())
        .contentType(MediaType.APPLICATION_JSON_UTF8)
        .syncBody(temailAcctStses)
        .exchange()
        .subscribe(clientResponse -> {
          if (!clientResponse.statusCode().is2xxSuccessful()) {
            log.info("upd temailAcctStses fail {} , will try agagin later! ", clientResponse.statusCode());
            pendingTaskQueue.addTask(new Pair(type, temailAcctStses));
          } else {
            clientResponse.bodyToMono(new ParameterizedTypeReference<Response<ComnRespData>>() {
            }).subscribe(result -> {
              log.debug("response from status server: {}", result.toString());
              Optional.ofNullable(consumer).ifPresent(consumer1 -> consumer1.accept(result));
            });
          }
        });
  }

  static enum TemailAcctUptOptType {
    add(HttpMethod.POST),
    del(HttpMethod.DELETE);

    private TemailAcctUptOptType(HttpMethod method) {
      this.method = method;
    }

    private HttpMethod method;

    public HttpMethod getMethod() {
      return method;
    }
  }
}
