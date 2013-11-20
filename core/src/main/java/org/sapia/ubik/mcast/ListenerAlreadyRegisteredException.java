package org.sapia.ubik.mcast;

/**
 * Thrown when a synchronous event listener has already been registered for a
 * given event type.
 * 
 * @see org.sapia.ubik.mcast.EventConsumer#registerSyncListener(String,
 *      SyncEventListener)
 * @see org.sapia.ubik.mcast.EventChannel#registerAsyncListener(String,
 *      AsyncEventListener)
 * 
 * @author Yanick Duchesne
 */
public class ListenerAlreadyRegisteredException extends Exception {

  static final long serialVersionUID = 1L;

  /**
   * Constructor for ListenerAlreadyRegisteredException.
   */
  public ListenerAlreadyRegisteredException(String msg) {
    super(msg);
  }
}
