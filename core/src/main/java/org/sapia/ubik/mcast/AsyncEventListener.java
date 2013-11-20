package org.sapia.ubik.mcast;

/**
 * Specifies the behavior of listeners of {@link RemoteEvent} instances.
 * 
 * @author Yanick Duchesne
 */
public interface AsyncEventListener {
  /**
   * Receives multicast events.
   * 
   * @param evt
   *          a {@link RemoteEvent}.
   */
  public void onAsyncEvent(RemoteEvent evt);
}
