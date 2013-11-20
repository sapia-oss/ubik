package org.sapia.ubik.rmi.server.stub;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sapia.ubik.util.Collections2;
import org.sapia.ubik.util.Function;
import org.sapia.ubik.util.Strings;

/**
 * An instance of this class holds {@link RemoteRefContext} instances, kept on a
 * per-domain and per-object name basis within a {@link StatelessStubTable}.
 * 
 * @author yduchesne
 * 
 */
class Contexts {

  /**
   * Notified when the list of {@link RemoteRefContext} within a
   * {@link Contexts} instance changes (notification does not occur in the
   * context of removals).
   */
  public interface UpdateListener {

    /**
     * @param contexts
     *          the {@link Collection} of {@link RemoteRefContext} in the
     *          {@link Contexts} instance from which the notification
     *          originates.
     */
    public void onUpdate(Collection<RemoteRefContext> contexts);

  }

  protected List<WeakReference<RemoteRefContext>> contexts = new ArrayList<WeakReference<RemoteRefContext>>();
  protected List<WeakReference<UpdateListener>> listeners = new ArrayList<WeakReference<UpdateListener>>();

  Contexts() {
  }

  /**
   * @param listener
   *          an {@link UpdateListener}.
   */
  synchronized void addUpdateListener(UpdateListener listener) {
    listeners.add(new WeakReference<UpdateListener>(listener));
  }

  /**
   * @param toAdd
   *          the {@link RemoteRefContext}s to add to this instance.
   */
  synchronized void addAll(Collection<RemoteRefContext> toAdd) {

    Set<RemoteRefContext> currentSet = Collections2.convertAsSet(contexts, new Function<RemoteRefContext, WeakReference<RemoteRefContext>>() {
      @Override
      public RemoteRefContext call(WeakReference<RemoteRefContext> ref) {
        return ref.get();
      }
    });

    for (RemoteRefContext c : toAdd) {
      currentSet.add(c);
    }

    contexts = Collections2.convertAsList(currentSet, new Function<WeakReference<RemoteRefContext>, RemoteRefContext>() {
      @Override
      public java.lang.ref.WeakReference<RemoteRefContext> call(RemoteRefContext ctx) {
        return new WeakReference<RemoteRefContext>(ctx);
      }
    });

    notifyListeners();
  }

  /**
   * @return this instance's collection of {@link RemoteRefContext}s.
   */
  synchronized Collection<RemoteRefContext> getContexts() {
    return Collections2.convertAsList(contexts, new Function<RemoteRefContext, WeakReference<RemoteRefContext>>() {
      @Override
      public RemoteRefContext call(WeakReference<RemoteRefContext> ref) {
        return ref.get();
      }
    });
  }

  /**
   * @param toRemove
   *          the {@link RemoteRefContext} to remove from this instance.
   */
  synchronized void remove(RemoteRefContext toRemove) {

    for (int i = 0; i < contexts.size(); i++) {
      WeakReference<RemoteRefContext> ref = contexts.get(i);
      RemoteRefContext ctx = ref.get();
      if (ctx == null) {
        contexts.remove(i--);
      } else if (ctx.equals(toRemove)) {
        contexts.remove(i--);
        break;
      }
    }

  }

  /**
   * @return the number of {@link RemoteRefContext}s held by this instance.
   */
  synchronized int count() {
    return contexts.size();
  }

  private void notifyListeners() {
    Collection<RemoteRefContext> updated = getContexts();
    for (int i = 0; i < listeners.size(); i++) {
      WeakReference<UpdateListener> listenerRef = listeners.get(i);
      UpdateListener listener = listenerRef.get();
      if (listener == null) {
        listeners.remove(i--);
      } else {
        listener.onUpdate(updated);
      }
    }
  }

  @Override
  public String toString() {
    return Strings.toString("contexts", contexts);
  }

}