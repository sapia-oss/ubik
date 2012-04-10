/**
 * This command in fact holds asynchronous responses, corresponding to server callbacks (or,
 * more precisely, corresponding to the asynchronous invocation of {@link CallbackInvokeCommand}s at the server).
 * <p>
 * That is to say an instance of this class is executed at the client, after being sent from the server.
 * It encapsulates a list of {@link Response}s, each resulting from the execution of a single {@link CallbackInvokeCommand}.
 * <p>
 * Internally, this command dispatches the responses that it holds to the {@link CallbackResponseQueue}.
 * 
 * @see CallbackInvokeCommand
 * @see CommandModule
 * @see CallbackResponseQueues
 */
package org.sapia.ubik.rmi.server.command;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import org.sapia.ubik.log.Log;
import org.sapia.ubik.rmi.server.Hub;

/**
 * An instance of this class holds the list of {@link Response}s resulting from the 
 * to the execution of 
 * @author yduchesne
 *
 */
public final class CallbackResponseCommand extends RMICommand {
  
  private List<Response> responses;

  /**
   * Do not use (meant for serialization)
   */
  public CallbackResponseCommand() {
  }

  CallbackResponseCommand(List<Response> responses) {
    this.responses = responses;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.Executable#execute()
   */
  public Object execute() throws Throwable {
    if (Log.isDebug()) {
      Log.debug(getClass(), "Processing callback response command");
    }

    Hub.getModules().getCommandModule().getCallbackResponseQueue().onResponses(responses);

    return new Integer(0);
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#writeExternal(ObjectOutput)
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    super.writeExternal(out);
    out.writeObject(responses);
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.RMICommand#readExternal(ObjectInput)
   */
  @SuppressWarnings(value="unchecked")
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    super.readExternal(in);
    responses = (List<Response>) in.readObject();
  }
}