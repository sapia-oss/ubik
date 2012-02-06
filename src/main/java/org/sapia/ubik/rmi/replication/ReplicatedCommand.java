package org.sapia.ubik.rmi.replication;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.command.InvokeCommand;


/**
 * Wraps an {@link InvokeCommand} that is intented to be replicated to the different
 * servers in a domain or cluster.
 *
 * @author Yanick Duchesne
 */
public abstract class ReplicatedCommand extends InvokeCommand {
  private Set<ServerAddress>  visited     = new HashSet<ServerAddress>();
  private Set<ServerAddress>  targets;
  private ReplicatedInvoker   invoker;
  private boolean             executed;
  private boolean             synchronous;
  private boolean             disabled;

  /** Do not call; used for externalization only. */
  public ReplicatedCommand() {
  }

  /**
   * @param cmd the {@link InvokeCommand} to replicate.
   * @param targets the {@link Set} of {@link ServerAddress}es that are
   * targeted by the command. If <code>null</code>, then all siblings will be targeted.
   * @param invoker the {@link ReplicatedInvoker} implementation in charge of
   * performing replicated method invocations.
   */
  public ReplicatedCommand(
      InvokeCommand cmd, 
      Set<ServerAddress> targets,
      ReplicatedInvoker invoker, 
      boolean synchronous) {
    super(cmd.getOID(), cmd.getMethodName(), cmd.getParams(), cmd.getParameterTypes(), null);
    this.targets       = targets;
    this.invoker       = invoker;
    this.synchronous   = synchronous;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    if (disabled) {
      return super.execute();
    }

    Object toReturn;
    Hub.getModules().getServerRuntime().getDispatcher().dispatch(new ReplicationEvent(this));

    Set<ServerAddress>  siblings = invoker.getSiblings();
    ReplicationStrategy strat    = new ReplicationStrategy(visited, targets, siblings);
    ServerAddress       addr;
    ServerAddress       current  = getServerAddress();

    if (executed) {
      convertParams(invoker.getClass().getClassLoader());
      toReturn = invoker.invoke(super.getMethodName(), super.getParameterTypes(), super.getParams());
    } else {
      if (targets != null) {
        if (targets.contains(current)) {
          toReturn    = super.execute();
          executed   = true;
          targets.remove(current);
        } else {
          executed   = true;
          toReturn    = send(strat.selectNextSibling());

          return toReturn;
        }
      } else {
        toReturn    = super.execute();
        executed   = true;
      }

      if ((addr = strat.selectNextSibling()) != null) {
        if (!disabled) {
          send(addr);
        }
      }
    }

    return toReturn;
  }

  /**
   * @return the {@link ReplicatedInvoker} that this instance holds.
   */
  public ReplicatedInvoker getReplicatedInvoker() {
    return invoker;
  }

  /**
   * Disables replication behavior (this command will execute as a normal
   * <code>InvokeCommand</code>).
   */
  public void disable() {
    disabled = true;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.InvokeCommand#readExternal(java.io.ObjectInput)
   */
  @SuppressWarnings(value="unchecked")
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    super.readExternal(in);
    visited       = (Set<ServerAddress>) in.readObject();
    targets       = (Set<ServerAddress>) in.readObject();
    invoker       = (ReplicatedInvoker) in.readObject();
    executed      = in.readBoolean();
    synchronous   = in.readBoolean();
    disabled      = in.readBoolean();
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.InvokeCommand#writeExternal(java.io.ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(visited);
    out.writeObject(targets);
    out.writeObject(invoker);
    out.writeBoolean(executed);
    out.writeBoolean(synchronous);
    out.writeBoolean(disabled);
  }

  /**
   * This method synchronously or asynchronously sends this instance to the server
   * at the given address.
   * @param next the <code>ServerAddress</code> of the "next" server to which this
   * instance should be sent.
   * @return the result of the invocation.
   * @throws RemoteException if a problem occurs sending this instance.
   */
  protected Object send(ServerAddress next) throws RemoteException {
    SendHelper helper = new SendHelper(this, next, synchronous);

    if (synchronous) {
      try {
        return helper.send();
      } catch (Throwable t) {
        if (t instanceof RemoteException) {
          throw (RemoteException) t;
        }

        throw new RemoteException("Exception caught replicating command", t);
      }
    } else {
      try {
        helper.send();
      } catch (Throwable t) {
        if (t instanceof RemoteException) {
          throw (RemoteException) t;
        }

        throw new RemoteException("Exception caught replicating command", t);
      }

      return null;
    }
  }
  
  protected Set<ServerAddress> getVisitedAddresses(){
    return visited;
  }
  
  protected Set<ServerAddress> getTargetAddresses(){
    return targets;
  }  
}
