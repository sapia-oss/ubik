package org.sapia.ubik.rmi.server.gc;

import org.sapia.ubik.rmi.server.*;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * This command is sent by clients (<code>ClientGC</code> instances) that wish
 * to notify the server that they have garbage-collected remote references.
 * The server-side GC (<code>ServerGC</code>) updates the reference count
 * for all object identifiers it receives (which are passed in through this
 * command).
 *
 * @author Yanick Duchesne
 * 2002-09-11
 */
public class CommandGc extends RMICommand {
  private int   _count;
  private OID[] _oids;

  /** Do not call; used for externalization only. */
  public CommandGc() {
  }

  CommandGc(OID[] oids, int count) {
    _oids    = oids;
    _count   = count;
  }

  /**
   * @see org.sapia.ubik.rmi.server.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    int i = 0;

    for (; i < _count; i++) {
      Hub.serverRuntime.gc.dereference(_vmId, _oids[i]);
    }
    
    Hub.serverRuntime.dispatchEvent(
      new GcEvent(
        super.getVmId(),
        super.getServerAddress(), 
        _count
      )
    );

    Hub.serverRuntime.gc.touch(_vmId);

    if (Log.isDebug()) {
      Log.debug(getClass(), "cleaned " + i + " objects");
    }

    return null;
  }

  /**
   * @see org.sapia.ubik.rmi.server.RMICommand#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    super.readExternal(in);
    _count   = in.readInt();
    _oids    = (OID[]) in.readObject();
  }

  /**
   * @see org.sapia.ubik.rmi.server.RMICommand#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeInt(_count);
    out.writeObject(_oids);
  }
}
