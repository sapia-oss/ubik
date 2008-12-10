package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.rmi.naming.remote.RemoteInitialContextFactory;

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
public class PrimitiveServer implements PrimitiveService {
  public static void main(String[] args) {
    try {
      //PerfAnalyzer.getInstance().setEnabled(true);
      Properties props = new Properties();
      props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099");
      props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
        RemoteInitialContextFactory.class.getName());

      InitialContext ctx = new InitialContext(props);

      ctx.rebind("PrimitiveService", new PrimitiveServer());

      System.out.println("Primitive server started...");

      while (true) {
        Thread.sleep(10000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public boolean getBoolean() {
    return false;
  }

  public byte getByte() {
    return 0;
  }

  public char getChar() {
    return 0;
  }

  public double getDouble() {
    return 0;
  }

  public float getFloat() {
    return 0;
  }

  public int getInt() {
    return 0;
  }

  public long getLong() {
    return 0;
  }

  public short getShort() {
    return 0;
  }

  public void setBoolean(boolean bool) {
  }

  public void setByte(byte b) {
  }

  public void setChar(char c) {
  }

  public void setDouble(double d) {
  }

  public void setFloat(float f) {
  }

  public void setInt(int i) {
  }

  public void setLong(long l) {
  }

  public void setShort(short s) {
  }

  public void setBytes(byte[] b) {
    for (int i = 0; i < b.length; i++) {
      System.out.print(b[i]);
    }

    System.out.println();
    System.out.println(new String(b));
  }
}
