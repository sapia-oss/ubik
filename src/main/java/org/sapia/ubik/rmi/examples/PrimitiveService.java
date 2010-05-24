package org.sapia.ubik.rmi.examples;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface PrimitiveService extends java.rmi.Remote {
  public boolean getBoolean();

  public byte getByte();

  public char getChar();

  public short getShort();

  public int getInt();

  public long getLong();

  public float getFloat();

  public double getDouble();

  public void setBoolean(boolean bool);

  public void setByte(byte b);

  public void setBytes(byte[] b);

  public void setChar(char c);

  public void setShort(short s);

  public void setInt(int i);

  public void setLong(long l);

  public void setFloat(float f);

  public void setDouble(double d);
}
