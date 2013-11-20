package org.sapia.ubik.rmi.server.stub;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.RemoteRuntimeException;
import org.sapia.ubik.rmi.server.ClientRuntime;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.util.Strings;

/**
 * Encapsulates the state common to remote references.
 * 
 * @author yduchesne
 * 
 */
public class RemoteRefContext implements Externalizable {

  static final long serialVersionUID = 1L;

  private OID oid;
  private ServerAddress address;
  private boolean callback;
  private int hopCount;
  private VmId vmId = VmId.getInstance();

  protected transient volatile Connections pool;
  protected transient Object lock = new Object();

  /** Used for serialization only */
  public RemoteRefContext() {
  }

  public RemoteRefContext(OID oid, ServerAddress address) {
    this.oid = oid;
    this.address = address;
  }

  /**
   * @return this instance's {@link OID}.
   */
  public OID getOid() {
    return oid;
  }

  /**
   * @return this instance's {@link ServerAddress}.
   */
  public ServerAddress getAddress() {
    return address;
  }

  /**
   * @return this instance's {@link VmId}.
   */
  public VmId getVmId() {
    return vmId;
  }

  /**
   * 
   * @param callback
   *          <code>true</code> if this instance's corresponding stub supports
   *          callback invocations, <code>false</code>
   * 
   */
  public void setCallback(boolean callback) {
    this.callback = callback;
  }

  /**
   * @return this instance's <code>callback/code> flag.
   */
  public boolean isCallback() {
    return callback;
  }

  /**
   * @return this instance's hop count.
   */
  public int getHopCount() {
    return hopCount;
  }

  /**
   * @return <code>true</code> if this is this instance's first hop.
   */
  public boolean isFirstHop() {
    return hopCount <= 1;
  }

  /**
   * @return the {@link Connections} that this instance uses.
   */
  public Connections getConnections() {
    if (pool == null) {
      synchronized (lock) {
        if (pool == null) {
          try {
            initPool(false);
          } catch (RemoteException e) {
            throw new RemoteRuntimeException("Could not initialize connection pool", e);
          }
        }
      }
    }
    return pool;
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.oid = (OID) in.readObject();
    this.address = (ServerAddress) in.readObject();
    this.callback = in.readBoolean();
    this.hopCount = in.readInt();
    hopCount++;
    this.vmId = (VmId) in.readObject();
    createReference();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(oid);
    out.writeObject(address);
    out.writeBoolean(callback);
    out.writeInt(hopCount);
    out.writeObject(vmId);
  }

  public int hashCode() {
    return oid.hashCode();
  }

  public boolean equals(Object other) {
    if (other instanceof RemoteRefContext) {
      RemoteRefContext otherContext = (RemoteRefContext) other;
      return oid.equals(otherContext.oid);
    }
    return false;
  }

  @Override
  public String toString() {
    return Strings.toString("oid", oid, "address", address, "vmId", vmId, "callback", callback, "hopCount", hopCount);
  }

  protected Object sendCommand(RMICommand cmd) throws Throwable {
    if (pool == null) {
      synchronized (lock) {
        if (pool == null) {
          initPool(false);
        }
      }
    }

    RmiConnection conn = pool.acquire();

    try {
      try {
        conn.send(cmd);
      } catch (RemoteException e) {
        synchronized (lock) {
          pool.invalidate(conn);
          pool.clear();
        }
        conn = pool.acquire();
        conn.send(cmd);
      }

      Object toReturn = conn.receive();

      if (toReturn == null) {
        return toReturn;
      } else if (toReturn instanceof Throwable) {
        Throwable err = (Throwable) toReturn;
        err.fillInStackTrace();
        throw err;
      }

      return toReturn;
    } finally {
      pool.release(conn);
    }
  }

  protected synchronized void initPool(boolean force) throws java.rmi.RemoteException {
    if (pool == null) {
      pool = Hub.getModules().getTransportManager().getConnectionsFor(address);
    } else if (force) {
      pool.clear();
      pool = Hub.getModules().getTransportManager().getConnectionsFor(address);
    }
  }

  private void createReference() throws RemoteException {
    ClientRuntime runtime = Hub.getModules().getClientRuntime();
    if (!isFirstHop()) {
      if (Log.isDebug())
        Log.debug(getClass(), "Creating remote ref " + oid);
      runtime.createReference(address, oid);
    }
    runtime.getGc().register(address, oid, this);
  }

}
