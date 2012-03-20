package org.sapia.ubik.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

public class ByteVectorTest extends TestCase{
  
  private ByteVector _out;
  
  public ByteVectorTest(String name){
    super(name);
  }
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    _out = new ByteVector(5, 1);
  }
  
  public void testWrite(){
    byte[] bytes = byteArray(10);
    _out.write(bytes);
    super.assertEquals(_out.arrayCount(), 2);
  }
  
  public void testWriteByteBuffer(){
    ByteBuffer bytes = byteBuffer(10);
    _out.write(bytes);
    super.assertEquals(_out.arrayCount(), 2);    
  }
  
  public void testRead(){
    byte[] bytes = byteArray(7);
    _out.write(bytes);
    byte[] read = byteArray(7);
    _out.reset();
    super.assertEquals(7, _out.read(read , 0, read.length));
    for(int i = 0; i < read.length; i++){
      super.assertEquals((int)read[i], (int)bytes[i]);
    }
    _out.reset();
    super.assertEquals(3, _out.read(read , 0, 3));    
    for(int i = 0; i < read.length; i++){
      super.assertEquals((int)read[i], (int)bytes[i]);
    }
    _out.reset();
    int i = _out.read();
    super.assertEquals(0, i);
  }
  
  public void testWriteReadOutputStream() throws IOException{
    byte[] bytes = byteArray(7);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    _out.write(bytes);
    _out.reset();
    _out.read(out);
    byte[] written = out.toByteArray();
    super.assertEquals(7, written.length);
    for(int i = 0; i < written.length; i++){
      super.assertEquals((int)written[i], (int)bytes[i]);
    }    
  }
  
  public void testReadByteBuffer(){
    ByteBuffer bytes = byteBuffer(7);
    _out.write(bytes);
    ByteBuffer read = byteBuffer(7);
    _out.reset();
    super.assertEquals(7, _out.read(read));
    read.flip();
    bytes.flip();
    super.assertEquals(7, read.remaining());
    super.assertEquals(7, bytes.remaining());    
    for(; read.hasRemaining(); ){
      super.assertEquals((int)read.get(), (int)bytes.get());
    }
  }  
  
  public void testMark(){
    byte[] bytes = byteArray(7);
    _out.write(bytes);
    _out.mark(4);
    _out.reset();
    super.assertEquals(4, _out.read());    
    _out.mark(6);
    _out.reset();
    super.assertEquals(6, _out.read());    
  }
  
  public void testToByteArray(){
    byte[] bytes = byteArray(7);
    _out.write(bytes);
    byte[] read = _out.toByteArray();
    super.assertEquals(7, read.length);
  }   
  
  private byte[] byteArray(int size){
    byte[] b = new byte[size];
    for(int i = 0; i < size; i++){
      b[i] = (byte)i;
    }
    return b;
  }
  
  private ByteBuffer byteBuffer(int size){
    ByteBuffer buffer = ByteBuffer.allocate(size);
    for(int i = 0; i < size; i++){
      buffer.put((byte)i);
    }
    buffer.flip();
    return buffer;
  }  

}
