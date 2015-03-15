package org.sapia.ubik.mcast.control;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.Collects;

/**
 * The base class for control requests.
 * 
 * @author yduchesne
 * 
 */
public abstract class ControlRequest implements Externalizable, SplitteableMessage {

  private long requestId;
  private long creationTime;
  private ServerAddress masterAddress;
  private String masterNode;
  private Set<String> targetedNodes;

  /**
   * Meant for externalization only.
   */
  public ControlRequest() {
  }

  /**
   * This constructor is meant to be used by subclasses when overridding the
   * {@link #getCopy(Set)} method.
   * 
   * @param targetedNodes
   *          the {@link Set} of identifiers corresponding to the nodes to which
   *          this request is targeted.
   */
  protected ControlRequest(Set<String> targetedNodes) {
    this.targetedNodes = targetedNodes;
  }

  /**
   * @param clock
   *          the {@link SysClock} that should be used to internally set this
   *          instance's creation time.
   * @param the
   *          unique identifier that should be assigned to this request.
   * @param masterNode
   *          the identifier of the master node, which is initiating this
   *          request.
   * @param masterAddress
   *          the unicast address of the master node.
   * @param targetedNodes
   *          the {@link Set} of identifiers of the nodes to which this request
   *          should be sent.
   */
  protected ControlRequest(SysClock clock, long requestId, String masterNode, ServerAddress masterAddress, Set<String> targetedNodes) {
    this.creationTime = clock.currentTimeMillis();
    this.masterNode = masterNode;
    this.masterAddress = masterAddress;
    this.targetedNodes = targetedNodes;
  }
  
  /**
   * @param creationTime
   *          the creation time, in millis.
   * @param the
   *          unique identifier that should be assigned to this request.
   * @param masterNode
   *          the identifier of the master node, which is initiating this
   *          request.
   * @param masterAddress
   *          the unicast address of the master node.
   * @param targetedNodes
   *          the {@link Set} of identifiers of the nodes to which this request
   *          should be sent.
   */
  protected ControlRequest(long creationTime, long requestId, String masterNode, ServerAddress masterAddress, Set<String> targetedNodes) {
    this.creationTime = creationTime;
    this.masterNode = masterNode;
    this.masterAddress = masterAddress;
    this.targetedNodes = targetedNodes;
  }

  /**
   * @return the identifier of the master node - which initiated this request.
   */
  public String getMasterNode() {
    return masterNode;
  }

  /**
   * @return the unicast {@link ServerAddress} of the master node.
   */
  public ServerAddress getMasterAddress() {
    return masterAddress;
  }

  /**
   * @return the {@link Set} of identifiers of the targeted nodes.
   */
  public Set<String> getTargetedNodes() {
    return targetedNodes;
  }

  /**
   * @return request's unique identifier.
   */
  public long getRequestId() {
    return requestId;
  }

  /**
   * @return the time (in millis), at which this request was first created.
   */
  public long getCreationTime() {
    return creationTime;
  }

  /**
   * Splits this notification into multiple other ones, each targeted at a
   * subset of the original targeted nodes.
   * 
   * @return a {@link List} of {@link ControlRequest}s.
   */

  public List<SplitteableMessage> split(int batchSize) {
    List<Set<String>> batches = Collects.divideAsSets(targetedNodes, batchSize);
    List<SplitteableMessage> requests = new ArrayList<SplitteableMessage>();
    for (Set<String> batch : batches) {
      ControlRequest copy = getCopy(batch);
      copy.creationTime   = this.creationTime;
      copy.masterAddress  = this.masterAddress;
      copy.masterNode     = this.masterNode;
      copy.requestId      = this.requestId;
      requests.add(copy);
    }
    return requests;
  }

  protected abstract ControlRequest getCopy(Set<String> targetedNodes);

  @Override
  @SuppressWarnings(value = "unchecked")
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.requestId = in.readLong();
    this.creationTime = in.readLong();
    this.masterNode = in.readUTF();
    this.masterAddress = (ServerAddress) in.readObject();
    this.targetedNodes = (Set<String>) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeLong(requestId);
    out.writeLong(creationTime);
    out.writeUTF(masterNode);
    out.writeObject(masterAddress);
    out.writeObject(targetedNodes);
  }

}
