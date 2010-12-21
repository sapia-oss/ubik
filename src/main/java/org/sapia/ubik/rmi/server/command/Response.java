package org.sapia.ubik.rmi.server.command;


/**
 * Models a "response": i.e.: the return value of an asynchronous call-back.
 * An instance of this class in fact encapsulate the return value itself,
 * plus the command identifier of the response at the originating host.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Response implements Executable, java.io.Serializable {
  
  static final long serialVersionUID = 1L;
  
  private String _id;
  private Object _obj;

  /**
   * Constructor for Response.
   */
  public Response(String cmdId, Object response) {
    _id    = cmdId;
    _obj   = response;
  }

  /**
   * Returns the command identifier that this response corresponds to.
   *
   * @return a command identifier.
   */
  public String getId() {
    return _id;
  }

  /**
   * This response's return value.
   *
   * @return an <code>Object</code> or <code>null</code> if the
   * return value of the original asynchronous call-back is null.
   */
  public Object get() {
    return _obj;
  }

  /**
   * @see org.sapia.ubik.rmi.server.command.Executable#execute()
   */
  public Object execute() throws Throwable {
    return _obj;
  }

  public String toString() {
    return "[ id=" + _id + " ]";
  }
}
