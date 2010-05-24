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
import java.util.regex.Pattern;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Log;

/**
 * This class provides a utility method that can be used to retrieve the
 * IP address of this host.
 * <p>
 * This class internaly uses the <code>NetworkInterface</code> class to 
 * retrieve that address. If an address can't be found that way, the
 * following code is used: 
 * <pre>
 * InetAddress.getLocalHost()
 * </pre>
 * The above code is know to return 0.0.0.0 or the loopback address under some
 * Linux distributions, which is what this class first attempts to use
 * the <code>NetworkInterface</code>.
 *
 * @author yduchesne
 */
public class Localhost {
  
  private static String LOCALHOST = "0.0.0.0";
  private static String LOOPBACK  = "127.0.0.1";
  private static Pattern DEFAULT_IP_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
  private static Pattern _pattern = DEFAULT_IP_PATTERN;
  
  static{
    String patternStr = System.getProperty(Consts.IP_PATTERN_KEY);
    if(patternStr != null){
      if(Log.isDebug()){
        Log.debug(Localhost.class, "Got local network address pattern: " + patternStr);
      }
      _pattern = Pattern.compile(patternStr);
    }
  }
  
  /**
   * @return the <code>InetAddress</code> of this host.
   */
  public static InetAddress getLocalAddress() throws UnknownHostException{
    NetworkInterface iface;
    try{
      for(Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();){
        iface = (NetworkInterface)ifaces.nextElement();
        InetAddress ia = null;
        for(Enumeration ips = iface.getInetAddresses(); ips.hasMoreElements();){
          ia = (InetAddress)ips.nextElement();
          String addr = ia.getHostAddress();
          if(addr.equals(LOCALHOST) || addr.equals(LOOPBACK)){
            continue;
          }
          if(isLocalAddress(_pattern, addr)){
            if(Log.isDebug()){
              Log.debug(Localhost.class, "Address " + addr + " matches: " + _pattern.toString());
            }
            return ia;
          }
        }
      }
    }catch(SocketException e){}
    return InetAddress.getLocalHost();
  }
  
  static boolean isLocalAddress(Pattern pattern, String addr){
    return pattern.matcher(addr).matches();
  }
  
}
