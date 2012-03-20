package org.sapia.ubik.rmi.server.command;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.oid.OID;


/**
 * A method invocation command that is executed asynchronously: it is sent from the client
 * and received at the server, where it is dispatched to the 
 * {@link CommandProcessor#processAsyncCommand(String, org.sapia.ubik.rmi.server.VmId, ServerAddress, Command)} method.  
 *
 * @author Yanick Duchesne
 */
public class CallbackInvokeCommand extends InvokeCommand
  implements Externalizable {
  
  static final long     serialVersionUID = 1L;
  
  private boolean       executed;
  private long          respId;
  private ServerAddress callBackId;

  /**
   * Do not call; used for externalization only.
   */
  public CallbackInvokeCommand() {
  }

  /**
   * @param oid the object identifier of the object on which to perform the invocation.
   * @param methodName the name of the method to call on the target object.
   * @param params the parameters of the method.
   * @param paramClasses the classes of the method's parameters (representing the method's signature).
   */
  public CallbackInvokeCommand(OID oid, String methodName, Object[] params,
    Class<?>[] paramClasses, String transportType) {
    super(oid, methodName, params, paramClasses, transportType);
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    if (!executed) {
      executed = true;

      if (Log.isDebug()) {
        Log.debug(getClass(), "dispatching callback command " + respId);
      }

      Hub.getModules().getCommandModule().getCommandProcessor().processAsyncCommand(respId, vmId, callBackId, this);

      return new Integer(0);
    } else {
      if (Log.isDebug()) {
        Log.debug(getClass(), "executing callback command " + respId);
      }

      return super.execute();
    }
  }

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    super.readExternal(in);
    executed     = in.readBoolean();
    respId       = in.readLong();
    callBackId   = (ServerAddress) in.readObject();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeBoolean(executed);
    out.writeLong(respId);
    out.writeObject(callBackId);
  }

  public final void setUp(long respId, ServerAddress callBackId) {
    this.respId       = respId;
    this.callBackId   = callBackId;
  }
}
