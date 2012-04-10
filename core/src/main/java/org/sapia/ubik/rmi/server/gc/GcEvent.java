package org.sapia.ubik.rmi.server.gc;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.rmi.server.VmId;


/**
 * A server-side event that can be intercepted for various purposes,
 * mainly monitoring: it signals that a client GC has synchronized
 * with the server GC.
 *
 * @see org.sapia.ubik.rmi.server.gc.ClientGC
 * @see org.sapia.ubik.rmi.server.gc.ServerGC
 *
 * @author Yanick
 */
public class GcEvent implements Event {
  private VmId          originId;
  private ServerAddress originAddress;
  private int           count;

  /**
   * Creates an instance of this class with the given parameters.
   *
   * @param originAddr the {@link ServerAddress} of the client from which
   * the GC event comes.
   *
   */
  GcEvent(VmId id, ServerAddress originAddr, int objCount) {
    this.originId      = id;
    this.originAddress = originAddr;
    this.count         = objCount;
  }

  /**
   * Returns the address of the host from which the GC synchronization
   * call comes.
   *
   * @return a {@link ServerAddress}
   */
  public ServerAddress getOriginAddress() {
    return originAddress;
  }
  
  /**
   * @return the {@link VmId} of the JVM by which the GC event was
   * triggered.
   */
  public VmId getOriginId(){
    return originId;
  }

  /**
   * Returns the number of objects that where garbage collected at
   * the client.
   *
   * @return a number of GC'ed objects.
   */
  public int getCleanedCount() {
    return count;
  }
}
