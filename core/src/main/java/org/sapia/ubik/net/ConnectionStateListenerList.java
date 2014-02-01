package org.sapia.ubik.net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements thread-safe logic for managing lists of {@link ConnectionStateListener}s. Also conveniently implements the
 * {@link ConnectionStateListener} interface, delegating the relevant callbacks to the internal listeners.
 * 
 * @author yduchesne
 *
 */
public class ConnectionStateListenerList implements ConnectionStateListener {
  
  private List<ConnectionStateListener> listeners = Collections.synchronizedList(new ArrayList<ConnectionStateListener>());
  
  /**
   * @param listener adds the given {@link ConnectionStateListener}.
   */
  public void add(ConnectionStateListener listener) {
    synchronized (listeners) {
      if (!listeners.contains(listener)) {
        listeners.add(listener);
      }
    }
  }
  
  /**
   * @param listener removes the given {@link ConnectionStateListener} from this instance.
   */
  public void remove(ConnectionStateListener listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }
  
  /**
   * Invokes {@link ConnectionStateListener#onConnected()} on this instance's listeners.
   */
  public void notifyConnected() {
    synchronized (listeners) { 
      for (ConnectionStateListener csl : listeners) {
        csl.onConnected();
      }
    }
  }
  
  /**
   * Invokes {@link ConnectionStateListener#onReconnected()} on this instance's listeners.
   */
  public void notifyReconnected() {
    synchronized (listeners) { 
      for (ConnectionStateListener csl : listeners) {
        csl.onReconnected();
      }
    }
  }
  
  /**
   * Invokes {@link ConnectionStateListener#onDisconnected()} on this instance's listeners.
   */
  public void notifyDisconnected() {
    synchronized (listeners) { 
      for (ConnectionStateListener csl : listeners) {
        csl.onDisconnected();
      }
    }
  }
  
  @Override
  public void onConnected() {
    notifyConnected();
  }
  
  @Override
  public void onDisconnected() {
    notifyDisconnected();
  }
  
  public void onReconnected() {
    notifyReconnected();
  }
}
