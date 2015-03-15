package org.sapia.ubik.mcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Models a list of {@link Response} objects.
 * 
 * @author Yanick Duchesne
 */
public class RespList implements Iterable<Response> {
  private List<Response> resps;

  /**
   * @param capacity
   *          the initial capacity of the {@link List} that is internally
   *          created to hold {@link Response} instances.
   */
  public RespList(int capacity) {
    resps = new ArrayList<Response>(capacity);
  }

  /**
   * @param responses
   *          a {@link List} of {@link Response} instances.
   */
  public RespList(List<Response> responses) {
    this.resps = responses;
  }

  /**
   * Adds the given response to this instance.
   * 
   * @param resp
   *          a {@link Response} object.
   */
  public void addResponse(Response resp) {
    resps.add(resp);
  }

  /**
   * Returns the {@link Response} object at the given index.
   * 
   * @param index
   *          an index.
   * @return a {@link Response} object.
   */
  public Response get(int index) {
    return (Response) resps.get(index);
  }

  /**
   * Returns <code>true</code> if this instance contains a {@link Response}
   * object that represents an error that occurred on the remote side.
   * 
   * @return <code>true</code> if this instance contains a {@link Response}
   *         object that holds a {@link Throwable}
   */
  public boolean containsThrowable() {
    Response r;

    for (int i = 0; i < resps.size(); i++) {
      r = (Response) resps.get(i);

      if (r.isThrowable()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns <code>true</code> if this instance contains a {@link Response}
   * object whose status is "suspect" - meaning that the corresponding node is
   * probably down.
   * 
   * @return <code>true</code> if this instance contains a {@link Response}
   *         whose corresponding node is probably down.
   */
  public boolean containsSuspect() {
    Response r;

    for (int i = 0; i < resps.size(); i++) {
      r = (Response) resps.get(i);

      if (r.isThrowable()) {
        return true;
      }
    }

    return false;
  }

  /**
   * @return the number of responses within this instance.
   */
  public int count() {
    return resps.size();
  }
  
  @Override
  public Iterator<Response> iterator() {
    return Collections.unmodifiableList(resps).iterator();
  }
}
