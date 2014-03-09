package org.sapia.ubik.rmi.server.stub;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.sapia.ubik.rmi.server.stub.ContextList.Callback;

/** 
 * An instance of this class synchronizes its state with the {@link ContextList} from
 * which it was created. It is meant to be kept in a {@link ThreadLocal}, to be used an
 * thread-safe manner without incurring locks.
 * 
 * @author yduchesne
 *
 */
class ThreadSpecificContextList {
  
  private long timestamp;
  private ContextList.Callback   owner;
  private List<RemoteRefContext> contexts = new ArrayList<>();

  /**
   * @param owner a {@link Callback} abstracting the {@link ContextList} 
   * to which this instance corresponds.
   */
  ThreadSpecificContextList(ContextList.Callback owner) {
    this.owner = owner;
    timestamp  = owner.getTimestamp();
  }
  
  /**
   * @return a {@link RemoteRefContext} instance.
   * @throws RemoteException no {@link RemoteRefContext} corresponding to a remote
   * object is currently available.
   */
  RemoteRefContext roundrobin() throws RemoteException {
    if (timestamp != owner.getTimestamp()) {
      contexts.clear();
      contexts.addAll(owner.getContexts());
      timestamp = owner.getTimestamp();
    }
    if (contexts.size() == 0) {
      throw new RemoteException("No connection available");
    }
    RemoteRefContext toReturn = contexts.remove(0);
    contexts.add(toReturn);
    return toReturn;
  }
}
