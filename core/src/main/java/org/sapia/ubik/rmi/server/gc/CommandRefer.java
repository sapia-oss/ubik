package org.sapia.ubik.rmi.server.gc;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.oid.OID;


/**
 * A command sent from the client indicating that it refers to a remote
 * object living on the server side.
 *
 * @author Yanick Duchesne
 */
public class CommandRefer extends RMICommand {
  
  private OID oid;

  /** Do not call; used for externalization only */
  public CommandRefer() {
  }

  /**
   * This constructor takes the object identifier of the remote object
   * to refer to.
   *
   * @param oid an {@link OID}
   */
  public CommandRefer(OID oid) {
    this.oid = oid;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    if(Log.isDebug()) {
      Log.debug(getClass(), String.format("Referring to %s from client VM: %s ", oid, vmId));
    }
    Hub.getModules().getServerTable().getGc().reference(vmId, oid);
    return null;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    super.readExternal(in);
    oid = (OID) in.readObject();
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(oid);
  }
}
