package org.sapia.ubik.rmi.server;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.command.Command;


/**
 * This class models an executable command. Typically, a command object
 * is created on the client side, then sent to the server where it is executed.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class RMICommand extends Command implements Externalizable {
  protected transient Config _config;
  protected VmId             _vmId = VmId.getInstance();

  public RMICommand() {
  }

  /**
   * Initializes this instance with a <code>CommandConfig</code> upon its
   * arrival at the server. It is a server's responsability to call this
   * method once it receives a command.
   * <p>
   * When overriding this method, <code>super.init(config)</code> must
   * be called. If a command nests another one, it should also call
   * <code>init()</code> on the nested command.
   *
   * @param config <code>CommandConfig</code>
   */
  public void init(Config config) {
    _config = config;
  }

  /**
   * Returns the identifier of the VM from which this command comes from.
   *
   * @return a <code>VmId</code>.
   */
  public VmId getVmId() {
    return _vmId;
  }

  /**
   * Returns this command's target server address, which corresponds
   * to the server that received this command.
   *
   * @return this command's <code>ServerAddress</code>
   */
  public ServerAddress getServerAddress() {
    return _config.getServerAddress();
  }

  /**
   * Returns this command's connection.
   *
   * @return a <code>Connection</code>
   */
  public final Connection getConnection() {
    return _config.getConnection();
  }

  /**
   * Executes this command.
   *
   * @throws Throwable if an error occurs while executing this command
   */
  public abstract Object execute() throws Throwable;

  /**
   * @see java.io.Externalizable#readExternal(ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    _vmId = (VmId) in.readObject();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(_vmId);
  }
}
