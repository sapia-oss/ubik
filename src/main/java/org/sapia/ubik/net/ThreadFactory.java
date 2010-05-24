package org.sapia.ubik.net;


/**
 * This interface specifies the behavior of a factory of thread.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ThreadFactory {
  /**
   * Given a <code>Runnable</code> instance, this method returns
   * a <code>Thread</code> instance which encapsulates the given
   * runnable. The returned thread is not started.
   *
   * @return a <code>Thread</code> instance wrapping the given <code>Runnable</code>.
   */
  public Thread newThreadFor(Runnable r);
}
