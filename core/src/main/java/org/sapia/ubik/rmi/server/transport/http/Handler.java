package org.sapia.ubik.rmi.server.transport.http;

import org.simpleframework.http.core.Container;

/**
 * An instance of this interface is simply a {@link Container} that provides a
 * shutdown hook.
 * 
 * @author yduchesne
 * 
 */
public interface Handler extends Container {

  /**
   * Frees this instance's resource.
   */
  public void shutdown();

}
