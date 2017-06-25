package org.sapia.ubik.rmi.naming.remote;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.PropUtil;
import org.sapia.ubik.util.cli.Cmd;

/**
 * This class implements a JNDI server by exporting a root
 * {@link javax.naming.Context} as a remote object. It has the following
 * characteristics:
 * 
 * <ul>
 * <li>It sends notifications to new clients that appear on the network,
 * allowing these clients to benefit from the dynamic discovery of JNDI servers.
 * <li>It sends notifications to clients every time a service is bound to them.
 * This allows clients to benefit from the dynamic discovery of new services
 * that appear on the network.
 * </ul>
 * 
 * To benefit from these features, clients must connect to this server by using
 * a {@link RemoteInitialContextFactory}.
 * 
 * 
 * @see org.sapia.ubik.rmi.naming.remote.JNDIServer
 * @see org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory
 * 
 * @author Yanick Duchesne
 */
public class JNDIServer {
  /**
   * Starts this server. The port on which the server must listen, as well as
   * its domain are specified through the following command-line switches,
   * respectively:
   * <ul>
   * <li><code>-p</code>.
   * <li><code>-d</code>.
   * <ul>
   * 
   * Additionally the <code>-f</code> switch can be used to specify a path
   * holding Ubik runtime properties (see the {@link Consts} class for all the
   * properties that can be specified). The properties in such a file will be
   * exported to the system properties.
   * <p>
   * If no properties specify otherwise, broadcast will use IP multicast, using
   * the default multicast address and port, respectively:
   * 
   * <ul>
   * <li>231.173.5.7
   * <li>5454
   * </ul>
   */
  public static void main(String[] args) throws Exception {

    // disabling log4j output at startup
    Logger.getRootLogger().setLevel(Level.OFF);

    Args argsObj = parseArgs(args, true);
    Conf props = new Conf();
    props.addProperties(argsObj.properties);
    PropUtil.copy(argsObj.properties, System.getProperties());

    EventChannel channel = new EventChannel(props.getProperty(Consts.UBIK_DOMAIN_NAME, Defaults.DEFAULT_DOMAIN), props);
    EmbeddableJNDIServer server = new EmbeddableJNDIServer(channel, argsObj.port);
    server.start(false);
    Runtime.getRuntime().addShutdownHook(new ShutdownHook(server));

    System.out.println("Started JNDI server on port: " + argsObj.port);

    while (true) {
      Thread.sleep(100000);
    }
  }

  /**
   * Parses the given command-line arguments and returns their object
   * representation, or null if the args could not be parsed. If the latter
   * happens, this method displays help to stdout before returing.
   * 
   * @return an {@link Args} instance holding command-line arguments.
   */
  static Args parseArgs(String[] args, boolean doExitOnHelp) {

    int port = JNDIConsts.DEFAULT_PORT;
    Properties props = new Properties();

    if (args.length > 0) {
      Cmd cmd = Cmd.fromArgs(args);

      // ----------------------------------------------------------------------
      // help
      if (cmd.hasSwitch("h")) {
        help(doExitOnHelp);
      }

      // ----------------------------------------------------------------------
      // determining JNDI server port
      if (cmd.hasSwitch("p")) {
        try {
          port = cmd.getOpt("p").getIntValue();
        } catch (NumberFormatException e) {
          System.out.println("Invalid port: " + cmd.getOpt("p").getValue());
          help(doExitOnHelp);
        }
      } else {
        port = JNDIConsts.DEFAULT_PORT;
      }

      // ----------------------------------------------------------------------
      // determining JNDI server domain
      if (cmd.hasSwitch("d")) {
        props.setProperty(Consts.UBIK_DOMAIN_NAME, cmd.getOpt("d").getTrimmedValueOrBlank());
      } else {
        props.setProperty(Consts.UBIK_DOMAIN_NAME, Defaults.DEFAULT_DOMAIN);
      }

      // ----------------------------------------------------------------------
      // loading properties from file if -f swith specified.
      if (cmd.hasSwitch("f")) {
        File configFile = new File(cmd.getOptWithValue("f").getTrimmedValueOrBlank());
        if (configFile.exists()) {
          if (configFile.isDirectory()) {
            System.out.println("Specified config file is a directory: " + cmd.getOpt("f"));
            help(doExitOnHelp);
          }
          try {
            PropUtil.loadIntoPropertiesFrom(props, configFile);
            System.out.println();
            System.out.println("Found properties (" + configFile.getName() + "):");
            for (String name : props.stringPropertyNames()) {
              System.out.println(name + " = " + props.getProperty(name));
            }
            System.out.println();

          } catch (IOException e) {
            System.out.println("Could not load config file: " + cmd.getOpt("f"));
            e.printStackTrace(System.out);
            help(doExitOnHelp);
          }
        } else {
          System.out.println("Specified config file does not exist: " + cmd.getOpt("f"));
          help(doExitOnHelp);
        }
      }
    }

    return new Args(port, props);
  }

  static final void help(boolean doExitOnHelp) {
    System.out.println();
    System.out.println("Syntax: jndi [-p <port>] [-d <domain>] [-f <path_to_config>]");
    System.out.println();
    System.out.println("Where:");
    System.out.println("  <port>..........: The port on which the JNDI server should listen (defaults to 1099).");
    System.out.println("  <domain>........: The name of the domain that the JNDI server should join (defaults to 'default').");
    System.out.println("  <path_to_config>: The path to the Java properties file with which to configure this server.");
    System.out.println("                    The properties in the file will be exported to the JVM's system properties.");
    System.out.println();
    System.out.println(String.format("This server will use the default IP multicast address (%s) and port (%s)", Defaults.DEFAULT_MCAST_ADDR,
        Defaults.DEFAULT_MCAST_PORT));
    System.out.println("for group communication. If you wish otherwise, you have to configure the relevant properties");
    System.out.println("in a file and use the -f option to point to that file.");
    System.out.println();
    System.out.println("Notes:");
    System.out.println("  1) You do not have to use the default transport for broadcast. You can configure group");
    System.out.println("     communication to use Avis, which may prove more robust (and will allow you to work");
    System.out.println("     use broadcast in networks that do not support IP multicast. How to configure broadcast");
    System.out.println("     based on Avis is explained on Ubik's web site.");
    System.out.println("  2) If you are running the JNDI server on a host with more than one multicast address,");
    System.out.println("     you should specify the ubik.rmi.address-pattern JVM property - which consists of a");
    System.out.println("     regexp that should match the address of the interface you wish the server to bind to.");
    System.out.println("     This address will also be returned to clients as part of the JNDI server stub itself");
    System.out.println("     (since the server is implemented using Ubik RMI). The property in question should also");
    System.out.println("     be specified in the properties file that you use to configure this server.");

    if (doExitOnHelp) {
      System.exit(1);
    }
  }

  // ==========================================================================

  public static class Args {
    int port;
    Properties properties;

    Args(int port, Properties properties) {
      this.port = port;
      this.properties = properties;
    }
  }

  // --------------------------------------------------------------------------

  public static final class ShutdownHook extends Thread {

    private EmbeddableJNDIServer svr;

    ShutdownHook(EmbeddableJNDIServer svr) {
      this.svr = svr;
    }

    /**
     * @see java.lang.Thread#run()
     */
    public void run() {
      svr.stop();
      try {
        Hub.shutdown(30000);
      } catch (InterruptedException e) {
        Log.error(getClass(), "JNDI server could not shut down properly", e);
      }
    }
  }
}
