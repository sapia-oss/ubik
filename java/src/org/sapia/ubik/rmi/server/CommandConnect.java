package org.sapia.ubik.rmi.server;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * This command sends back a remote reference to the caller, for the server
 * listening at the port specified in the constructor.
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CommandConnect extends RMICommand {
  private String _transportType;

  /** Do not call; used for externalization only */
  public CommandConnect() {
  }

  public CommandConnect(String transportType) {
    _transportType = transportType;
  }

  /**
   * @see org.sapia.ubik.rmi.server.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    ServerRef ref = Hub.serverRuntime.server.getServerRef(_transportType);
    Hub.serverRuntime.gc.registerRef(_vmId, ref.oid, ref.server);

    return ref.stub;
  }

  /**
   * @see org.sapia.ubik.rmi.server.RMICommand#readExternal(java.io.ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    super.readExternal(in);
    _transportType = in.readUTF();
  }

  /**
   * @see org.sapia.ubik.rmi.server.RMICommand#writeExternal(java.io.ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeUTF(_transportType);
  }
}
