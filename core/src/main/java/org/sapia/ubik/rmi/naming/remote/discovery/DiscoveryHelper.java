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
import org.sapia.ubik.mcast.EventChannelRef;
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
 * An instance of this class can be used by applications to listen for the
 * appearance of a) appearing JNDI servers; b) existing JNDI servers; and c)
 * appearing service bindings.
 *
 * @author Yanick Duchesne
 */
public class DiscoveryHelper implements AsyncEventListener {

  protected Category log = Log.createCategory(this.getClass());
  protected EventChannelRef channelRef;
  private List<ServiceDiscoListener> svclisteners = new CopyOnWriteArrayList<ServiceDiscoListener>();
  private List<JndiDiscoListener> jndiListeners = new CopyOnWriteArrayList<JndiDiscoListener>();
  private ContextResolver resolver = new DefaultContextResolver();

  public DiscoveryHelper(EventChannelRef ec) throws IOException {
    channelRef = ec;
    initChannel();
  }

  /**
   * Creates an instance of this class that will listen for new JNDI servers and
   * for the binding of new services on the given domain.
   *
   * @param domain
   *          the name of a domain.
   */
  public DiscoveryHelper(String domain) throws IOException {
    channelRef = new EventChannel(domain, Props.getSystemProperties()).getReference();
    initChannel();
  }

  /**
   * Creates an instance of this class that will listen for new JNDI servers and
   * for the binding of new services on the given domain.
   *
   * @param domain
   *          the name of a domain.
   * @param mcastAddr
   *          the multicast address to use internally.
   * @param mcastPort
   *          the multicast port on which to listen.
   */
  public DiscoveryHelper(String domain, String mcastAddr, int mcastPort) throws IOException {
    Properties props = new Properties();
    props.setProperty(JNDIConsts.MCAST_ADDR_KEY, mcastAddr);
    props.setProperty(JNDIConsts.MCAST_PORT_KEY, Integer.toString(mcastPort));
    channelRef = new EventChannel(domain, new Props().addProperties(props).addSystemProperties()).getReference();
    initChannel();
  }

  /**
   * @param res
   *          the {@link ContextResolver} that this instance should use when
   *          notified about new JNDI servers.
   */
  public void setContextResolver(ContextResolver res) {
    resolver = res;
  }

  /**
   * Adds a service discovery listener to this instance.
   *
   * @param listener
   *          a {@link ServiceDiscoListener}.
   */
  public synchronized void addServiceDiscoListener(ServiceDiscoListener listener) {
    if (!svclisteners.contains(listener)) {
      svclisteners.add(listener);
    }
  }

  /**
   * Removes the given service discovery listener from this instance.
   *
   * @param listener
   *          a {@link ServiceDiscoListener}.
   */
  public synchronized void removeServiceDiscoListener(ServiceDiscoListener listener) {
    svclisteners.remove(listener);
  }

  /**
   * Adds a JNDI discovery listener to this instance.
   *
   * @param listener
   *          a {@link JndiDiscoListener}.
   */
  public synchronized void addJndiDiscoListener(JndiDiscoListener listener) {
    if (!jndiListeners.contains(listener)) {
      jndiListeners.add(listener);
      try {
        channelRef.get().dispatch(JNDIConsts.JNDI_CLIENT_PUBLISH, "");
      } catch (IOException e) {
      }
    }
  }

  /**
   * Removes the given JNDI discovery listener from this instance.
   *
   * @param listener
   *          {@link JndiDiscoListener}.
   */
  public synchronized void removeJndiDiscoListener(JndiDiscoListener listener) {
    jndiListeners.remove(listener);
  }

  @Override
  public void onAsyncEvent(RemoteEvent evt) {
    try {
      if (evt.getType().equals(JNDIConsts.JNDI_SERVER_PUBLISH) || evt.getType().equals(JNDIConsts.JNDI_SERVER_DISCO)) {
        processJndiServerEvent(evt);
      } else if (evt.getType().equals(SyncPutEvent.class.getName())) {
        SyncPutEvent bevt = (SyncPutEvent) evt.getData();

        try {
          ServiceDiscoListener listener;
          Properties props;
          String name;
          Name nameObj = bevt.getNodePath().add(bevt.getName());

          if (bevt.getName() instanceof AttributeNamePart) {
            props = ((AttributeNamePart) bevt.getName()).getAttributes();
            name = new AttributeNameParser().asString(nameObj);
          } else {
            props = new Properties();
            name = new DefaultNameParser().asString(nameObj);
          }

          ServiceDiscoveryEvent sevt = new ServiceDiscoveryEvent(props, name, bevt.getValue());

          List<ServiceDiscoListener> listeners = new ArrayList<ServiceDiscoListener>(svclisteners);
          for (int i = 0; i < listeners.size(); i++) {
            listener = listeners.get(i);
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

  private void processJndiServerEvent(RemoteEvent evt) {
    try {
      TCPAddress tcp = (TCPAddress) evt.getData();

      Context remoteCtx = resolver.resolve(tcp);

      List<JndiDiscoListener> listeners = new ArrayList<JndiDiscoListener>(jndiListeners);

      /*
       * TODO: this code is a hack and a refactoring will be necessary
       */
      if (remoteCtx instanceof RemoteContext) {
        try {
          remoteCtx = new LocalContext(getJndiURI(tcp), (RemoteContext) remoteCtx);
        } catch (NamingException e) {
          log.warning("Could not create local context", e);
        }
      }
      for (int i = 0; i < listeners.size(); i++) {
        listeners.get(i).onJndiDiscovered(remoteCtx);
      }

    } catch (Exception e) {
      log.error("Could not process remote event: %s", e, evt.getType());
    }
  }

  /**
   * Closes this instance - should thereafter be discarded.
   */
  public void close() {
    channelRef.get().close();
  }

  /**
   * Returns this instance's {@link EventChannelRef}.
   */
  public EventChannelRef getChannel() {
    return channelRef;
  }

  void initChannel() throws IOException {
    channelRef.get().registerAsyncListener(JNDIConsts.JNDI_SERVER_PUBLISH, this);
    channelRef.get().registerAsyncListener(JNDIConsts.JNDI_SERVER_DISCO, this);
    channelRef.get().registerAsyncListener(SyncPutEvent.class.getName(), this);

    if (!channelRef.get().isStarted()) {
      channelRef.get().start();
    }
  }

  protected String getJndiURI(TCPAddress addr) {
    return new StringBuffer(ServiceLocator.UBIK_SCHEME).append("://").append(addr.getHost()).append(":").append(addr.getPort()).toString();
  }
}
