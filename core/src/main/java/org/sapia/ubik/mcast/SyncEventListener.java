package org.sapia.ubik.mcast;


/**
 * Specifies the behavior of listeners of {@link RemoteEvent} instances that are expected
 * to return a response.
 *
 * @author Yanick Duchesne
 */
public interface SyncEventListener {
  /**
   * Receives multicast events.
   *
   * @param evt {@link RemoteEvent}.
   * @return an {@link Object} consisting of
   * the synchronous response that is expected from this
   * intance. This method can return <code>null</code>, if
   * such can be the case. If an exception must be signaled
   * to the caller, then it should be returned.
   */
  public Object onSyncEvent(RemoteEvent evt);
}
