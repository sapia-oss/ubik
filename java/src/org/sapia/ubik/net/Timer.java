package org.sapia.ubik.net;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Timer {
  private long _start    = System.currentTimeMillis();
  private long _duration;
  private long _total;

  public Timer(long duration) {
    _duration = duration;
  }

  public void start() {
    _start = System.currentTimeMillis();
  }

  public boolean isOver() {
    _total   = (_total + System.currentTimeMillis()) - _start;
    _start   = System.currentTimeMillis();

    return _total > _duration;
  }
}
