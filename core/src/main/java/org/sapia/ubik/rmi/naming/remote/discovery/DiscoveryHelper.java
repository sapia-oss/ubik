package org.sapia.ubik.rmi.naming.remote.discovery;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.naming.Context;
import javax.naming.NamingException;

import org.sapia.archie.Name;
import org.sapia.archie.impl.AttributeNameParser;
import org.sapia.archie.impl.AttributeNamePart;
import org.sapia.archie.impl.DefaultNameParser;
import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.naming.ServiceLocator;
import org.sapia.ubik.rmi.naming.remote.JNDIConsts;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.sapia.ubik.rmi.naming.remote.archie.SyncPutEvent;
import org.sapia.ubik.rmi.naming.remote.proxy.ContextResolver;
import org.sapia.ubik.rmi.naming.remote.proxy.DefaultContextResolver;
import org.sapia.ubik.rmi.naming.remote.proxy.LocalContext;
import org.sapia.ubik.util.Props;

/**
 * An instance of this class can be used by applications to listen for the appearance of
 * a) appearing JNDI servers; b) existing JNDI servers; and c) appearing service bindings.
 *
 * @author Yanick Duchesne
 */
public class DiscoveryHelper implements AsyncEventListener {
	
  protected Category                  log           = Log.createCategory(this.getClass());
  protected EventChannel              channel;
  private List<ServiceDiscoListener>  svclisteners  = new CopyOnWriteArrayList<ServiceDiscoListener>();
  private List<JndiDiscoListener>     jndiListeners = new CopyOnWriteArrayList<JndiDiscoListener>();
  private ContextResolver             resolver      = new DefaultContextResolver();

  /**
   * Constructor for DiscoveryHelper.
   */
  public DiscoveryHelper(EventChannel ec) throws IOException {
    channel = ec;
    initChannel();
  }

  /**
   * Creates an instance of this class that will listen for new JNDI servers and
   * for the binding of new services on the given domain.
   *
   * @param domain the name of a domain.
   */
  public DiscoveryHelper(String domain) throws IOException {
    channel = new EventChannel(domain, Props.getSystemProperties());
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
    Properties props = new Properties();
    props.setProperty(JNDIConsts.MCAST_ADDR_KEY, mcastAddr);
    props.setProperty(JNDIConsts.MCAST_PORT_KEY, Integer.toString(mcastPort));
    channel = new EventChannel(domain, new Props().addProperties(props).addSystemProperties());
    initChannel();
  }

  /**
   * @param res the {@link ContextResolver} that this instance should use
   * when notified about new JNDI servers.
   */
  public void setContextResolver(ContextResolver res){
    resolver = res;
  }  

  /**
   * Adds a service discovery listener to this instance.
   *
   * @param listener a {@link ServiceDiscoListener}.
   */
  public synchronized void addServiceDiscoListener(ServiceDiscoListener listener) {
    if(!svclisteners.contains(listener)){
      svclisteners.add(listener);
    }
  }
  
  /**
   * Removes the given service discovery listener from this instance.
   *
   * @param listener a {@link ServiceDiscoListener}.
   */
  public synchronized void removeServiceDiscoListener(ServiceDiscoListener listener) {
    svclisteners.remove(listener);
  }  

  /**
   * Adds a JNDI discovery listener to this instance.
   *
   * @param listener a {@link JndiDiscoListener}.
   */
  public synchronized void addJndiDiscoListener(JndiDiscoListener listener) {
    if(!jndiListeners.contains(listener)){
      jndiListeners.add(listener);
      try{
        channel.dispatch(JNDIConsts.JNDI_CLIENT_PUBLISH, "");
      }catch(IOException e){}      
    }
  }
  
  /**
   * Removes the given JNDI discovery listener from this instance.
   *
   * @param listener {@link JndiDiscoListener}.
   */
  public synchronized void removeJndiDiscoListener(JndiDiscoListener listener) {
    jndiListeners.remove(listener);    
  }  
  
  /**
   * @see org.sapia.ubik.mcast.AsyncEventListener#onAsyncEvent(RemoteEvent)
   */
  public void onAsyncEvent(RemoteEvent evt) {
    TCPAddress tcp;
    
    try {
      if (evt.getType().equals(JNDIConsts.JNDI_SERVER_PUBLISH)
          || evt.getType().equals(JNDIConsts.JNDI_SERVER_DISCO)) {
        tcp = (TCPAddress) evt.getData();
        
        Context remoteCtx = (Context) resolver.resolve(tcp);

        List<JndiDiscoListener> listeners = new ArrayList<JndiDiscoListener>(jndiListeners);
         
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
          listeners.get(i).onJndiDiscovered(remoteCtx);
        }
      } else if (evt.getType().equals(SyncPutEvent.class.getName())) {
        SyncPutEvent bevt = (SyncPutEvent) evt.getData();

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

          List<ServiceDiscoListener> listeners = new ArrayList<ServiceDiscoListener>(svclisteners);
          for (int i = 0; i < listeners.size(); i++) {
            listener = (ServiceDiscoListener) listeners.get(i);
            listener.onServiceDiscovered(sevt);
          }
        } catch (IOException e) {
          log.warning("Caught connection error", e);
        } catch (ClassNotFoundException e) {
          log.warning("Class not found deserializing event", e);
        }
      }
    } catch (RemoteException e) {
      log.warning("Caught connection error", e);
    } catch (IOException e) {
      log.warning("Caught IO error", e);
    }
  }

  /**
   * Closes this instance - should thereafter be discarded.
   */
  public void close() {
    channel.close();
  }

  /**
   * Returns this instance's event channel.
   */
  public EventChannel getChannel() {
    return channel;
  }
  
  void initChannel() throws IOException {
    channel.registerAsyncListener(JNDIConsts.JNDI_SERVER_PUBLISH, this);
    channel.registerAsyncListener(JNDIConsts.JNDI_SERVER_DISCO, this);
    channel.registerAsyncListener(SyncPutEvent.class.getName(), this);
    
    if(!channel.isStarted()) {
    	channel.start();
    }
  }
  
  
  protected String getJndiURI(TCPAddress addr){
    return new StringBuffer(ServiceLocator.UBIK_SCHEME)
      .append("://")
      .append(addr.getHost())
      .append(":")
      .append(addr.getPort()).toString();
  }
}
