package org.sapia.ubik.mcast;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Strings;
import org.sapia.ubik.util.SysClock;

/**
 * Holds state corresponding to a member node in the cluster.
 * 
 * @author yduchesne
 *
 */
public class NodeInfo implements Externalizable, Comparable<NodeInfo> {
  
  private static final int PRIME_NUMBER = 31;
  
  static final long serialVersionUID = 1L;

  public enum State {
    NORMAL, SUSPECT;
    
    public boolean isNormal() {
      return this == NORMAL;
    }
    
    public boolean isSuspect() {
      return this == SUSPECT;
    }
  }
  
  private State         state     = State.NORMAL;
  private long          touches;
  private long          timestamp = System.currentTimeMillis();
  private ServerAddress addr;
  private String        node;

  /**
   * Meant for externalization.
   */
  public NodeInfo() {
  }

  /**
   * @param addr
   *          the {@link ServerAddress} of the node to which this instance
   *          corresponds.
   * @param node
   *          the identifier of the node.
   */
  public NodeInfo(ServerAddress addr, String node) {
    this.addr = addr;
    this.node = node;
  }

  /**
   * @return this instance's {@link ServerAddress}.
   */
  public ServerAddress getAddr() {
    return addr;
  }

  /**
   * @return this instance's node identifier.
   */
  public String getNode() {
    return node;
  }
  
  /**
   * @return this instance's timestamp.
   */
  public synchronized long getTimestamp() {
    return timestamp;
  }
  
  /**
   * @return this instance's state.
   */
  public synchronized State getState() {
    return state;
  }
  
  /**
   * @return this instance.
   */
  public synchronized NodeInfo reset(SysClock clock) {
    state = State.NORMAL;
    return touch(clock);
  }
  
  /**
   * Sets this instance's state to {@link State#SUSPECT}.
   * @return this instance.
   */
  public synchronized NodeInfo suspect() {
    state = State.SUSPECT;
    return this;
  }
  
  /**
   * Internally checks this instance's state, setting it to {@link State#SUSPECT}
   * if appropriate.
   * 
   * @return this instance's state.
   */
  public synchronized State checkState(long heartbeatTimeout, SysClock clock) {
    if (clock.currentTimeMillis() - timestamp >= heartbeatTimeout) {
      state = State.SUSPECT;
    }
    return state;
  }
  
  /**
   * Modifies this instance's timestamp and touch count.
   * 
   * @return this instance.
   */
  public synchronized NodeInfo touch(SysClock clock) {
    timestamp = clock.currentTimeMillis();
    if (touches == Long.MAX_VALUE) {
      touches = 1;
    } else {
      touches++;
    }
    return this;
  }
  
  /**
   * @return the number of touches done on this instance so far.
   */
  public synchronized long getTouches() {
    return touches;
  }

  // --------------------------------------------------------------------------
  // Comparable 
  
  @Override
  public int compareTo(NodeInfo o) {
    int c = 0;
    if (timestamp < o.timestamp) {
      c = -1;
    } else if (timestamp > o.timestamp) {
      c = 1;
    } 
    return c;
  }

  // --------------------------------------------------------------------------
  // Externalizable
  
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    touches   = in.readLong();
    timestamp = in.readLong();
    addr      = (ServerAddress) in.readObject();
    node      = in.readUTF();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(touches);
    out.writeLong(timestamp);
    out.writeObject(addr);
    out.writeUTF(node);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NodeInfo) {
      NodeInfo inf = (NodeInfo) obj;

      return inf.addr.equals(addr) && inf.node.equals(node);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return addr.hashCode() * PRIME_NUMBER + node.hashCode() * PRIME_NUMBER;
  }
  
  @Override
  public String toString() {
    return Strings.toString("node", node, "addr", addr);
  }

}