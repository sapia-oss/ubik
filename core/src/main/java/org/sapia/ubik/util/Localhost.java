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

  private static final Category LOG             = Log.createCategory(Localhost.class);
  
  private static final String   LOCALHOST       = "0.0.0.0";
  private static final String   LOCALHOST_IPV6  = "0:0:0:0:0:0:0:1";
  private static final String   LOOPBACK        = "127.0";
  private static final String   IPV4_PATTERN    =  "\\d+\\.\\d+\\.\\d+\\.\\d+";

  private static final List<Condition<String>> PATTERNS             = new ArrayList<>();
  private static final List<Condition<String>> DEFAULT_CONDITIONS   = new ArrayList<>();
  private static final List<Condition<String>> LOCALHOST_CONDITIONS = Collects.arrayToList(
      startsWith("192.168"), startsWith(LOOPBACK), exact(LOCALHOST), exact(LOCALHOST_IPV6)
  );
      
  static {
    
    DEFAULT_CONDITIONS.add(not(regex(IPV4_PATTERN), LOCALHOST_CONDITIONS));
    DEFAULT_CONDITIONS.addAll(LOCALHOST_CONDITIONS);
    
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
        PATTERNS.add(regex(patternStr));
      }
    }

    if (PATTERNS.isEmpty()) {
      LOG.debug("Local network address pattern not set. Will fall back to default address selection");
    }
    PATTERNS.addAll(DEFAULT_CONDITIONS);
  }

  private Localhost() {
  }

  static void setAddressPattern(String aPattern) {
    PATTERNS.add(0, regex(aPattern));
  }

  static void unsetAddressPattern() {
    PATTERNS.clear();
    PATTERNS.addAll(DEFAULT_CONDITIONS);
  }

  /**
   * @return <code>true</code> if one or more IP address pattern(s) has/have been defined.
   */
  public static boolean isIpPatternDefined() {
    return !PATTERNS.isEmpty();
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

    return doGetPreferredLocalAddress(netAddresses);

  }

  // --------------------------------------------------------------------------
  /// Restricted methods

  static InetAddress doGetPreferredLocalAddress(Set<InetAddress> netAddresses) throws UnknownHostException {
    
    for (Condition<String> c : PATTERNS) {
      for (InetAddress n : netAddresses) {
        if(c.apply(n.getHostAddress())) {
          return n;
        }
      }
    }

    return InetAddress.getLocalHost();
  }
  
  private static Condition<String> startsWith(final String pattern) {
    return new Condition<String>() {
      @Override
      public boolean apply(String item) {
        return item.startsWith(pattern);
      }
      
      @Override
      public String toString() {
        return pattern;
      }
    };
  }
  
  private static Condition<String> exact(final String pattern) {
    return new Condition<String>() {
      @Override
      public boolean apply(String item) {
        return item.equals(pattern);
      }
      
      @Override
      public String toString() {
        return pattern;
      }
    };
  }
  
  private static Condition<String> regex(final String pattern) {
    return new Condition<String>() {
      
      private Pattern p =  Pattern.compile(pattern);

      @Override
      public boolean apply(String item) {
        return p.matcher(item).matches();
      }
      
      @Override
      public String toString() {
        return pattern;
      }
    };
  }
  
  private static Condition<String> not(final Condition<String> included, final List<Condition<String>> excluded) {
    
    return new Condition<String>() {
      
      @Override
      public boolean apply(String item) {
        if (included.apply(item)) {
          for (Condition<String> n : excluded) {
            if (n.apply(item)) {
              return false;
            }
          }
          return true;
        }
        return false;
      }
    };
    
  }

}
