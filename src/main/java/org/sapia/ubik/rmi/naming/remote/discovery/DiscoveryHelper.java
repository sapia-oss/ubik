package org.sapia.ubik.rmi.naming.remote.discovery;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;

import org.sapia.archie.Name;
import org.sapia.archie.impl.AttributeNameParser;
import org.sapia.archie.impl.AttributeNamePart;
import org.sapia.archie.impl.DefaultNameParser;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.naming.ServiceLocator;
import org.sapia.ubik.rmi.naming.remote.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.sapia.ubik.rmi.naming.remote.archie.SyncPutEvent;
import org.sapia.ubik.rmi.naming.remote.proxy.ContextResolver;
import org.sapia.ubik.rmi.naming.remote.proxy.DefaultContextResolver;
import org.sapia.ubik.rmi.naming.remote.proxy.LocalContext;
import org.sapia.ubik.rmi.server.Log;

/**
 * An instance of this class can be used by applications to listen for the appearance of
 * a) appearing JNDI servers; b) existing JNDI servers; and c) appearing service bindings.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DiscoveryHelper implements AsyncEventListener {
  protected EventChannel  _channel;
  private List            _svclisteners  = Collections.synchronizedList(new ArrayList());
  private List            _jndiListeners = Collections.synchronizedList(new ArrayList());
  private ContextResolver _resolver = new DefaultContextResolver();

  /**
   * Constructor for DiscoveryHelper.
   */
  public DiscoveryHelper(EventChannel ec) {
    _channel = ec;
    initChannel();
  }

  /**
   * Creates an instance of this class that will listen for new JNDI servers and
   * for the binding of new services on the given domain.
   *
   * @param domain the name of a domain.
   */
  public DiscoveryHelper(String domain) throws IOException {
    _channel = new EventChannel(domain,
        org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_ADDR,
        org.sapia.ubik.rmi.Consts.DEFAULT_MCAST_PORT);
    _channel.start();
    initChannel();
  }
  
  /**
   * Creates an instance of this class that will listen for new JNDI servers and
   * for the binding of new services on the given domain.
   *
   * @param domain the name of a domain.
   * @param mcastAddr the multicast address to use internally.
   * @param mcastPort the multicast port on which to listen.
   */
  public DiscoveryHelper(String domain, String mcastAddr, int mcastPort)
    throws IOException {
    _channel = new EventChannel(domain,
        mcastAddr, mcastPort);
    _channel.start();
    initChannel();
  }

  /**
   * @param res the <code>ContextResolver</code> that this instance should use
   * when notified about new JNDI servers.
   */
  public void setContextResolver(ContextResolver res){
    _resolver = res;
  }  

  /**
   * Adds a service discovery listener to this instance.
   *
   * @param a <code>ServiceDiscoListener</code>.
   */
  public synchronized void addServiceDiscoListener(ServiceDiscoListener listener) {
    if(!_svclisteners.contains(listener)){
      _svclisteners.add(listener);
    }
  }
  
  /**
   * Removes the given service discovery listener from this instance.
   *
   * @param a <code>ServiceDiscoListener</code>.
   */
  public synchronized void removeServiceDiscoListener(ServiceDiscoListener listener) {
    List listeners = new ArrayList(_svclisteners);
    listeners.remove(listener);
    _svclisteners = listeners;
  }  

  /**
   * Adds a JNDI discovery listener to this instance.
   *
   * @param a <code>JndiDiscoListener</code>.
   */
  public synchronized void addJndiDiscoListener(JndiDiscoListener listener) {
    if(!_jndiListeners.contains(listener)){
      _jndiListeners.add(listener);
      try{
        _channel.dispatch(Consts.JNDI_CLIENT_PUBLISH, "");
      }catch(IOException e){}      
    }
  }
  
  /**
   * Removes the given JNDI discovery listener from this instance.
   *
   * @param a <code>JndiDiscoListener</code>.
   */
  public synchronized void removeJndiDiscoListener(JndiDiscoListener listener) {
    List listeners = new ArrayList(_jndiListeners);
    listeners.remove(listener);
    _jndiListeners = listeners;    
  }  
  
  /**
   * @see org.sapia.ubik.mcast.AsyncEventListener#onAsyncEvent(RemoteEvent)
   */
  public void onAsyncEvent(RemoteEvent evt) {
    TCPAddress tcp;
    
    try {
      if (evt.getType().equals(Consts.JNDI_SERVER_PUBLISH)
          || evt.getType().equals(Consts.JNDI_SERVER_DISCO)) {
        tcp = (TCPAddress) evt.getData();

        Context remoteCtx = (Context) _resolver.resolve(tcp);

        List listeners = new ArrayList(_jndiListeners);
         
        /*
         * TODO: this code is a hack and a refactoring will
         * be necessary
         */
        if(remoteCtx instanceof RemoteContext){
          try{
            remoteCtx = new LocalContext(getJndiURI(tcp), (RemoteContext)remoteCtx);
          }catch(NamingException e){
            Log.warning(getClass(), "Could not create local context", e);
            return;              
          }
        }
        for (int i = 0; i < listeners.size(); i++) {
          ((JndiDiscoListener) listeners.get(i)).onJndiDiscovered(remoteCtx);
        }
      } else if (evt.getType().equals(SyncPutEvent.class.getName())) {
        SyncPutEvent bevt = (SyncPutEvent) evt.getData();
        Object       obj;

        try {
          ServiceDiscoListener listener;
          Properties           props;
          String               name;
          Name                 nameObj = bevt.getNodePath().add(bevt.getName());

          if (bevt.getName() instanceof AttributeNamePart) {
            props   = ((AttributeNamePart) bevt.getName()).getAttributes();
            name    = new AttributeNameParser().asString(nameObj);
          } else {
            props   = new Properties();
            name    = new DefaultNameParser().asString(nameObj);
          }

          ServiceDiscoveryEvent sevt = new ServiceDiscoveryEvent(props, name,
              bevt.getValue());

          List listeners = new ArrayList(_svclisteners);
          for (int i = 0; i < listeners.size(); i++) {
            listener = (ServiceDiscoListener) listeners.get(i);
            listener.onServiceDiscovered(sevt);
          }
        } catch (IOException e) {
          Log.warning(getClass(), "Caught connection error", e);
        } catch (ClassNotFoundException e) {
          Log.warning(getClass(), "Class not found deserializing event", e);
        }
      }
    } catch (RemoteException e) {
      Log.warning(getClass(), "Caught connection error", e);
    } catch (IOException e) {
      Log.warning(getClass(), "Caught IO error", e);
    }
  }

  /**
   * Closes this instance - should thereafter be discarded.
   */
  public synchronized void close() {
    _channel.close();
  }

  /**
   * Returns this instance's event channel.
   */
  public synchronized EventChannel getChannel() {
    return _channel;
  }
  
  void initChannel() {
    _channel.registerAsyncListener(Consts.JNDI_SERVER_PUBLISH, this);
    _channel.registerAsyncListener(Consts.JNDI_SERVER_DISCO, this);
    _channel.registerAsyncListener(SyncPutEvent.class.getName(), this);
  }
  
  
  protected String getJndiURI(TCPAddress addr){
    return new StringBuffer(ServiceLocator.UBIK_SCHEME)
      .append("://")
      .append(addr.getHost())
      .append(":")
      .append(addr.getPort()).toString();
  }
}
