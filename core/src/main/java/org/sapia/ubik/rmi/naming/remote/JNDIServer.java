package org.sapia.ubik.rmi.naming.remote;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Props;
import org.sapia.ubik.util.cli.Cmd;


/**
 * This class implements a JNDI server by exporting a root
 * {@link javax.naming.Context} as a remote object. It has the following
 * characteristics:
 *
 * <ul>
 *   <li>It sends notifications to new clients that appear on the network, allowing these
 *       clients to benefit from the dynamic discovery of JNDI servers.
 *   <li>It sends notifications to clients every time a service is bound to them. This allows
 *       clients to benefit from the dynamic discovery of new services that appear on the
 *       network.
 * </ul>
 *
 * To benefit from these features, clients must connect to this server by using a
 * {@link RemoteInitialContextFactory}.
 *
 *
 * @see org.sapia.ubik.rmi.naming.remote.JNDIServer
 * @see org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory
 *
 * @author Yanick Duchesne
 */
public class JNDIServer {
  /**
   * Starts this server. The port on which the server must listen, as well as its domain are specified through 
   * the following command-line switches, respectively:
   * <ul>
   *   <li><code>-p</code>.
   *   <li><code>-d</code>.
   * <ul>
   * 
   * Additionally the <code>-f</code> switches can be used to specify a path that holding Ubik runtime properties
   * (see the {@link Consts} class for all the properties that can be specified).
   * <p>
   * If no properties specify otherwise, broadcast will use IP multicast, using the default multicast address and port, respectively:
   *
   * <ul>
   *   <li>231.173.5.7
   *   <li>5454
   * </ul>
   */
  public static void main(String[] args) throws Exception {
    Args argsObj = parseArgs(args);

    Props props = new Props();
    props.addProperties(argsObj.properties);
    
    for(String name : argsObj.properties.stringPropertyNames()) {
    	String value = argsObj.properties.getProperty(name);
    	System.setProperty(name, value);
    }
    
    EventChannel 				 channel = new EventChannel(props.getProperty(Consts.UBIK_DOMAIN_NAME, Consts.DEFAULT_DOMAIN));
    EmbeddableJNDIServer server  = new EmbeddableJNDIServer(channel, argsObj.port);
    server.start(false);
    Runtime.getRuntime().addShutdownHook(new ShutdownHook(server));

    System.out.println("Started JNDI server on port: " + argsObj.port);
    
    while (true) {
    	Thread.sleep(100000);
    }
  }

  /**
   * Parses the given command-line arguments and returns their object representation, or
   * null if the args could not be parsed. If the latter happens, this method displays
   * help to stdout before returing.
   *
   * @return an {@link Args} instance holding command-line arguments.
   */
  static Args parseArgs(String[] args) {
  	
    int    		 port  = JndiConsts.DEFAULT_PORT;
    Properties props = new Properties();
    
    if (args.length > 0) {
      Cmd cmd = Cmd.fromArgs(args);
      
      // ----------------------------------------------------------------------
      // help
      if (cmd.hasSwitch("h")) {
        help();
      }
      
      // ----------------------------------------------------------------------
      // determining JNDI server port
      if (cmd.hasSwitch("p")) {
      	try {
      		port = cmd.getOpt("p").getIntValue();
      	} catch (NumberFormatException e) {
      		System.out.println("Invalid port: " + cmd.getOpt("p").getValue());
      		help();
      	}
      } else {
      	port = JndiConsts.DEFAULT_PORT;
      }

      // ----------------------------------------------------------------------
      // determining JNDI server domain
      if (cmd.hasSwitch("d")) {
      	props.setProperty(Consts.UBIK_DOMAIN_NAME, cmd.getOpt("d").getTrimmedValueOrBlank());
      } else {
      	props.setProperty(Consts.UBIK_DOMAIN_NAME, Consts.DEFAULT_DOMAIN);
      }

      // ----------------------------------------------------------------------
      // loading properties from file if -f swith specified.
      if (cmd.hasSwitch("f")) {
      	File configFile = new File(cmd.getOpt("f").getTrimmedValueOrBlank());
      	if (configFile.exists()) {
      		if (configFile.isDirectory()) {
        		System.out.println("Specified config file is a directory: " + cmd.getOpt("f"));
        		help();
      		}
      		FileInputStream is = null;
      		try {
      			is = new FileInputStream(configFile);
      			props.load(new BufferedInputStream(is));
      		} catch (IOException e) {
      			System.out.println("Could not load config file: " + cmd.getOpt("f"));
      			e.printStackTrace(System.out);
      			help();
      		} finally {
      			if (is != null) {
      				try {
      					is.close();
      				} catch (IOException e) {
      					// noop
      				}
      			}
      		}
      	} else {
      		System.out.println("Specified config file does not exist: " + cmd.getOpt("f"));
      		help();
      	}
      }
    }

    return new Args(port, props);
  }
  
  
  static final void help() {
    System.out.println();
    System.out.println("Syntax: jndi [-p <port>] [-d <domain>] [-f <path_to_config>]");
    System.out.println("where:");
    System.out.println(
      "<port>: is the port on which JNDI server should listen (defaults to 1099).");
    System.out.println(
      "<domain>: is the domain name that JNDI server is part of (defaults to 'default').");
    System.out.println(
        "<path_to_config>: is the path to the properties file for configuring this server.");    
    System.out.println();
    System.out.println();
    System.exit(1);
  }  
  
  // ==========================================================================
  
  public static class Args {
    int    		 port;
    Properties properties;

    Args(int port, Properties properties) {
      this.port           = port;
      this.properties     = properties;
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
      try{
        Hub.shutdown(30000);
      }catch(InterruptedException e){
      	Log.error(getClass(), "JNDI server could not shut down properly", e);
      }
    }
  }
}

