package org.sapia.ubik.rmi.server.command;

/**
 * Models a "response": i.e.: the return value of an asynchronous call-back. An
 * instance of this class in fact encapsulate the return value itself, plus the
 * command identifier of the response at the originating host.
 * 
 * @author Yanick Duchesne
 */
public class Response implements Executable, java.io.Serializable {

  static final long serialVersionUID = 1L;

  private long id;
  private Object obj;

  public Response(long cmdId, Object response) {
    id = cmdId;
    obj = response;
  }

  /**
   * Returns the command identifier that this response corresponds to.
   * 
   * @return a command identifier.
   */
  public long getId() {
    return id;
  }

  /**
   * This response's return value.
   * 
   * @return an {@link Object} or <code>null</code> if the return value of the
   *         original asynchronous call-back is null.
   */
  public Object get() {
    return obj;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.Executable#execute()
   */
  public Object execute() throws Throwable {
    return obj;
  }

  public String toString() {
    return "[ id=" + id + " ]";
  }
}
