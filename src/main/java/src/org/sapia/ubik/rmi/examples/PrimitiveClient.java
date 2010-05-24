package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;
import org.sapia.ubik.rmi.server.Hub;

import java.util.Properties;

import javax.naming.InitialContext;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class PrimitiveClient {
  public static void main(String[] args) {
    try {
      Properties props = new Properties();

      // ENABLES MARSHALLING      
      System.setProperty(Consts.MARSHALLING, "true");

      props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      InitialContext ctx      = new InitialContext(props);
      Object         lookedUp = ctx.lookup("PrimitiveService");
      System.out.println(lookedUp.getClass().getName());

      PrimitiveService svc = (PrimitiveService) lookedUp;

      svc.getBoolean();
      svc.getByte();
      svc.getChar();
      svc.getShort();
      svc.getInt();
      svc.getLong();
      svc.getFloat();
      svc.getDouble();

      svc.setBoolean(true);
      svc.setByte((byte) 0);
      svc.setChar('c');
      svc.setShort((short) 0);
      svc.setInt(0);
      svc.setLong(0);
      svc.setFloat(0);
      svc.setDouble(0);

      byte[] b = "Hello World".getBytes();

      for (int i = 0; i < b.length; i++) {
        System.out.print(b[i]);
      }

      svc.setBytes(b);

      Hub.shutdown(30000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
