package org.sapia.ubik.rmi.server.command;

import java.util.List;


/**
 * Specifies the behavior of a sender of {@link Response} instances.
 *
 * @see CommandProcessor#setResponseSender(ResponseSender)
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ResponseSender {
  /**
   * Sends the given list of responses to the host corresponding to the
   * given destination.
   *
   * @param destination a {@link Destination}.
   * @param responses a {@link List} of {@link Response} objects.
   */
  public void sendResponses(Destination destination, java.util.List<Response> responses);
}
