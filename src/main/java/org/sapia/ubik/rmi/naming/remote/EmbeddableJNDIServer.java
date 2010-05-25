package org.sapia.ubik.rmi.naming.remote;

import javax.naming.Context;

import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.Log;
import org.sapia.ubik.util.Localhost;


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
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class EmbeddableJNDIServer implements Runnable, RemoteContextProvider, AsyncEventListener{
  private String       _domain;
  private String       _mcastAddr;
  private int          _port;
  private int          _mcastPort;
  private Thread       _serverThread;
  private EventChannel _ec;
  private Context      _root;
  private boolean      _closed;
  private boolean      _started;
  private Exception    _startErr;

  /**
   * Creates an instance of this class that will listen on the default (1099) port,
   * on the "default" domain. Also internally binds its multicast event channel to the
   * default multicast address and port.
   *
   * @see Consts#DEFAULT_MCAST_ADDR
   * @see Consts#DEFAULT_MCAST_PORT
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
   * @see Consts#DEFAULT_MCAST_ADDR
   * @see Consts#DEFAULT_MCAST_PORT
   */
  public EmbeddableJNDIServer(String domain, int port) {
    this(domain, port, org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_ADDR,
      org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_PORT);
  }

  /**
   * Creates an instance of this class that will listen on the given port and domain.
   * Also internally binds its multicast event channel to the given multicast address and port.
   */
  public EmbeddableJNDIServer(String domain, int port, String mcastAddress,
    int mcastPort) {
    _domain      = domain;
    _port        = port;
    _mcastAddr   = mcastAddress;
    _mcastPort   = mcastPort;
  }
  
  /**
   * Returns the event channel that this instance uses to broacast events
   * and subscribe to notifications.
   *
   * @return this instance's <code>EventChannel</code>.
   */
  public EventChannel getEventChannel() {
    if (_ec == null) {
      throw new IllegalStateException("Multicast event channel not initialized");
    }

    return _ec;
  }
  
  public void onAsyncEvent(RemoteEvent evt) {
    if(evt.getType().equals(Consts.JNDI_CLIENT_PUBLISH)){
      try{
        _ec.dispatch(Consts.JNDI_SERVER_DISCO,
            new TCPAddress(Localhost.getLocalAddress().getHostAddress(), _port));
      }catch(Exception e){
        Log.warning(getClass(), "Could not dispatch JNDI server publishing event", e);
      }
    }
    
  }

  /**
   * @return this instance's root JNDI <code>Context</code>.
   */
  public Context getRootContext() {
    if (_root == null) {
      throw new IllegalStateException("Context not initialized");
    }

    return _root;
  }
  
  public RemoteContext getRemoteContext(){
    return (RemoteContext)_root;
  }

  /**
   * Stops this instance (this internally closes the <code>EventChannel</code>
   * that this instance holds).
   */
  public void stop() {
    if (_serverThread != null) {
      _serverThread.interrupt();
      waitClosed();
    } else {
      if (_ec != null) {
        _ec.close();
      }
    }
  }

  /**
   * Starts this instance.
   *
   * @param daemon if <code>true</code>, this instance will be started
   * as a daemon thread.
   */
  public void start(boolean daemon) throws Exception {
    _serverThread = new Thread(this, "ubik.jndi:" + _domain + ":" + _port);
    _serverThread.setDaemon(daemon);
    _serverThread.start();
    waitStarted();

    if (_startErr != null) {
      throw _startErr;
    }
  }

  public final void run() {
    try {
      _ec = new EventChannel(_domain, _mcastAddr, _mcastPort);

      _ec.start();
      
      _ec.registerAsyncListener(Consts.JNDI_CLIENT_PUBLISH, this);

      _ec.dispatch(Consts.JNDI_SERVER_PUBLISH,
        new TCPAddress(Localhost.getLocalAddress().getHostAddress(), _port));
      
      _ec.dispatch(Consts.JNDI_SERVER_DISCO,
          new TCPAddress(Localhost.getLocalAddress().getHostAddress(), _port));
      

      _root = JNDIServerHelper.newRootContext(_ec);

      Hub.exportObject(this, _port);
      
      Log.warning(getClass(),
        "Server started on port: " + _port + ", domain: " + _domain);

      notifyStarted();

      while (true) {
        Thread.sleep(10000);
      }
    } catch (InterruptedException e) {
      Log.warning(getClass(),
        "Shutting down JNDI server: " + _port + ", " + _domain);
      _ec.close();
      notifyClosed();
    } catch (Exception e) {
      _startErr = e;
      notifyStarted();
    }
  }

  private synchronized void notifyClosed() {
    _closed = true;
    notify();
  }

  private synchronized void notifyStarted() {
    _started = true;
    notify();
  }

  private synchronized void waitClosed() {
    try {
      while (!_closed) {
        wait();
      }
    } catch (InterruptedException e) {
      _ec.close();
    }
  }

  private synchronized void waitStarted() {
    try {
      while (!_started) {
        wait();
      }
    } catch (InterruptedException e) {
      return;
    }
  }
}
