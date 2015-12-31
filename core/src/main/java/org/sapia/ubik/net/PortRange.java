package org.sapia.ubik.net;

import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Strings;

/**
 * Models a port range.
 * 
 * @author yduchesne
 */
public class PortRange {

  private int minPort;
  
  private int maxPort;
  
  public PortRange(int minPort, int maxPort) {
    Assertions.isTrue(minPort < maxPort, "Expected min port to be greater than max port. Got: %s - %s", minPort, maxPort);
    Assertions.isFalse(minPort < 0, "Expected min port to be positive number. Got: %s", minPort);
    Assertions.isFalse(maxPort < 0, "Expected max port to be positive number. Got: %s", maxPort);
    this.minPort = minPort;
    this.maxPort = maxPort;
  }
  
  /**
   * @return this instance's min port.
   */
  public int getMaxPort() {
    return maxPort;
  }
  
  /**
   * @return this instance's max port.
   */
  public int getMinPort() {
    return minPort;
  }
  
  /**
   * Takes a port range literal, of the form: <code>[min-port - max-port]</code>
   * 
   * @param literal a {@link PortRange} literal.
   * @return a new {@link PortRange}, corresponding to the given literal.
   */
  public static PortRange valueOf(String literal) {
    String[] parts = Strings.split(literal, '-', true, new char[]{'[', ']'});
    Assertions.isTrue(parts.length == 2, "Expected <min_port>-<max_port> in port range literal. Got: %s", literal);
    int minPort = Integer.parseInt(parts[0]);
    int maxPort = Integer.parseInt(parts[1]);
    return new PortRange(minPort, maxPort);
  }

  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return minPort * 31 + maxPort * 31;
  }
   
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PortRange) {
      PortRange other = (PortRange) obj;
      return minPort == other.minPort && maxPort == other.maxPort;
    }
    return false;
  }
  
  @Override
  public String toString() {
    return new StringBuilder().append('[').append(minPort).append(" - ").append(maxPort).append(']').toString();
  }

}
