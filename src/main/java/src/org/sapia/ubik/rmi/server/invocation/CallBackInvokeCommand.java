package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.*;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


/**
 * A method invocation command that handles call backs.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CallBackInvokeCommand extends InvokeCommand
  implements Externalizable {
  static final long     serialVersionUID = 1L;
  private boolean       _executed;
  private String        _respId;
  private ServerAddress _callBackId;

  /**
   * Do not call; used for externalization only.
   */
  public CallBackInvokeCommand() {
  }

  /**
   * Constructor for CallBackInvokeCommand.
   * @param oid the object identifier of the object on which to perform the invocation.
   * @param methodName the name of the method to call on the target object.
   * @param params the parameters of the method.
   * @param paramClasses the classes of the method's parameters (representing the method's signature).
   */
  public CallBackInvokeCommand(OID oid, String methodName, Object[] params,
    Class[] paramClasses, String transportType) {
    super(oid, methodName, params, paramClasses, transportType);
  }

  /**
   * @see org.sapia.ubik.rmi.server.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    if (!_executed) {
      _executed = true;

      if (Log.isDebug()) {
        Log.debug(getClass(), "dispatching callback command " + _respId);
      }

      Hub.serverRuntime.processor.processAsyncCommand(_respId, _vmId,
        _callBackId, this);

      return new Integer(0);
    } else {
      if (Log.isDebug()) {
        Log.debug(getClass(), "executing callback command " + _respId);
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
    _executed     = in.readBoolean();
    _respId       = (String) in.readObject();
    _callBackId   = (ServerAddress) in.readObject();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeBoolean(_executed);
    out.writeObject(_respId);
    out.writeObject(_callBackId);
  }

  final void setUp(String respId, ServerAddress callBackId) {
    _respId       = respId;
    _callBackId   = callBackId;
  }
}
