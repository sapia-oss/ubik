/*
 * Localhost.java
 *
 * Created on August 18, 2005, 8:57 AM
 *
 */

package org.sapia.ubik.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Consts;

/**
 * This class provides a utility method that can be used to retrieve the IP
 * address of this host.
 * <p>
 * This class internaly uses the <code>NetworkInterface</code> class to retrieve
 * that address. If an address can't be found that way, the following code is
 * used:
 * 
 * <pre>
 * InetAddress.getLocalHost()
 * </pre>
 * 
 * The above code is know to return 0.0.0.0 or the loopback address under some
 * Linux distributions, which is what this class first attempts to use the
 * <code>NetworkInterface</code>.
 * 
 * @author yduchesne
 */
public final class Localhost {

  private static String LOCALHOST = "0.0.0.0";
  private static String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
  private static String LOOPBACK = "127.0";
  private static Pattern IPV4_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
  private static Pattern _pattern;

  static {
    String patternStr = System.getProperty(Consts.IP_PATTERN_KEY);
    if (patternStr != null) {
      if (Log.isDebug()) {
        Log.debug(Localhost.class, "Got local network address pattern: " + patternStr);
      }
      _pattern = Pattern.compile(patternStr);
    }
  }

  private Localhost() {
  }

  static void setAddressPattern(String pattern) {
    _pattern = Pattern.compile(pattern);
  }

  static void unsetAddressPattern() {
    _pattern = null;
  }

  public static boolean isIpPatternDefined() {
    return (_pattern != null);
  }

  /**
   * 
   * @return an InetAddress
   * @throws UnknownHostException
   * @deprecated Should method {@link #getAnyLocalAddress()} instead.
   */
  public static InetAddress getLocalAddress() throws UnknownHostException {
    return getAnyLocalAddress();
  }

  public static InetAddress getAnyLocalAddress() throws UnknownHostException {

    Set<InetAddress> netAddresses = new HashSet<InetAddress>();
    try {
      for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
        NetworkInterface iface = ifaces.nextElement();
        for (Enumeration<InetAddress> ips = iface.getInetAddresses(); ips.hasMoreElements();) {
          InetAddress ia = ips.nextElement();
          netAddresses.add(ia);
        }
      }
    } catch (SocketException e) {
      return InetAddress.getLocalHost();
    }

    return doGetAnyLocalAddress(netAddresses);

  }

  static InetAddress doGetAnyLocalAddress(Set<InetAddress> netAddresses) throws UnknownHostException {
    if (isIpPatternDefined()) {
      return doSelectAddress(netAddresses);
    } else {
      Set<InetAddress> nonLocalAddresses = Collections2.filterAsSet(netAddresses, new Condition<InetAddress>() {
        @Override
        public boolean apply(InetAddress addr) {
          return !(addr.getHostAddress().startsWith(LOCALHOST) || addr.getHostAddress().startsWith(LOOPBACK) || addr.getHostAddress().startsWith(
              LOCALHOST_IPV6));
        }
      });

      if (nonLocalAddresses.size() == 1) {
        return nonLocalAddresses.iterator().next();
      }

      Set<InetAddress> ipV4Addresses = Collections2.filterAsSet(nonLocalAddresses, new Condition<InetAddress>() {
        @Override
        public boolean apply(InetAddress addr) {
          return IPV4_PATTERN.matcher(addr.getHostAddress()).matches();
        }
      });

      if (ipV4Addresses.size() == 1) {
        return ipV4Addresses.iterator().next();
      }

      return InetAddress.getLocalHost();
    }
  }

  private static InetAddress doSelectAddress(Set<InetAddress> netAddresses) throws UnknownHostException {

    Set<InetAddress> nonLocalAddresses = Collections2.filterAsSet(netAddresses, new Condition<InetAddress>() {
      @Override
      public boolean apply(InetAddress addr) {
        return !(addr.getHostAddress().startsWith(LOCALHOST) || addr.getHostAddress().startsWith(LOOPBACK) || addr.getHostAddress().startsWith(
            LOCALHOST_IPV6));
      }
    });

    Set<InetAddress> matchedAddresses = Collections2.filterAsSet(nonLocalAddresses, new Condition<InetAddress>() {
      @Override
      public boolean apply(InetAddress addr) {
        return isLocalAddress(_pattern, addr.getHostAddress());
      }
    });

    if (!matchedAddresses.isEmpty()) {
      InetAddress addr = matchedAddresses.iterator().next();
      if (Log.isInfo()) {
        Log.info(Localhost.class, "Address " + addr + " matches: " + _pattern.toString());
      }
      return addr;
    }
    return InetAddress.getLocalHost();
  }

  static boolean isLocalAddress(Pattern pattern, String addr) {
    return pattern.matcher(addr).matches();
  }

}
