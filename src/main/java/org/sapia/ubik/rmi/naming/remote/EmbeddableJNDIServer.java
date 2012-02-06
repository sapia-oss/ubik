package org.sapia.ubik.rmi.naming.remote;

import java.io.IOException;
import java.util.Properties;

import javax.naming.Context;

import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.concurrent.ThreadStartup;
import org.sapia.ubik.concurrent.ThreadShutdown;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.Hub;
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
   * @see JndiConsts#DEFAULT_MCAST_ADDR
   * @see JndiConsts#DEFAULT_MCAST_PORT
   */
  public EmbeddableJNDIServer() {
    this(JNDIServerHelper.DEFAULT_DOMAIN, JNDIServerHelper.DEFAULT_PORT,
      org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_ADDR,
      org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_PORT);
  }

  /**
   * Creates an instance of this class that will listen on the given port and domain.
   * Also internally binds its multicast event channel to the default multicast address and port.
   *
   * @see JndiConsts#DEFAULT_MCAST_ADDR
   * @see JndiConsts#DEFAULT_MCAST_PORT
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
    props.setProperty(JndiConsts.MCAST_ADDR_KEY, mcastAddress);
    props.setProperty(JndiConsts.MCAST_PORT_KEY, Integer.toString(mcastPort));
    try {
      channel = new EventChannel(domain, new Props().addProperties(props).addSystemProperties());
    } catch (IOException e) {
      throw new IllegalStateException("Could not build event channel", e);
    }
  }
  
  /**
   * @param ec the {@link EventChannel} that this instance should wrap.
   */
  public EmbeddableJNDIServer(EventChannel ec) {
    this.channel = ec;
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
   * Handles {@link JndiConsts#JNDI_CLIENT_PUBLISH} remote events. Dispatches {@link JndiConsts#JNDI_SERVER_DISCO}
   * remote events in response.
   */
  public void onAsyncEvent(RemoteEvent evt) {
    if(evt.getType().equals(JndiConsts.JNDI_CLIENT_PUBLISH)){
      try{
        channel.dispatch(JndiConsts.JNDI_SERVER_DISCO,
            new TCPAddress(Localhost.getAnyLocalAddress().getHostAddress(), port));
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
    waitStarted();
  }

  private final void doRun() {
    try {

      channel.start();
      
      channel.registerAsyncListener(JndiConsts.JNDI_CLIENT_PUBLISH, this);

      channel.dispatch(JndiConsts.JNDI_SERVER_PUBLISH,
          new TCPAddress(Localhost.getAnyLocalAddress().getHostAddress(), port));
      channel.dispatch(JndiConsts.JNDI_SERVER_DISCO,
          new TCPAddress(Localhost.getAnyLocalAddress().getHostAddress(), port));

      root = JNDIServerHelper.newRootContext(channel);

      Hub.exportObject(this, port);
      
      log.warning("Server started on port: " + port + ", domain: " + domain);

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

  private void waitStarted() throws InterruptedException, Exception {
    startBarrier.await();
  }
}
