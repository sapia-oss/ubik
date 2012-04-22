package org.sapia.ubik.rmi.naming.remote;

import java.io.IOException;
import java.util.Properties;

import javax.naming.Context;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.concurrent.ThreadShutdown;
import org.sapia.ubik.concurrent.ThreadStartup;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.naming.remote.archie.UbikRemoteContext;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketTransportProvider;
import org.sapia.ubik.util.Localhost;
import org.sapia.ubik.util.Props;


/**
 * This class implements an embeddable JNDI server.
 * <p>
 * Usage:
 * <pre>
 * EmbeddableJNDIServer jndi = new EmbeddableJNDIServer();
 * jndi.start(true);
 * // use it...
 * jndi.stop();
 * </pre>
 *
 * @author Yanick Duchesne
 *
 */
public class EmbeddableJNDIServer implements RemoteContextProvider, AsyncEventListener{
  
  private Category       log          = Log.createCategory(getClass());
  private String         domain;
  private int            port;
  private Thread         serverThread;
  private EventChannel   channel;
  private Context        root;
  private ThreadStartup  startBarrier = new ThreadStartup();
  
  /**
   * Creates an instance of this class that will listen on the default (1099) port,
   * on the "default" domain. Also internally binds its multicast event channel to the
   * default multicast address and port.
   *
   * @see JNDIConsts#DEFAULT_MCAST_ADDR
   * @see JNDIConsts#DEFAULT_MCAST_PORT
   */
  public EmbeddableJNDIServer() {
    this(JNDIConsts.DEFAULT_DOMAIN, JNDIConsts.DEFAULT_PORT,
      org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_ADDR,
      org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_PORT);
  }

  /**
   * Creates an instance of this class that will listen on the given port and domain.
   * Also internally binds its multicast event channel to the default multicast address and port.
   *
   * @see JNDIConsts#DEFAULT_MCAST_ADDR
   * @see JNDIConsts#DEFAULT_MCAST_PORT
   */
  public EmbeddableJNDIServer(String domain, int port) {
    this(
        domain, 
        port, 
        org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_ADDR,
        org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_PORT
    );
  }

  /**
   * Creates an instance of this class that will listen on the given port and domain.
   * Also internally binds its multicast event channel to the given multicast address and port.
   */
  public EmbeddableJNDIServer(
      String domain, 
      int port, 
      String mcastAddress,
      int mcastPort) {
    this.domain      = domain;
    this.port        = port;
    Properties props = new Properties();
    props.setProperty(JNDIConsts.MCAST_ADDR_KEY, mcastAddress);
    props.setProperty(JNDIConsts.MCAST_PORT_KEY, Integer.toString(mcastPort));
    try {
      channel = new EventChannel(domain, new Props().addProperties(props).addSystemProperties());
    } catch (IOException e) {
      throw new IllegalStateException("Could not build event channel", e);
    }
  }
  
  /**
   * @param ec the {@link EventChannel} that this instance should wrap.
   */
  public EmbeddableJNDIServer(EventChannel ec, int port) {
    this.channel = ec;
    this.domain  = ec.getDomainName().toString();
    this.port    = port;
  }
  
  /**
   * Returns the event channel that this instance uses to broacast events
   * and subscribe to notifications.
   *
   * @return this instance's {@link EventChannel}.
   */
  public EventChannel getEventChannel() {
    if (channel == null) {
      throw new IllegalStateException("Multicast event channel not initialized");
    }

    return channel;
  }

  /**
   * Handles {@link JNDIConsts#JNDI_CLIENT_PUBLISH} remote events. Dispatches {@link JNDIConsts#JNDI_SERVER_DISCO}
   * remote events in response.
   */
  public void onAsyncEvent(RemoteEvent evt) {
    if(evt.getType().equals(JNDIConsts.JNDI_CLIENT_PUBLISH)){
      try{
        channel.dispatch(JNDIConsts.JNDI_SERVER_DISCO,
            new TCPAddress(MultiplexSocketTransportProvider.MPLEX_TRANSPORT_TYPE, Localhost.getAnyLocalAddress().getHostAddress(), port));
      }catch(Exception e){
        log.warning("Could not dispatch JNDI server publishing event", e);
      }
    }
  }

  /**
   * @return this instance's root JNDI <code>Context</code>.
   */
  public Context getRootContext() {
    if (root == null) {
      throw new IllegalStateException("Context not initialized");
    }

    return root;
  }
  
  /**
   * @return this instance's {@link RemoteContext}.
   */
  public RemoteContext getRemoteContext(){
    return (RemoteContext) root;
  }

  /**
   * Stops this instance (this internally closes the <code>EventChannel</code>
   * that this instance holds).
   */
  public void stop() {
    ThreadShutdown.create(serverThread).shutdownLenient();
    if (channel != null) {
      channel.close();
    }
  }

  /**
   * Starts this instance.
   *
   * @param daemon if <code>true</code>, this instance will be started
   * as a daemon thread.
   */
  public void start(boolean daemon) throws Exception {
    serverThread = NamedThreadFactory
      .createWith("ubik.jndi.server@"+domain)
      .setDaemon(true)
      .newThread(new Runnable() {
        @Override
        public void run() {
          doRun();
        }
      });
    serverThread.start();
    startBarrier.await();
  }

  private final void doRun() {
    try {
    	log.warning("Starting JNDI server on port %s, domain %s", port, domain);
    	if (!channel.isStarted()) {
    		channel.start();
    	}
      
      channel.registerAsyncListener(JNDIConsts.JNDI_CLIENT_PUBLISH, this);

      channel.dispatch(JNDIConsts.JNDI_SERVER_PUBLISH,
          new TCPAddress(MultiplexSocketTransportProvider.MPLEX_TRANSPORT_TYPE, Localhost.getAnyLocalAddress().getHostAddress(), port));
      channel.dispatch(JNDIConsts.JNDI_SERVER_DISCO,
          new TCPAddress(MultiplexSocketTransportProvider.MPLEX_TRANSPORT_TYPE, Localhost.getAnyLocalAddress().getHostAddress(), port));

      root = UbikRemoteContext.newInstance(channel);

      Hub.exportObject(this, port);
      
      log.warning("JNDI Server started");

      startBarrier.started();

      while (true) {
        Thread.sleep(10000);
      }
    } catch (InterruptedException e) {
      log.warning("Thread interrupted - shutting down JNDI server: " + port + ", " + domain);
      startBarrier.failed(e);
    } catch (Exception e) {      ;
      log.error("Could not start JNDI server", e);
      startBarrier.failed(e);
    }
  }
}
