package com.syswin.temail.gateway.service;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class JSONStrUtil {

  private static final Pattern PATTERN = Pattern.compile("^\\{.*\\}$", Pattern.DOTALL);

  public static boolean isJSONStr(String str){
    if(StringUtils.isBlank(str)){
      return false;
    }
    return PATTERN.matcher(str).matches();
  }

  public static boolean isJSONBytes(byte[] bytes){
    if(bytes == null || bytes.length < 2){
      return false;
    }
    return bytes[0] == '{' && bytes[bytes.length-1] == '}';
  }

}
