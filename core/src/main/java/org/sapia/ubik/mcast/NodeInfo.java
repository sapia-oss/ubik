package org.sapia.ubik.mcast;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.net.ServerAddress;

public class NodeInfo implements Externalizable {

  ServerAddress addr;
  String node;

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

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    addr = (ServerAddress) in.readObject();
    node = in.readUTF();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(addr);
    out.writeUTF(node);
  }

  public boolean equals(Object obj) {
    if (obj instanceof NodeInfo) {
      NodeInfo inf = (NodeInfo) obj;

      return inf.addr.equals(addr) && inf.node.equals(node);
    }
    return false;
  }

  public int hashCode() {
    return addr.hashCode() * 31 + node.hashCode() * 31;
  }
}