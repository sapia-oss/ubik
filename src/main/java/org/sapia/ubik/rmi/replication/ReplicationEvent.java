package org.sapia.ubik.rmi.replication;

import org.sapia.ubik.rmi.interceptor.Event;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ReplicationEvent implements Event {
  private ReplicatedCommand _cmd;

  /**
   * Creates an instance of this class with the given command.
   */
  public ReplicationEvent(ReplicatedCommand command) {
    _cmd = command;
  }

  /**
   * @return the <code>ReplicatedCommand</code> that this instance holds.
   */
  public ReplicatedCommand getReplicatedCommand() {
    return _cmd;
  }
}
