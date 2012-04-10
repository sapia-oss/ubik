package org.sapia.ubik.rmi.server.command;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.net.Connection;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Config;
import org.sapia.ubik.rmi.server.VmId;


/**
 * This class models an executable command. Typically, a command object
 * is created on the client side, then sent to the server where it is executed.
 *
 * @author Yanick Duchesne
 */
public abstract class RMICommand extends Command implements Externalizable {
  
  protected transient Config config;
  protected VmId             vmId = VmId.getInstance();

  public RMICommand() {
  }

  /**
   * Initializes this instance with a {@link Config}  upon its
   * arrival at the server. It is a server's responsability to call this
   * method once it receives a command.
   * <p>
   * When overriding this method, <code>super.init(config)</code> must
   * be called. If a command nests another one, it should also call
   * {@link #init(Config)} on the nested command.
   *
   * @param config <code>CommandConfig</code>
   */
  public void init(Config config) {
    this.config = config;
  }

  /**
   * @return the identifier of the VM from which this command comes.
   */
  public VmId getVmId() {
    return vmId;
  }

  /**
   * Returns this command's target server address, which corresponds
   * to the server that received this command.
   *
   * @return this command's {@link ServerAddress}
   */
  public ServerAddress getServerAddress() {
    return config.getServerAddress();
  }

  /**
   * Returns this command's connection.
   *
   * @return a {@link Connection}.
   */
  public final Connection getConnection() {
    return config.getConnection();
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
    vmId = (VmId) in.readObject();
  }

  /**
   * @see java.io.Externalizable#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(vmId);
  }
}
