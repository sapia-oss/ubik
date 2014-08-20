package org.sapia.ubik.rmi.server.stub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.ubik.concurrent.Spawn;
import org.sapia.ubik.util.Strings;

/**
 * An instance of this class is kept within a {@link RemoteRefStateless}
 * instance. It holds the list of {@link RemoteRefContext}s corresponding to the
 * remote objects to which the remote reference connects.
 *
 * @author yduchesne
 *
 */
class ContextList implements Contexts.UpdateListener {

  /**
   * An instance of this interface is notified when a removal occurs on a
   * {@link ContextList}.
   */
  public interface RemovalListener {

    /**
     * @param context
     *          the {@link RemoteRefContext} that was removed.
     */
    public void onRemoval(RemoteRefContext context);

  }

  /**
   * This interface is meant to isolate a {@link ThreadSpecificContextList} from the {@link ContextList}
   * to which it is associated.
   */
  public interface Callback {

    /**
     * @return the timestamp at which the {@link ContextList} behind this
     * instance was last modified.
     */
    public long getTimestamp();

    /**
     * @return the currently available {@link RemoteRefContext}s, each corresponding
     * to a remote object.
     */
    public List<RemoteRefContext> getContexts();
  }

  // ==========================================================================

  private volatile long timestamp = System.nanoTime();
  private Set<RemoteRefContext> contextSet = new HashSet<RemoteRefContext>();
  private List<RemovalListener> listeners = new ArrayList<RemovalListener>();

  /**
   * @return a {@link ThreadSpecificContextList} attached to this instance.
   */
  synchronized ThreadSpecificContextList getThreadSpecificContextList() {
    return new ThreadSpecificContextList(new Callback() {
      @Override
      public long getTimestamp() {
        return timestamp;
      }

      @Override
      public List<RemoteRefContext> getContexts() {
        return getAll();
      }
    });
  }

  /**
   * @return the time at which this instance was last modified.
   */
  long getTimestamp() {
    return timestamp;
  }

  /**
   * Replaces this instance's contexts with the list of given ones.
   *
   * @param otherContexts
   *          the {@link Collection} of {@link RemoteRefContext}s that this
   *          instance should update itself with.
   */
  synchronized void update(Collection<RemoteRefContext> otherContexts) {
    contextSet.clear();
    contextSet.addAll(otherContexts);
    timestamp = System.nanoTime();
  }

  /**
   * @param context
   *          the {@link RemoteRefContext} to add to this instance.
   * @return <code>true</code> if the context was added, <code>false</code>
   *         otherwise.
   */
  synchronized boolean add(RemoteRefContext context) {
    if (contextSet.add(context)) {
      timestamp = System.nanoTime();
      return true;
    }
    return false;
  }

  /**
   * @param context
   *          the {@link RemoteRefContext} to add to this instance.
   * @return <code>true</code> if the context was added, <code>false</code>
   *         otherwise.
   */
  synchronized boolean remove(RemoteRefContext context) {
    if (contextSet.remove(context)) {
      timestamp = System.nanoTime();
      notifyListeners(context);
      return true;
    }
    return false;
  }

  /**
   * @return the {@link List} of {@link RemoteRefContext}s held by this instance.
   */
  synchronized List<RemoteRefContext> getAll() {
    return new ArrayList<RemoteRefContext>(contextSet);
  }

  /**
   * @return the {@link Set} of {@link RemoteRefContext}s held by this instance.
   */
  synchronized Set<RemoteRefContext> getAllAsSet() {
    return new HashSet<RemoteRefContext>(contextSet);
  }


  /**
   * @return the number of {@link RemoteRefContext}s currently held by this instance.
   */
  synchronized int count() {
    return contextSet.size();
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
   *
   * @param listener
   *          a {@link RemovalListener}.
   */
  void addRemovalListener(RemovalListener listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
  }

  private void notifyListeners(final RemoteRefContext removed) {
    Spawn.run(new Runnable() {
      @Override
      public void run() {
        List<RemovalListener> toNotify = null;
        synchronized (listeners) {
          toNotify = new ArrayList<>(listeners); 
        }
        for (RemovalListener listener : toNotify) {
          listener.onRemoval(removed);
        }
      }
    });
  }

  @Override
  public synchronized String toString() {
    return Strings.toString("contexts", contextSet);
  }
}
