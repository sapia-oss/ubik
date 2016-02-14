package org.sapia.ubik.rmi.naming.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.naming.NameParser;

import org.sapia.archie.impl.DefaultNameParser;
import org.sapia.archie.jndi.JndiNameParser;
import org.sapia.ubik.rmi.naming.remote.proxy.BindingCache;

import junit.framework.TestCase;

/**
 * @author Yanick Duchesne
 */
public class BindingCacheTest extends TestCase {

  NameParser parser;

  public BindingCacheTest(String arg0) {
    super(arg0);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    parser = new JndiNameParser(new DefaultNameParser());
  }

  public void testAdd() throws Exception {
    BindingCache bc = new BindingCache();
    bc.add("junit", parser.parse("someObject"), new Object());
    super.assertEquals(1, bc.cachedRefs().size());
  }

  public void testSerializeNotNull() throws Exception {
    BindingCache bc = new BindingCache();
    bc.add("junit", parser.parse("someObject"), "theObject");

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream ous = new ObjectOutputStream(bos);
    ous.writeObject(bc);
    ous.flush();
    ous.close();

    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
    bc = (BindingCache) ois.readObject();
    super.assertEquals(1, bc.cachedRefs().size());

    BindingCache.BoundRef ref = (BindingCache.BoundRef) bc.cachedRefs().get(0);
    super.assertEquals(parser.parse("someObject"), ref.name);
    super.assertEquals("theObject", ref.obj);
  }

  public void testSerializeNull() throws Exception {
    BindingCache bc = new BindingCache();
    bc.add("junit", parser.parse("someObject"), null);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream ous = new ObjectOutputStream(bos);
    ous.writeObject(bc);
    ous.flush();
    ous.close();

    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
    bc = (BindingCache) ois.readObject();
    super.assertEquals(0, bc.cachedRefs().size());
  }
}
