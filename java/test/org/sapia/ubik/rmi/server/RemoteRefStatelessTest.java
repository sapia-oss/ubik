package org.sapia.ubik.rmi.server;

import junit.framework.TestCase;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class RemoteRefStatelessTest extends TestCase {
  public RemoteRefStatelessTest(String arg0) {
    super(arg0);
  }

  public void testAddSibling() throws Exception {
    RemoteRefStateless             st          = new RemoteRefStateless();
    ServerAddress                  currentAddr = new TCPAddress("current", 1);
    OID                            currentOid  = new OID(1);
    RemoteRefStateless.ServiceInfo current     = new RemoteRefStateless.ServiceInfo(currentAddr,
        currentOid, false, VmId.getInstance(), true);
    st._serviceInfos.add(current);
    super.assertEquals(1, st.getInfos().size());

    RemoteRefStateless             otherRef  = new RemoteRefStateless();
    ServerAddress                  otherAddr = new TCPAddress("other", 1);
    OID                            otherOid  = new OID(2);
    RemoteRefStateless.ServiceInfo other     = new RemoteRefStateless.ServiceInfo(currentAddr,
        currentOid, false, VmId.getInstance(), true);
    st.addSibling(otherRef);
    super.assertEquals(1, st.getInfos().size());
    st.addSibling(st);
    super.assertEquals(1, st.getInfos().size());
  }
}
