package org.sapia.ubik.rmi.naming.remote.archie;

import org.sapia.archie.Node;
import org.sapia.archie.NodeFactory;
import org.sapia.archie.ProcessingException;
import org.sapia.archie.sync.Synchronizer;


/**
 * A Ubik-specific {@link NodeFactory}.
 * 
 * @author Yanick Duchesne
 */
public class UbikNodeFactory implements NodeFactory {
  private Synchronizer sync;

  UbikNodeFactory(Synchronizer sync) {
    this.sync = sync;
  }

  /**
   * @see org.sapia.archie.NodeFactory#newNode()
   */
  public Node newNode() throws ProcessingException {
    UbikSyncNode node = new UbikSyncNode(this);
    node.setSynchronizer(sync);

    return node;
  }
}
