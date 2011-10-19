package org.sapia.ubik.rmi.naming.remote;

import java.io.IOException;

import javax.naming.Context;
import javax.naming.NamingException;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.naming.remote.archie.UbikRemoteContext;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class JNDIServerHelper implements Consts {

  /**
   * Parses the given command-line arguments and returns their object representation, or
   * null if the args could not be parsed. If the latter happens, this method displays
   * help to stdout before returing.
   *
   * @return an <code>Args</code> instance holding command-line arguments.
   */
  public static Args parseArgs(String[] args) {
    int    port         = DEFAULT_PORT;
    String domain       = DEFAULT_DOMAIN;

    if (args.length > 0) {
      if (args[0].equals("-h")) {
        help();

        return null;
      }

      try {
        port = Integer.parseInt(args[0]);

        if (args.length == 2) {
          domain = args[1];
        }
      } catch (NumberFormatException e) {
        domain = args[0];

        if (args.length == 2) {
          try {
            port = Integer.parseInt(args[1]);
          } catch (NumberFormatException e2) {
            help();

            return null;
          }
        }
      }
    }

    String portProp = null;
    int mcastPort;
    String mcastAddress;
    
    try {
      portProp = System.getProperty(org.sapia.ubik.rmi.Consts.MCAST_PORT_KEY, Integer.toString(Consts.DEFAULT_MCAST_PORT));
      mcastPort = Integer.parseInt(portProp);
      
    } catch (NumberFormatException e) {
      System.out.println("Invalid multicast port: " + portProp);
      help();
      return null;
    }

    mcastAddress = System.getProperty(org.sapia.ubik.rmi.Consts.MCAST_ADDR_KEY, Consts.DEFAULT_MCAST_ADDR);

    return new Args(port, domain, mcastAddress, mcastPort);
  }

  public static Context newRootContext(EventChannel ec)
    throws NamingException {
    return UbikRemoteContext.newInstance(ec);
  }

  public static ClientListener createClientListener(EventChannel ec,
    ServerAddress addr) throws NamingException, IOException {
    ClientListener listener = new ClientListener(ec, addr);
    ec.registerAsyncListener(Consts.JNDI_CLIENT_PUBLISH, listener);

    ec.dispatch(Consts.JNDI_SERVER_PUBLISH, addr);

    return listener;
  }

  static final void help() {
    System.out.println();
    System.out.println("Syntax: jndi [<port>] [<domain>]");
    System.out.println("where:");
    System.out.println(
      "<port>  := port on which JNDI server should listen (defaults to 1099).");
    System.out.println(
      "<domain>:= domain name that JNDI server is part of (defaults to 'default').");
    System.out.println();
    System.out.println();
    System.exit(1);
  }

  public static class Args {
    int    port;
    int    mcastPort;
    String mcastAddress;
    String domain;

    Args(int port, String domain, String mcastAddress, int mcastPort) {
      this.port           = port;
      this.domain         = domain;
      this.mcastAddress   = mcastAddress;
      this.mcastPort      = mcastPort;
    }
  }
}
