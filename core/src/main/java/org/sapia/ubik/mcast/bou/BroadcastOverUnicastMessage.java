package org.sapia.ubik.mcast.bou;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.control.ControlNotification;

public class BroadcastOverUnicastMessage extends ControlNotification {

  private RemoteEvent event;

  // DO NOT CALL: meant for serialization only
  public BroadcastOverUnicastMessage() {
  }
  
  public BroadcastOverUnicastMessage(RemoteEvent event, Set<String> targetedNodes) {
    super(targetedNodes);
    this.event = event;
  }
  
  public RemoteEvent getEvent() {
    return event;
  }
  
  @Override
  protected ControlNotification getCopy(Set<String> targetedNodes) {
    return new BroadcastOverUnicastMessage(event, targetedNodes);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    event = (RemoteEvent) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(event);
  }
}