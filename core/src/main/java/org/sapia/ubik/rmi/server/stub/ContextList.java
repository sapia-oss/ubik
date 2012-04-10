package org.sapia.ubik.rmi.server.stub;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.ubik.util.Strings;

/**
 * An instance of this class is kept within a {@link RemoteRefStateless} instance. It holds
 * the list of {@link RemoteRefContext}s corresponding to the remote objects to which the remote
 * reference connects.
 * 
 * @author yduchesne
 *
 */
class ContextList implements Contexts.UpdateListener {
  
  /**
   * An instance of this interface is notified when a removal occurs on a {@link ContextList}.
   */
  public interface RemovalListener {

    /**
     * @param context the {@link RemoteRefContext} that was removed.
     */
    public void onRemoval(RemoteRefContext context);
    
  }

  private Set<RemoteRefContext>  contextSet      = new HashSet<RemoteRefContext>();
  private List<RemoteRefContext> orderedContexts = new ArrayList<RemoteRefContext>();
  private List<RemovalListener>  listeners       = new ArrayList<RemovalListener>();
  
  /**
   * Replaces this instance's contexts with the list of given ones.
   * 
   * @param otherContexts the {@link Collection} of {@link RemoteRefContext}s that this instance should
   * update itself with.
   */
  synchronized void update(Collection<RemoteRefContext> otherContexts) {
    contextSet.clear();
    orderedContexts.clear();
    contextSet.addAll(otherContexts);
    orderedContexts.addAll(otherContexts);
  }
  
  /**
   * @param context the {@link RemoteRefContext} to add to this instance.
   * @return <code>true</code> if the context was added, <code>false</code> otherwise.
   */
  synchronized boolean add(RemoteRefContext context) {
    if(contextSet.add(context)) { 
      orderedContexts.add(context);
      return true;
    }
    return false;
  }

  /**
   * @param context the {@link RemoteRefContext} to add to this instance.
   * @return <code>true</code> if the context was added, <code>false</code> otherwise.
   */
  synchronized boolean remove(RemoteRefContext context) {
    if(contextSet.remove(context)) {
      orderedContexts.remove(context);
      notifyListeners(context);
      return true;
    }
    return false;
  }
  
  synchronized List<RemoteRefContext> getAll() {
    return new ArrayList<RemoteRefContext>(orderedContexts);
  }
  
  synchronized int count() {
    return orderedContexts.size();
  }
  
  /**
   * Rotates this instance's {@link RemoteRefContext}s.
   * 
   * @return the {@link RemoteRefContext} that is the first in this instance's
   * ordered list (after the internal rotation has been done).
   * @throws RemoteException
   */
  synchronized RemoteRefContext rotate() throws RemoteException {
    if (orderedContexts.size() == 0) {
      throw new RemoteException("No connection available");
    }      
    RemoteRefContext toReturn = orderedContexts.remove(0);
    orderedContexts.add(toReturn);
    return toReturn;
  }
  
  /**
   * @see Contexts.UpdateListener
   */
  @Override
  public void onUpdate(Collection<RemoteRefContext> contexts) {
    this.update(contexts);
  }
  
  /**
   * Adds the given {@link RemovalListener} to this instance.
   * @param listener a {@link RemovalListener}.
   */
  synchronized void addRemovalListener(RemovalListener listener) {
    listeners.add(listener);
  }
  
  private synchronized void notifyListeners(RemoteRefContext removed) {
    for(RemovalListener listener : listeners) {
      listener.onRemoval(removed);
    }
  }
  
  @Override
  public String toString() {
    return Strings.toString("contexts", orderedContexts);
  }
}
