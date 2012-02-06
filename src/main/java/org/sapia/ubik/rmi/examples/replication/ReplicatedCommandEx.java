package org.sapia.ubik.rmi.examples.replication;

import java.util.Set;

import org.sapia.ubik.rmi.replication.ReplicatedCommand;
import org.sapia.ubik.rmi.replication.ReplicatedInvoker;
import org.sapia.ubik.rmi.server.command.InvokeCommand;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ReplicatedCommandEx extends ReplicatedCommand {
  /** Constructor necessary for externalization. */
  public ReplicatedCommandEx() {
  }

  public ReplicatedCommandEx(InvokeCommand command, Set targets,
    ReplicatedInvoker invoker) {
    super(command, targets, invoker, true);
  }
}
