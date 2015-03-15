package org.sapia.ubik.mcast.control;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sapia.ubik.log.Category;
import org.sapia.ubik.log.Log;
import org.sapia.ubik.util.Collects;

/**
 * Base class for splitteable {@link SynchronousControlRequest}s.
 * 
 * @author yduchesne
 *
 */
public abstract class SplitteableSynchronousControlRequest extends SynchronousControlRequest implements Externalizable, SplitteableMessage {

  private Category    log = Log.createCategory(getClass());
  private Set<String> targetedNodes;

  /** 
   * DO NOT CALL: meant for externalization only. 
   */
  public SplitteableSynchronousControlRequest() {
  }
  
  public SplitteableSynchronousControlRequest(Set<String> targetedNodes) {
    this.targetedNodes = targetedNodes;
  }
  
  @Override
  public Set<String> getTargetedNodes() {
    return targetedNodes;
  }
  
  /**
   * Splits this notification into multiple other ones, each targeted at a
   * subset of the original targeted nodes.
   * 
   * @return a {@link List} of {@link ControlRequest}s.
   */
  public List<SplitteableMessage> split(int batchSize) {
    List<Set<String>> batches = Collects.divideAsSets(targetedNodes, batchSize);
    log.debug("Got %s targeted nodes subdivided in %s batches", targetedNodes.size(), batches.size());
    List<SplitteableMessage> requests = new ArrayList<SplitteableMessage>();
    for (Set<String> batch : batches) {
      SplitteableSynchronousControlRequest copy = getCopy(batch);
      requests.add(copy);
    }
    log.debug("Split request in %s", requests.size());
    return requests;
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.targetedNodes = (Set<String>) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(targetedNodes);
  }
  
  protected abstract SplitteableSynchronousControlRequest getCopy(Set<String> targetedNodes);
}
