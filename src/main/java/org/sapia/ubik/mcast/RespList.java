package org.sapia.ubik.mcast;

import java.util.ArrayList;
import java.util.List;


/**
 * Models a list of {@link Response} objects.
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
   * @param resp a {@link Response} object.
   */
  public void addResponse(Response resp) {
    _resps.add(resp);
  }

  /**
   * Returns the {@link Response} object at
   * the given index.
   *
   * @param index an index.
   * @return a {@link Response} object.
   */
  public Response get(int index) {
    return (Response) _resps.get(index);
  }

  /**
   * Returns <code>true</code> if this instance contains a
   * {@link Response} object that represents an error
   * that occurred on the remote side.
   *
   * @return <code>true</code> if this instance contains a
   * {@link Response} object that holds a {@link Throwable}
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
   * a {@link Response} object whose status is "suspect" -
   * meaning that the corresponding node is probably down.
   *
   * @return <code>true</code> if this instance contains
   * a {@link Response} whose corresponding node is probably
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
