package org.sapia.ubik.mcast;

import org.sapia.ubik.util.pool.Pool;

/**
 * This class implements a pool of byte arrays.
 * 
 * @author yduchesne
 */
public class ByteArrayPool extends Pool<byte[]>{
  
  private volatile int bufSize;
  
  /** Creates a new instance of ByteArrayPool */
  public ByteArrayPool(int bufSize) {
    this.bufSize = bufSize;
  }
  
  public void setBufSize(int bufSize){
    this.bufSize = bufSize;
  }
  
  public int getBufSize(){
    return bufSize;
  }
  
  protected byte[] onAcquire(byte[] bytes) throws Exception {
    if(bytes.length != bufSize){
      return new byte[bufSize];
    }
    else{
      return bytes;
    }
  }  
  
  protected byte[] doNewObject() throws Exception{
    return new byte[bufSize];
  }
  
}
