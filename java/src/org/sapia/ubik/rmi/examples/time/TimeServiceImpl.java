package org.sapia.ubik.rmi.examples.time;

import java.text.SimpleDateFormat;

import java.util.Date;


/**
 * @
 */
public class TimeServiceImpl implements TimeServiceIF {
  private SimpleDateFormat _theFormat;
  private int              _theCoutner;
  private String           _theId;

  /**
   * Creates a new TimeServiceImpl instance.
   */
  public TimeServiceImpl() {
    System.out.println("Creating a new sticky time service...");
    _theFormat   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    _theId       = Integer.toHexString(String.valueOf(
          System.currentTimeMillis()).hashCode());
  }

  /**
   * Implements the TimeServiceIF interface.
   *
   * @see org.sapia.test.TimeServiceIF#getTime()
   */
  public String getTime() {
    long   aCurrentTime = System.currentTimeMillis();
    Date   aCurrentDate = new Date(aCurrentTime);
    String aResult      = _theFormat.format(aCurrentDate);

    System.out.println((++_theCoutner) + " - Invocation took " +
      (System.currentTimeMillis() - aCurrentTime) + " ms @" + _theId);

    return aResult + "  @" + _theId;
  }
}
