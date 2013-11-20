package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.rmi.server.command.RMICommand;

/**
 * This command sends back a remote reference to the caller, for the server
 * listening at the port specified in the constructor.
 * 
 * @author Yanick Duchesne
 */
public class CommandConnect extends RMICommand {
  private String transportType;

  /** Do not call; used for externalization only */
  public CommandConnect() {
  }

  public CommandConnect(String transportType) {
    this.transportType = transportType;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    return Hub.getModules().getServerTable().getServerRef(transportType).getStub();
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#readExternal(java.io.ObjectInput)
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    transportType = in.readUTF();
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#writeExternal(java.io.ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeUTF(transportType);
  }
}
