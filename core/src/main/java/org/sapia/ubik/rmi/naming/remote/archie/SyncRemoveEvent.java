package org.sapia.ubik.rmi.naming.remote.archie;

import org.sapia.archie.Name;
import org.sapia.archie.NamePart;

/**
 * Corresponds to an unbind that is disributed to other JNDI nodes.
 * 
 * @author Yanick Duchesne
 */
public class SyncRemoveEvent extends SyncEvent {

  static final long serialVersionUID = 1L;

  public SyncRemoveEvent(Name nodePath, NamePart name) {
    super(nodePath, name);
  }
}
