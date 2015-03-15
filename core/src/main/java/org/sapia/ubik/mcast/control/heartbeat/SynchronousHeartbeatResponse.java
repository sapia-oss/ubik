package org.sapia.ubik.mcast.control.heartbeat;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.mcast.NodeInfo;
import org.sapia.ubik.mcast.control.SynchronousControlResponse;
import org.sapia.ubik.net.ServerAddress;

/**
 * An {@link SynchronousHeartbeatResponse} is sent from a slave to the master, as a
 * confirmation that is is alive.
 * 
 * @author yduchesne
 * 
 */
public class SynchronousHeartbeatResponse extends SynchronousControlResponse {

  private Set<NodeInfo> healthyNodes = new HashSet<NodeInfo>();

  /**
   * Meant for externalization
   */
  public SynchronousHeartbeatResponse() {
  }

  /**
   * @param originNode the identifier of the node from which the response was sent.
   *          the identifier of the original {@link HeartbeatRequest} to which
   *          this response corresponds.
   * @param originAddress the unicast {@link ServerAddress} of the origin node.
   */
  public SynchronousHeartbeatResponse(String originNode, ServerAddress originAddress) {
    super(originNode, originAddress);
    healthyNodes.add(new NodeInfo(originAddress, originNode));
  }
  

  /**
   * @return the {@link Set} of node identifiers corresponding to the nodes that have replied to the health check.
   */
  public Set<NodeInfo> getHealthyNodes() {
    return healthyNodes;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    healthyNodes = (Set<NodeInfo>) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(healthyNodes);
  }

}
