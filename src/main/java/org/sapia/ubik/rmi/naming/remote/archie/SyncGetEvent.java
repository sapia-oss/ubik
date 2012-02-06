package org.sapia.ubik.rmi.naming.remote.archie;

import org.sapia.archie.Name;
import org.sapia.archie.NamePart;


/**
 * Corresponds to a lookup that is disributed to other JNDI nodes, if lookup at a given node fails.
 * 
 * @author Yanick Duchesne
 */
public class SyncGetEvent extends SyncEvent implements java.io.Serializable {
  
  static final long serialVersionUID = 1L;
  
  public SyncGetEvent(Name nodePath, NamePart name) {
    super(nodePath, name);
  }
  
}
