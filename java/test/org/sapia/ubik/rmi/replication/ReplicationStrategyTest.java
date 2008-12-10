package org.sapia.ubik.rmi.replication;

import junit.framework.TestCase;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ReplicationStrategyTest extends TestCase {
  public ReplicationStrategyTest(String arg0) {
    super(arg0);
  }

  public void testNoTargets() {
    TCPAddress addr1 = new TCPAddress("test", 1);
    TCPAddress addr2 = new TCPAddress("test", 2);
    TCPAddress addr3 = new TCPAddress("test", 3);
    TCPAddress addr4 = new TCPAddress("test", 4);

    Set        siblings = new HashSet();
    siblings.add(addr1);
    siblings.add(addr2);
    siblings.add(addr3);
    siblings.add(addr4);

    Set                 visited = new HashSet();
    ReplicationStrategy st   = new ReplicationStrategy(visited, null, siblings);
    ServerAddress       next;

    next = st.selectNextSibling();
    super.assertTrue(next != null);
    super.assertEquals(1, visited.size());

    next = st.selectNextSibling();
    super.assertTrue(next != null);
    super.assertEquals(2, visited.size());

    next = st.selectNextSibling();
    super.assertTrue(next != null);
    super.assertEquals(3, visited.size());

    next = st.selectNextSibling();
    super.assertTrue(next != null);
    super.assertEquals(4, visited.size());

    next = st.selectNextSibling();
    super.assertTrue(next == null);
    super.assertEquals(4, visited.size());
  }

  public void testTargets() {
    TCPAddress addr1 = new TCPAddress("test", 1);
    TCPAddress addr2 = new TCPAddress("test", 2);
    TCPAddress addr3 = new TCPAddress("test", 3);
    TCPAddress addr4 = new TCPAddress("test", 4);

    Set        siblings = new HashSet();
    siblings.add(addr1);
    siblings.add(addr2);
    siblings.add(addr3);
    siblings.add(addr4);

    Set targets = new HashSet();
    targets.add(addr2);
    targets.add(addr4);

    Set                 visited = new HashSet();
    ReplicationStrategy st   = new ReplicationStrategy(visited, targets,
        siblings);
    ServerAddress       next;

    next = st.selectNextSibling();
    super.assertTrue(next != null);
    super.assertTrue(next.equals(addr2) || next.equals(addr4));
    super.assertEquals(1, visited.size());

    next = st.selectNextSibling();
    super.assertTrue(next != null);
    super.assertTrue(next.equals(addr2) || next.equals(addr4));
    super.assertEquals(2, visited.size());

    next = st.selectNextSibling();
    super.assertTrue(next == null);
    super.assertEquals(2, visited.size());
  }
}
