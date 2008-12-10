package org.sapia.ubik.net.mplex;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;


/**
 * Class documentation
 *
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">
 *     Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *     <a href="http://www.sapia-oss.org/license.html" target="sapia-license">license page</a>
 *     at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ObjectStreamSelectorTest extends TestCase {
  /**
   * Creates a new ObjectStreamSelectorTest instance.
   */
  public ObjectStreamSelectorTest(String aName) {
    super(aName);
  }

  public void testObjectStream() throws Exception {
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    ObjectOutputStream    oos = new ObjectOutputStream(data);
    oos.writeObject("testObjectStream");
    oos.flush();

    ObjectStreamSelector selector = new ObjectStreamSelector();
    assertTrue("Should select the object stream",
      selector.selectStream(data.toByteArray()));
  }

  public void testStringStream() throws Exception {
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    data.write("testObjectStream".getBytes());
    data.flush();

    ObjectStreamSelector selector = new ObjectStreamSelector();
    assertTrue("Should select the object stream",
      !selector.selectStream(data.toByteArray()));
  }
}
