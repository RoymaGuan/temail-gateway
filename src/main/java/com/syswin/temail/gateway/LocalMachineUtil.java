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

package com.syswin.temail.gateway;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalMachineUtil {

  private static final String DEFAULT_IP = "127-0-0-1";

  public static String getLocalIp() {
    String osName = System.getProperty("os.name"); // 获取系统名称
    if (osName != null && osName.startsWith("Windows")) { // 如果是Windows系统
      return getWindowsIp();
    } else {
      return geLinuxIp();
    }
  }


  private static String geLinuxIp() {
    String ipLocalAddr = DEFAULT_IP;
    InetAddress ip;
    try {
      Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
      while (allNetInterfaces.hasMoreElements()) {
        NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
        Enumeration addresses = netInterface.getInetAddresses();
        while (addresses.hasMoreElements()) {
          ip = (InetAddress) addresses.nextElement();
          if (ip instanceof Inet4Address) { // IP是ipv4，ipv6换成Inet6Address
            String hostAddress = ip.getHostAddress();
            if (!"127.0.0.1".equals(hostAddress) && !"/127.0.0.1".equals(hostAddress)) {
              ipLocalAddr = ip.toString().split("[/]")[1]; // 得到本地IP
            }
          }
        }
      }
    } catch (SocketException ex) {
      log.error("fail to get local ip.", ex);
    }
    ipLocalAddr = ipLocalAddr.replace(".", "-");
    return ipLocalAddr;
  }


  private static String getWindowsIp() {
    String localIp = "";
    try {
      InetAddress addr = InetAddress.getLocalHost();
      localIp = addr.getHostAddress(); // 获取本机ip
      localIp = localIp.replace(".", "-");
    } catch (Exception ex) {
      log.error("fail to get local ip", ex);
      localIp = DEFAULT_IP;
    }
    return localIp;
  }
}
