package org.sapia.ubik.rmi.naming.remote.archie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.sapia.archie.Node;
import org.sapia.archie.ProcessingException;
import org.sapia.archie.jndi.JndiContext;
import org.sapia.ubik.mcast.EventChannelRef;
import org.sapia.ubik.rmi.naming.remote.DomainInfo;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;

/**
 * Implements a remote {@link JndiContext}.
 *
 * @author Yanick Duchesne
 */
@SuppressWarnings(value = "unchecked")
public class UbikRemoteContext extends JndiContext implements RemoteContext {

  /**
   * Notified upon bind/rebind operations. The {@link #onBind(Name, Object)} method
   * must return the object that must be exported. It allows performing substitution and
   * replacing the original object, passed as a parameter.
   */
  public interface BindListener {

    /**
     * @param name the name of the object to export.
     * @param toExport the object to import.
     * @return the object that will finally be exported.
     * @throws NamingException if a problem occurred while performing this operation.
     */
    public Object onBind(Name name, Object toExport) throws NamingException;
  }

  // --------------------------------------------------------------------------

  private DomainInfo         domain;
  private List<BindListener> listeners = Collections.synchronizedList(new ArrayList<BindListener>());

  protected UbikRemoteContext(DomainInfo domain, Node root) {
    super(root);
    this.domain = domain;
  }

  protected UbikRemoteContext(UbikSyncNode node) {
    super(node);
    EventChannelRef channel = ((UbikSynchronizer) node.getSynchronizer()).getEventChannel();
    this.domain = new DomainInfo(channel.get().getDomainName(), channel.get().getMulticastAddress());
  }

  /**
   * @see org.sapia.ubik.rmi.naming.remote.RemoteContext#getDomainInfo()
   */
  @Override
  public DomainInfo getDomainInfo() {
    return domain;
  }

  /**
   * @param listener adds the given {@link BindListener} to this instance.
   */
  public void addBindListener(BindListener listener) {
    listeners.add(listener);
  }

  /**
   * @param listener removes the given {@link BindListener} from this instance.
   */
  public void removeBindListener(BindListener listener) {
    listeners.remove(listener);
  }

  @Override
  public void bind(Name name, Object obj) throws NamingException {
    super.rebind(name, notifyBind(name, obj));
  }

  @Override
  public void bind(String name, Object obj) throws NamingException {
    super.rebind(name, notifyBind(getNameParser(name).parse(name), obj));
  }

  @Override
  public synchronized void rebind(Name name, Object obj) throws NamingException {
    super.rebind(name, notifyBind(name, obj));
  }

  @Override
  public synchronized void rebind(String name, Object obj) throws NamingException {
    super.rebind(name, notifyBind(getNameParser(name).parse(name), obj));
  }

  @Override
  protected Context newChildContext(Node node) {
    UbikSyncNode sync = (UbikSyncNode) node;

    return new UbikRemoteContext(sync);
  }

  private Object notifyBind(Name name, Object toBind) throws NamingException {
    Object remote = toBind;
    synchronized (listeners) {
      if (!listeners.isEmpty()) {
        for (BindListener l : listeners) {
          remote = l.onBind(name, remote);
        }
      }
    }
    return remote;
  }

  @Override
  @SuppressWarnings("rawtypes")
  protected NamingEnumeration newNamingEnum(Iterator entries, Iterator childNodes, int listType) {
    return new UbikNamingEnum(entries, childNodes, listType);
  }

  public static UbikRemoteContext newInstance(EventChannelRef channel) throws NamingException {
    UbikSynchronizer sync = new UbikSynchronizer(channel);
    UbikNodeFactory fac = new UbikNodeFactory(sync);

    try {
      UbikSyncNode root = new UbikSyncNode(fac);
      root.setSynchronizer(sync);
      sync.setRoot(root);

      return new UbikRemoteContext(
          new DomainInfo(channel.get().getDomainName(), channel.get().getMulticastAddress()),
          root
      );
    } catch (ProcessingException e) {
      NamingException ne = new NamingException("Could not create remote context");
      ne.setRootCause(e);
      throw ne;
    }
  }
}
