package org.sapia.ubik.rmi.naming.remote.archie;

import java.util.Iterator;

import javax.naming.Context;

import org.sapia.archie.Node;
import org.sapia.archie.jndi.JndiNamingEnum;

/**
 * Inherits from {@link JndiNamingEnum} and overriddes the
 * {@link #newJndiContext(Node)} method.
 * 
 * @author Yanick Duchesne
 */
public class UbikNamingEnum extends JndiNamingEnum implements java.rmi.Remote {

  @SuppressWarnings("rawtypes")
  public UbikNamingEnum(Iterator entries, Iterator nodes, int listType) {
    super(entries, nodes, listType);
  }

  /**
   * Wraps the given node in a {@link UbikRemoteContext}.
   * 
   * @param node
   *          the {@link Node} for which to return a {@link Context}.
   * @return a {@link UbikRemoteContext}.
   */
  protected Context newJndiContext(Node node) {
    return new UbikRemoteContext((UbikSyncNode) node);
  }
}
