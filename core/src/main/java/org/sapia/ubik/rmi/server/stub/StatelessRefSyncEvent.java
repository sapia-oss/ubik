package org.sapia.ubik.rmi.server.stub;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import javax.naming.Name;

import org.sapia.ubik.mcast.MulticastAddress;
import org.sapia.ubik.util.Strings;

/**
 * Holds data corresponding to a remote reference, which was received at the JVM. This event is sent in
 * response to a newly appearing node, so that its own stub has the same end points as the corresponding
 * stub at this JVM.
 *
 * @author yduchesne
 *
 */
public class StatelessRefSyncEvent implements Externalizable {

  private Name name;
  private String domain;
  private MulticastAddress multicastAddress;
  private Set<RemoteRefContext> contexts;

  /**
   * Default ctor. MEANT FOR SERIALIZATION ONLY.
   */
  public StatelessRefSyncEvent() {
  }

  /**
   * Constructor.
   */
  public StatelessRefSyncEvent(Name name, String domain, MulticastAddress address, Set<RemoteRefContext> contexts) {
    this.name = name;
    this.domain = domain;
    this.multicastAddress = address;
    this.contexts = contexts;
  }

  /**
   * @return the {@link Set} of {@link RemoteRefContext} instances of this this instance's corresponding remote reference.
   */
  public Set<RemoteRefContext> getContexts() {
    return contexts;
  }

  /**
   * @return the {@link Domain} of the event channel of this instance's corresponding remote reference.
   */
  public String getDomain() {
    return domain;
  }

  /**
   * @return the {@link MulticastAddress} of the event channel of this instance's corresponding remote reference.
   */
  public MulticastAddress getMulticastAddress() {
    return multicastAddress;
  }

  /**
   * @return the name of the remote reference correspdonding to this instance.
   */
  public Name getName() {
    return name;
  }

  @Override
  public String toString() {
    return Strings.toString("name", name, "domain", domain, "multicastAddress", multicastAddress, "contexts", contexts);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    name = (Name) in.readObject();
    domain = in.readUTF();
    multicastAddress = (MulticastAddress) in.readObject();
    contexts = (Set<RemoteRefContext>) in.readObject();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(name);
    out.writeUTF(domain);
    out.writeObject(multicastAddress);
    out.writeObject(contexts);
  }
}