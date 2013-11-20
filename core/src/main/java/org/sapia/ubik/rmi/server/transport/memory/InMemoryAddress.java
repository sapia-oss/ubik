package org.sapia.ubik.rmi.server.transport.memory;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Strings;

/**
 * Represents the address of an in-memory server.
 * 
 * @see InMemoryTransportProvider
 * @see InMemoryServer
 * @author yduchesne
 * 
 */
public class InMemoryAddress implements ServerAddress {

  static final long serialVersionUID = 1L;

  public static final String TRANSPORT_TYPE = "in-memory";

  private String name;

  /**
   * @param name
   *          the name that identifies the in-memory server to which this
   *          instance corresponds.
   */
  public InMemoryAddress(String name) {
    this.name = name;
  }

  /**
   * @return the name of the in-memory server to which this instance
   *         corresponds.
   */
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof InMemoryAddress) {
      InMemoryAddress other = (InMemoryAddress) obj;
      return name.equals(other.name);
    }
    return false;
  }

  @Override
  public String getTransportType() {
    return TRANSPORT_TYPE;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, "name", name, "transportType", getTransportType());
  }
}
