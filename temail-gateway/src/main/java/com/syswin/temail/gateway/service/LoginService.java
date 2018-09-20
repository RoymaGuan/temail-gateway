package com.syswin.temail.gateway.service;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;

import com.google.gson.Gson;
import com.syswin.temail.gateway.encrypt.util.SHA256Coder;
import com.syswin.temail.gateway.entity.Response;
import com.syswin.temail.ps.common.entity.CDTPHeader;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
class LoginService {

  private final RestTemplate restTemplate;

  private final String authUrl;

  private final Gson gson;

  private final HttpHeaders httpHeaders = new HttpHeaders();

  private final SHA256Coder sha256Coder = new SHA256Coder();


  public LoginService(RestTemplate restTemplate, String authUrl) {
    this.restTemplate = restTemplate;
    this.authUrl = authUrl;
    this.gson = new Gson();
    httpHeaders.setContentType(APPLICATION_JSON_UTF8);
  }


  public ResponseEntity<Response> validSignature(CDTPPacket cdtpPacket) {
    CDTPHeader header = cdtpPacket.getHeader();
    return this.validSignature(header.getSender(),
        header.getSignature(),
        extractUnsignedData(cdtpPacket),
        String.valueOf(header.getSignatureAlgorithm()));
  }

  public ResponseEntity<Response> validSignature(String temail, String signature,
      String unsignedText, String signatureAlgorithm) {
    Map<String, String> map = new HashMap<>();
    map.put("temail", temail);
    map.put("unsignedBytes", unsignedText);
    map.put("signature", signature);
    map.put("algorithm", signatureAlgorithm);
    String authDataJson = gson.toJson(map);
    HttpEntity<String> requestEntity = new HttpEntity<>(authDataJson, httpHeaders);
    try {
      ResponseEntity<Response> responseResponseEntity = restTemplate
          .postForEntity(authUrl, requestEntity, Response.class);
      log.info("signature valid result : {},  data : {}, url : {} ",
          responseResponseEntity.getStatusCode(), authDataJson, authUrl);
      return responseResponseEntity;
    } catch (RestClientException e) {
      log.error("signature valid error data : {}, url : {} ", authDataJson, authUrl, e);
      return new ResponseEntity<>(Response.failed(SERVICE_UNAVAILABLE,
          e.getMessage()), SERVICE_UNAVAILABLE);
    }
  }

  public String extractUnsignedData(CDTPPacket cdtpPacket) {
    return String.valueOf((cdtpPacket.getCommandSpace() + cdtpPacket.getCommand()))
        + cdtpPacket.getHeader().getTargetAddress()
        + cdtpPacket.getHeader().getTimestamp()
        + sha256Coder.encryptAndSwitch2Base64(cdtpPacket.getData());
  }

}
