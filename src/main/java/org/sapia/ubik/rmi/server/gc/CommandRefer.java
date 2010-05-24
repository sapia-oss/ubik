package org.sapia.ubik.rmi.server.gc;

import org.sapia.ubik.rmi.server.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * A command sent from the client indicating that it refers to a remote
 * object living on the server side.
 *
 * @author Yanick Duchesne
 * 2002-08-25
 */
public class CommandRefer extends RMICommand {
  private OID _oid;

  /** Do not call; used for externalization only */
  public CommandRefer() {
  }

  /**
   * This constructor takes the object identifier of the remote object
   * to refer to.
   *
   * @param an <code>OID</code>
   */
  public CommandRefer(OID oid) {
    _oid = oid;
  }

  /**
   * @see org.sapia.ubik.rmi.server.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    Hub.serverRuntime.gc.reference(_vmId, _oid);

    return null;
  }

  /**
   * @see org.sapia.ubik.rmi.server.RMICommand#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    super.readExternal(in);
    _oid = (OID) in.readObject();
  }

  /**
   * @see org.sapia.ubik.rmi.server.RMICommand#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(_oid);
  }
}
