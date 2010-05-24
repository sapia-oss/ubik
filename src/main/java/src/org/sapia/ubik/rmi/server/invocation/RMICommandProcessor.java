package org.sapia.ubik.rmi.server.invocation;

import org.sapia.ubik.rmi.server.*;
import org.sapia.ubik.rmi.server.command.CommandProcessor;
import org.sapia.ubik.rmi.server.command.Destination;
import org.sapia.ubik.rmi.server.command.ResponseQueue;
import org.sapia.ubik.rmi.server.command.ResponseSender;
import org.sapia.ubik.rmi.server.transport.RmiConnection;
import org.sapia.ubik.rmi.server.transport.TransportManager;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.List;


/**
 * A Ubik RMI-specific <code>CommandProcessor</code>.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class RMICommandProcessor extends CommandProcessor {
  private ResponseQueue _responses = ResponseQueue.getInstance();

  /**
   * Constructor for RMICommandProcessor.
   */
  public RMICommandProcessor(int maxThreads) {
    super(maxThreads);
    super.setResponseSender(new RMIResponseSender());
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.CommandProcessor#shutdown(long)
   */
  public void shutdown(long timeout) throws InterruptedException {
    super.shutdown(timeout);
    Log.warning(getClass(), "Shutting down incoming response queue");
    ResponseQueue.getInstance().shutdown(timeout);
    _responses.shutdown(timeout);
  }

  /*////////////////////////////////////////////////////////////////////
                              INNER CLASSES
  ////////////////////////////////////////////////////////////////////*/
  static final class RMIResponseSender implements ResponseSender {
    /**
    * @see org.sapia.ubik.rmi.command.ResponseSender#sendResponses(Destination, List)
    */
    public void sendResponses(Destination dest, List responses) {
      if (Log.isDebug()) {
        Log.debug(getClass(), "Sending callback responses to " + dest);
      }

      if (responses.size() > 0) {
        RmiConnection conn = null;

        try {
          conn = TransportManager.getConnectionsFor(dest.getServerAddress())
                                 .acquire();

          //VmId.register(dest.getVmId());
          conn.send(new ResponseListCommand(responses), dest.getVmId(),
            dest.getServerAddress().getTransportType());
          conn.receive();
        } catch (Exception e) {
          Log.error(RMICommandProcessor.class, e);

          if (conn != null) {
            conn.close();
          }

          return;
        }

        if (conn != null) {
          try {
            TransportManager.getConnectionsFor(dest.getServerAddress()).release(conn);
          } catch (Exception e) {
            Log.error(RMICommandProcessor.class, e);
          }
        }
      }
    }
  }

  public static final class ResponseListCommand extends RMICommand {
    private List _responses;

    public ResponseListCommand() {
    }

    ResponseListCommand(List responses) {
      _responses = responses;
    }

    /**
     * @see org.sapia.ubik.rmi.server.command.Executable#execute()
     */
    public Object execute() throws Throwable {
      if (Log.isDebug()) {
        Log.debug(getClass(), "Receiving callbacks");
      }

      ResponseQueue.getInstance().onResponses(_responses);

      return new Integer(0);
    }

    /**
     * @see org.sapia.ubik.rmi.server.RMICommand#writeExternal(ObjectOutput)
     */
    public void writeExternal(ObjectOutput out) throws IOException {
      super.writeExternal(out);
      out.writeObject(_responses);
    }

    /**
     * @see org.sapia.ubik.rmi.server.RMICommand#readExternal(ObjectInput)
     */
    public void readExternal(ObjectInput in)
      throws IOException, ClassNotFoundException {
      super.readExternal(in);
      _responses = (List) in.readObject();
    }
  }
}
