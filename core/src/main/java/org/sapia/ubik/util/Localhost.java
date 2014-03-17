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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.Consts;

/**
 * This class provides a utility method that can be used to retrieve the IP
 * address of this host.
 * <p>
 * This class internally uses the <code>NetworkInterface</code> class to retrieve
 * that address. If an address can't be found that way, the following code is
 * used:
 *
 * <pre>
 * InetAddress.getPreferredLocalAddress()
 * </pre>
 *
 * The above code is know to return 0.0.0.0 or the loopback address under some
 * Linux distributions, which is what this class first attempts to use the
 * <code>NetworkInterface</code>.
 *
 * @author yduchesne
 */
public final class Localhost {

  private static final Category LOG = Log.createCategory(Localhost.class);
  private static final String LOCALHOST = "0.0.0.0";
  private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
  private static final String LOOPBACK = "127.0";
  private static final Pattern IPV4_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
  private static final List<Pattern> PATTERNS = new ArrayList<>();

  static {
    
    List<String> patternKeys = new ArrayList<>();
    for (String n : System.getProperties().stringPropertyNames()) {
      if (n.startsWith(Consts.IP_PATTERN_KEY)) {
        patternKeys.add(n);
      }
    }
    
    Collections.sort(patternKeys);
    for (String k : patternKeys) {
      String patternStr = System.getProperty(k);
      if (patternStr != null) {
        LOG.info("Got local network address pattern: %s", patternStr);
        PATTERNS.add(Pattern.compile(patternStr));
      }
    }
    
    if (PATTERNS.isEmpty()) {
      LOG.debug("Local network address pattern not set. Will fall back to default address selection");
    }
  }

  private Localhost() {
  }

  static void setAddressPattern(String aPattern) {
    PATTERNS.add(Pattern.compile(aPattern));
  }

  static void unsetAddressPattern() {
    PATTERNS.clear();
  }

  /**
   * @return <code>true</code> if one or more IP address pattern(s) has/have been defined.
   */
  public static boolean isIpPatternDefined() {
    return !PATTERNS.isEmpty();
  }

  /**
   *
   * @return an InetAddress
   * @throws UnknownHostException
   * @deprecated Should method {@link #getPreferredLocalAddress()} instead.
   */
  @Deprecated
  public static InetAddress getLocalAddress() throws UnknownHostException {
    return getPreferredLocalAddress();
  }

  /**
   * @return the preferred {@link InetAddress}.
   * @throws UnknownHostException if the local host could not be resolved.
   */
  public static InetAddress getPreferredLocalAddress() throws UnknownHostException {

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

  // --------------------------------------------------------------------------
  /// Restricted methods
  
  static InetAddress doGetAnyLocalAddress(Set<InetAddress> netAddresses) throws UnknownHostException {
    if (isIpPatternDefined()) {
      return doSelectAddress(netAddresses);
    } else {
      Set<InetAddress> nonLocalAddresses = Collects.filterAsSet(netAddresses, new Condition<InetAddress>() {
        @Override
        public boolean apply(InetAddress addr) {
          return !(addr.getHostAddress().startsWith(LOCALHOST)
              || addr.getHostAddress().startsWith(LOOPBACK)
              || addr.getHostAddress().startsWith(LOCALHOST_IPV6));
        }
      });

      if (nonLocalAddresses.size() == 1) {
        return nonLocalAddresses.iterator().next();
      }

      Set<InetAddress> ipV4Addresses = Collects.filterAsSet(nonLocalAddresses, new Condition<InetAddress>() {
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

    Set<InetAddress> nonLocalAddresses = Collects.filterAsSet(netAddresses, new Condition<InetAddress>() {
      @Override
      public boolean apply(InetAddress addr) {
        return !(addr.getHostAddress().startsWith(LOCALHOST)
            || addr.getHostAddress().startsWith(LOOPBACK)
            || addr.getHostAddress().startsWith(LOCALHOST_IPV6));
      }
    });

    Set<InetAddress> matchedAddresses = Collects.filterAsSet(nonLocalAddresses, new Condition<InetAddress>() {
      @Override
      public boolean apply(InetAddress addr) {
        return isLocalAddress(PATTERNS, addr.getHostAddress());
      }
    });

    if (!matchedAddresses.isEmpty()) {
      InetAddress addr = matchedAddresses.iterator().next();
      LOG.debug("Address %s matches address pattern", addr);
      return addr;
    }
    return InetAddress.getLocalHost();
  }

  static boolean isLocalAddress(List<Pattern> patterns, String addr) {
    for (Pattern p : patterns) {
      if(p.matcher(addr).matches()) {
        return true;
      }
    }
    return false;
  }

}
