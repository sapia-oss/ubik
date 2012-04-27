package org.sapia.ubik.rmi.naming.remote.archie;

import java.util.Iterator;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.sapia.archie.Node;
import org.sapia.archie.ProcessingException;
import org.sapia.archie.jndi.JndiContext;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.naming.remote.DomainInfo;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;


/**
 * Implements a remote {@link JndiContext}.
 * 
 * @author Yanick Duchesne
 */
@SuppressWarnings(value = "unchecked")
public class UbikRemoteContext extends JndiContext implements RemoteContext {
  
  private DomainInfo domain;

  protected UbikRemoteContext(DomainInfo domain, Node root) {
    super(root);
    this.domain = domain;
  }

  protected UbikRemoteContext(UbikSyncNode node) {
    super(node);
    EventChannel channel = ((UbikSynchronizer) node.getSynchronizer()).getEventChannel();
    this.domain = new DomainInfo(channel.getDomainName(), channel.getMulticastAddress());
  }

  /**
   * @see org.sapia.ubik.rmi.naming.remote.RemoteContext#getDomainInfo()
   */
  public DomainInfo getDomainInfo() {
    return domain;
  }

  /**
   * @see javax.naming.Context#bind(javax.naming.Name, java.lang.Object)
   */
  public void bind(Name name, Object obj) throws NamingException {
    super.rebind(name, obj);
  }

  /**
   * @see javax.naming.Context#bind(java.lang.String, java.lang.Object)
   */
  public void bind(String name, Object obj) throws NamingException {
    super.rebind(name, obj);
  }

  protected Context newChildContext(Node node) {
    UbikSyncNode sync = (UbikSyncNode) node;

    return new UbikRemoteContext(sync);
  }

  @SuppressWarnings("rawtypes")
  protected NamingEnumeration newNamingEnum(Iterator entries,
    Iterator childNodes, int listType) {
    return new UbikNamingEnum(entries, childNodes, listType);
  }

  public static UbikRemoteContext newInstance(EventChannel channel)
    throws NamingException {
    UbikSynchronizer sync = new UbikSynchronizer(channel);
    UbikNodeFactory  fac = new UbikNodeFactory(sync);

    try {
      UbikSyncNode root = new UbikSyncNode(fac);
      root.setSynchronizer(sync);
      sync.setRoot(root);

      return new UbikRemoteContext(new DomainInfo(channel.getDomainName(), channel.getMulticastAddress()), root);
    } catch (ProcessingException e) {
      NamingException ne = new NamingException("Could not create remote context");
      ne.setRootCause(e);
      throw ne;
    }
  }
}
