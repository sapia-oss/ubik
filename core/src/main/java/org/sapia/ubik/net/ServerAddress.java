package org.sapia.ubik.net;

import java.io.Serializable;

/**
 * This interface models the concept of "server address", which hides the
 * transport-specific details (host, port, etc.).
 * <p>
 * Implementations of this interface should override the <code>equals</code> and
 * <code>hashCode</code> methods.
 * 
 * @author Yanick Duchesne
 */
public interface ServerAddress extends Serializable {
  /**
   * Returns the "transport type" of the server corresponding to this server
   * address. A transport type is an arbitrary, logical identifier.
   * 
   * @return a transport type, as a {@link String}.
   */
  public String getTransportType();

  /**
   * Implementations should override this method in manner consistent with the
   * method's specification (in the {@link Object} class).
   * 
   * @see Object#equals(Object obj).
   */
  public boolean equals(Object o);
}
