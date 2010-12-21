package org.sapia.ubik.mcast;

import java.util.ArrayList;
import java.util.List;


/**
 * Models a list of <code>Response</code> objects.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class RespList {
  private List<Response> _resps;

  /**
   * Constructor for RespList.
   */
  public RespList(int capacity) {
    _resps = new ArrayList<Response>(capacity);
  }

  /**
   * Adds the given response to this instance.
   *
   * @param a <code>Response</code> object.
   */
  public void addResponse(Response resp) {
    _resps.add(resp);
  }

  /**
   * Returns the <code>Response</code> object at
   * the given index.
   *
   * @param a <code>Response</code> object.
   */
  public Response get(int index) {
    return (Response) _resps.get(index);
  }

  /**
   * Returns <code>true</code> if this instance contains a
   * <code>Response</code> object that represents an error
   * that occurred on the remote side.
   *
   * @return <code>true</code> if this instance contains a
   * <code>Response</code> object that holds a <code>Throwable</code>
   */
  public boolean containsError() {
    Response r;

    for (int i = 0; i < _resps.size(); i++) {
      r = (Response) _resps.get(i);

      if (r.isError()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns <code>true</code> if this instance contains
   * a <code>Response</code> object whose status is "suspect" -
   * meaning that the corresponding node is probably down.
   *
   * @return <code>true</code> if this instance contains
   * a <code>Response</code> whose corresponding node is probably
   * down.
   */
  public boolean containsSuspect() {
    Response r;

    for (int i = 0; i < _resps.size(); i++) {
      r = (Response) _resps.get(i);

      if (r.isError()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns the number of responses within this instance.
   *
   * @return the number of responses within this instance.
   */
  public int count() {
    return _resps.size();
  }
}
