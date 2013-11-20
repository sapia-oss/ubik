package org.sapia.ubik.rmi.server.command;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.util.Props;

/**
 * A factory of {@link ResponseSender}s.
 * 
 * @see ColocatedResponseSender
 * @see RemoteResponseSender
 * 
 * @author yduchesne
 * 
 */
class ResponseSenderFactoryImpl implements ResponseSenderFactory {

  private ResponseSender remote;
  private ResponseSender local;
  private boolean supportsColocatedCalls = Props.getSystemProperties().getBooleanProperty(Consts.COLOCATED_CALLS_ENABLED, false);

  /**
   * @param transports
   *          the {@link TransportManager} that is used to internally create the
   *          {@link RemoteResponseSender}.
   * @param queue
   *          the {@link CallbackResponseQueue} that is used to internally
   *          create the {@link ColocatedResponseSender}.
   */
  ResponseSenderFactoryImpl(TransportManager transports, CallbackResponseQueue queue) {
    remote = new RemoteResponseSender(transports);
    local = new ColocatedResponseSender(queue);
  }

  /**
   * @param destination
   *          the {@link Destination} for which to return a
   *          {@link ResponseSender}.
   * @return the {@link ResponseSender} that is appropriate for th given
   *         {@link Destination}.
   */
  public ResponseSender getResponseSenderFor(Destination destination) {

    if (destination.getVmId().equals(VmId.getInstance()) && supportsColocatedCalls) {
      return local;
    } else {
      return remote;
    }

  }
}
