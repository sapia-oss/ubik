package org.sapia.ubik.rmi.naming.remote;

import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Log;


/**
 * This class implements a <code>JNDIServer</code> by exporting a root
 * <code>javax.naming.Context</code> as a remote object. It has the following
 * characteristics:
 *
 * <ul>
 *   <li>It sends notifications to new clients that appear on the network, allowing these
 *       clients to benefit from the dynamic discovery of JNDI servers.
 *   <li>It sends notifications to clients every time a service is bound to them. This allows
 *       clients to benefit from the dynamic discovry of new services that appear on the
 *       network.
 * </ul>
 *
 * To benefit from these features, clients must connect to this server by using a
 * <code>RemoteInitialContextFactory</code>.
 *
 *
 * @see org.sapia.ubik.rmi.naming.remote.JNDIServer
 * @see org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
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
        EmbeddableJNDIServer server = new EmbeddableJNDIServer(argsObj.domain,
            argsObj.port, argsObj.mcastAddress, argsObj.mcastPort);

        server.start(false);

        Runtime.getRuntime().addShutdownHook(new ShutdownHook(server));
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }
  }

  public static final class ShutdownHook extends Thread {
    private EmbeddableJNDIServer _svr;

    ShutdownHook(EmbeddableJNDIServer svr) {
      _svr = svr;
    }

    /**
     * @see java.lang.Thread#run()
     */
    public void run() {
      _svr.stop();
      try{
        Hub.shutdown(30000);
      }catch(InterruptedException e){
      	Log.error(getClass(), "JNDI server could not shut down properly", e);
      }
    }
  }
}
