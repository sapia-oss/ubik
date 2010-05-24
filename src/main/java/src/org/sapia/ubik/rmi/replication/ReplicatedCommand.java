package org.sapia.ubik.rmi.replication;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.InvokeCommand;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.rmi.RemoteException;

import java.util.HashSet;
import java.util.Set;


/**
 * Wraps an <code>InvokeCommand</code> that is intented to be replicated to the different
 * servers in a domain or cluster.
 *
 * @see #getReplicationContext()
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class ReplicatedCommand extends InvokeCommand {
  private Set               _visited     = new HashSet();
  private Set               _targets;
  private ReplicatedInvoker _invoker;
  private boolean           _executed;
  private boolean           _synchronous;
  private boolean           _disabled;

  /** Do not call; used for externalization only. */
  public ReplicatedCommand() {
  }

  /**
   * @param cmd the <code>InvokeCommand</code> to replicate.
   * @param targets the <code>Set</code> of <code>ServerAddress</code>es that are
   * targeted by the command. If <code>null</code>, then all siblings will be targeted.
   * @param invoker the <code>ReplicatedInvoker</code> implementation in charge of
   * performing replicated method invocations.
   */
  public ReplicatedCommand(InvokeCommand cmd, Set targets,
    ReplicatedInvoker invoker, boolean synchronous) {
    super(cmd.getOID(), cmd.getMethodName(), cmd.getParams(),
      cmd.getParameterTypes(), null);
    _targets       = targets;
    _invoker       = invoker;
    _synchronous   = synchronous;
  }

  /**
   * This method's implementation internally calls the <code>getReplicationContext()</code>
   * method. The returned <code>ReplicationContext</code> is used as a hook to the server
   * in which this command is executed. Internally, the method selects the next server
   * to which this instance should be dispatched, and executes the command that this
   * instance wraps.
   * </p>
   * If the execution is successful, this instance is dispatched by means of the
   * <code>ReplicationContext</code>.
   *
   * @see #getReplicationContext()
   *
   * @see org.sapia.ubik.rmi.server.RMICommand#execute()
   */
  public Object execute() throws Throwable {
    if (_disabled) {
      return super.execute();
    }

    Object toReturn;
    Hub.serverRuntime.dispatchEvent(new ReplicationEvent(this));

    Set                 siblings = _invoker.getSiblings();
    ReplicationStrategy strat   = new ReplicationStrategy(_visited, _targets,
        siblings);
    ServerAddress       addr;
    ServerAddress       current = getServerAddress();

    if (_executed) {
      convertParams(_invoker.getClass().getClassLoader());
      toReturn = _invoker.invoke(super.getMethodName(),
          super.getParameterTypes(), super.getParams());
    } else {
      if (_targets != null) {
        if (_targets.contains(current)) {
          toReturn    = super.execute();
          _executed   = true;
          _targets.remove(current);
        } else {
          _executed   = true;
          toReturn    = send(strat.selectNextSibling());

          return toReturn;
        }
      } else {
        toReturn    = super.execute();
        _executed   = true;
      }

      if ((addr = strat.selectNextSibling()) != null) {
        if (!_disabled) {
          send(addr);
        }
      }
    }

    return toReturn;
  }

  /**
   * @return the <code>ReplicatedInvoker</code> that this instance holds.
   */
  public ReplicatedInvoker getReplicatedInvoker() {
    return _invoker;
  }

  /**
   * Disables replication behavior (this command will execute as a normal
   * <code>InvokeCommand</code>).
   */
  public void disable() {
    _disabled = true;
  }

  /**
   * @see org.sapia.ubik.rmi.server.invocation.InvokeCommand#readExternal(java.io.ObjectInput)
   */
  public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
    super.readExternal(in);
    _visited       = (Set) in.readObject();
    _targets       = (Set) in.readObject();
    _invoker       = (ReplicatedInvoker) in.readObject();
    _executed      = in.readBoolean();
    _synchronous   = in.readBoolean();
    _disabled      = in.readBoolean();
  }

  /**
   * @see org.sapia.ubik.rmi.server.invocation.InvokeCommand#writeExternal(java.io.ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(_visited);
    out.writeObject(_targets);
    out.writeObject(_invoker);
    out.writeBoolean(_executed);
    out.writeBoolean(_synchronous);
    out.writeBoolean(_disabled);
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
    SendHelper helper = new SendHelper(this, next, _synchronous);

    if (_synchronous) {
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
  
  protected Set getVisitedAddresses(){
    return _visited;
  }
  
  protected Set getTargetAddresses(){
    return _targets;
  }  
}
