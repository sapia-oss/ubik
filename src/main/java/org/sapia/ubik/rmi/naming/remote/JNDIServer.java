package org.sapia.ubik.rmi.naming.remote;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Props;


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
   * Starts this server. The port on which the server must listen, as well as its domain, can
   * be specified. The default value for each of these arguments is - respectively - 1099 and
   * and "default".
   * <p>
   * The multicast address and port on which this JNDI server listens - and through which
   * it sends notifications - can be specified through system properties. The latter are:
   *
   * <ul>
   *   <li>ubik.rmi.naming.mcast.address
   *   <li>ubik.rmi.naming.mcast.port
   * </ul>
   *
   * If not specified, the following are used for multicast address and port, respectively:
   *
   * <ul>
   *   <li>231.173.5.7
   *   <li>5454
   * </ul>
   */
  public static void main(String[] args) {
    JNDIServerHelper.Args argsObj = JNDIServerHelper.parseArgs(args);
    if (argsObj != null) {
      try {
        
        Props props              = Props.getSystemProperties();
        String broadcastProvider = props.getProperty(Consts.BROADCAST_PROVIDER);
        String unicastProvider   = props.getProperty(Consts.UNICAST_PROVIDER);
        
        EmbeddableJNDIServer server;
        if(broadcastProvider != null || unicastProvider != null) {
          EventChannel channel = new EventChannel(argsObj.domain, props);
          server = new EmbeddableJNDIServer(channel, argsObj.port);
        } else {
          server = new EmbeddableJNDIServer(
                         argsObj.domain,
                         argsObj.port, 
                         argsObj.mcastAddress, 
                         argsObj.mcastPort
                       );
        }
        server.start(false);
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(server));
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

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
