package org.sapia.ubik.rmi.server.gc;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.command.RMICommand;
import org.sapia.ubik.rmi.server.oid.OID;

/**
 * This command is sent by clients ({@link ClientGC} instances) that wish to
 * notify the server that they have garbage-collected remote references. The
 * server-side GC ({@link ServerGC}) updates the reference count for all object
 * identifiers it receives (which are passed in through this command).
 * 
 * @author Yanick Duchesne
 */
public class CommandGc extends RMICommand {

  private int count;
  private OID[] oids;

  /** Do not call; used for externalization only. */
  public CommandGc() {
  }

  CommandGc(OID[] oids, int count) {
    this.oids = oids;
    this.count = count;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    int i = 0;

    for (; i < count; i++) {
      Hub.getModules().getServerTable().getGc().dereference(vmId, oids[i]);
    }

    Hub.getModules().getServerRuntime().dispatchEvent(new GcEvent(super.getVmId(), super.getServerAddress(), count));

    Hub.getModules().getServerTable().getGc().touch(vmId);

    if (Log.isDebug()) {
      Log.debug(getClass(), "Cleaned " + i + " objects");
    }

    return null;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    count = in.readInt();
    oids = (OID[]) in.readObject();
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeInt(count);
    out.writeObject(oids);
  }
}
