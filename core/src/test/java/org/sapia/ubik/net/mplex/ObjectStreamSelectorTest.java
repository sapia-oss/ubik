package org.sapia.ubik.net.mplex;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
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
    ObjectOutputStream oos = new ObjectOutputStream(data);
    oos.writeObject("testObjectStream");
    oos.flush();

    ObjectStreamSelector selector = new ObjectStreamSelector();
    assertTrue("Should select the object stream", selector.selectStream(data.toByteArray()));
  }

  public void testStringStream() throws Exception {
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    data.write("testObjectStream".getBytes());
    data.flush();

    ObjectStreamSelector selector = new ObjectStreamSelector();
    assertTrue("Should select the object stream", !selector.selectStream(data.toByteArray()));
  }
}
