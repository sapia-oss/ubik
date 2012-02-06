package org.sapia.ubik.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * This class can conveniently be used as a replacement for the JDK's 
 * {@link ByteArrayInputStream} and {@link ByteArrayOutputStream}
 * classes.
 * <p>
 * This class implements a vector of byte arrays (it encapsulates an array of
 * byte arrays). An instance of this class will "grow" (according to a predefined
 * increment) as new bytes are added to it (and if its current size does not allow 
 * for these new bytes to fit in).
 * <p>
 * In addition, each byte array that is internally kept (and stores the actual
 * bytes written to this instance) is created with a fixed capacity (which can 
 * be specified when creating an instance of this class).
 * <p>
 * An instance of this class internally keeps track of its current position in
 * the vector. That position is incremented according to the reads and writes that
 * are performed. Thus, existing bytes are not overwritten and are not read twice.
 * <p>
 * An instance of this class can be reused without destroying the internal byte
 * arrays that have been created. Similarly, reading bytes from the instance
 * does not require copying the whole bytes that the instance stores to an 
 * intermediary array that is returned to the caller - instead, this instance
 * directly supports reads (this is a workaround to the JDK's {@link ByteArrayOutputStream#toByteArray()}
 * method.
 * <p>
 * <b>WARNING: THIS CLASS IS NOT THREAD-SAFE</b>.
 *
 *
 * @author Yanick Duchesne
 *
 * @see ByteVectorInputStream
 * @see ByteVectorOutputStream
 */
public class ByteVector {
  
  public static final int DEFAULT_ARRAY_CAPACITY = 200;
  public static final int DEFAULT_INCREMENT      = 10;  
  
  protected int arrayPos, markPos, markOffset, arrayCount;
  protected int capacity  = DEFAULT_ARRAY_CAPACITY;
  protected int increment = DEFAULT_INCREMENT;  
  
  protected ByteArray[] arrays = new ByteArray[increment];

  /**
   * Creates an instance of this class with the default capacity and increment.
   */
  public ByteVector(){}
  
  /**
   * Creates an instance of this class with the given capacity and increment. 
   * The capacity corresponds to the size of the internal byte arrays (that are
   * created to store data). The increment corresponds to the amount of internal
   * byte arrays that will be created when this instance will grow.
   *
   * @param capacity some capacity.
   * @param increment some increment.
   */  
  public ByteVector(int capacity, int increment){
    this.capacity  = capacity;
    this.increment = increment;
  }
  
  
  /**
   * Internally sets the given position as a mark. The internal position counter
   * will internally be set to that mark upon the <code>reset</code> method
   * being called.
   *
   * @param mark some mark.
   * @see #reset()
   */
  public void mark(int mark){
    if(mark < capacity){
      markPos = 0;
      markOffset = mark; 
    }
    else{
      int pos      = mark / capacity;
      int offset   = mark % capacity;
      if(pos >= arrayCount){
        throw new IndexOutOfBoundsException(""+mark);
      }
      markPos    = pos;
      markOffset = offset;
    }
  }
  
  /**
   * Resets the internal position to the specified mark. The position will be 
   * set to 0 if no mark has been specified.
   *
   * @see #mark(int)
   */
  public void reset(){
    arrayPos = markPos;
    arrays[arrayPos].pos = markOffset;
    for(int i = arrayPos + 1; i < arrayCount; i++){
      arrays[i].pos = 0;
    }
  }
  
  /**
   * Clears the data that this instance holds, making it suitable for reuse.
   * 
   * @param freeMemory if <code>true</code>, the internal byte arrays that have
   * been created will be dereferenced (otherwise, they will be reused).
   */
  public void clear(boolean freeMemory){
    arrayPos   = 0;
    markOffset = 0;
    markPos    = 0;
    if(freeMemory){
      arrays = new ByteArray[increment];
    }
    else{
      for(int i = 0; i < arrayCount; i++){
        arrays[i].pos   = 0;
        arrays[i].limit = 0;        
      }      
    }
    arrayCount = 0;
  }

  /**
   * Returns the number of bytes that this instance holds.
   */
  public int length(){
    if(arrayCount == 0){
      return 0;
    }
    return (arrayCount - 1) * capacity + arrays[arrayCount - 1].limit;    
  }
  
  /**
   * @return the current internal position of this instance, in terms of the 
   * bytes it holds.
   */
  public int position(){
    if(arrayCount == 0){
      return 0;
    }
    else if(arrayPos == 0){
      return arrays[arrayPos].pos;
    }
    if(arrayPos >= arrayCount){
      return (arrayCount - 1) * capacity + arrays[arrayCount - 1].pos;      
    }
    else{
      return arrayPos * capacity  + arrays[arrayPos].pos;
    }
  }
  
  /**
   * Returns the number of remaining bytes in this instance.
   *
   * @return <code>lenth() - position()</code>
   */
  public int remaining(){
    return length() - position();
  }
  
  int arrayPosition(){
    return arrayPos;
  }
  
  int arrayCount(){
    return arrayCount;
  }  
  
  /**
   * @return <code>true</code> if this instance's current internal position is
   * less than the number of bytes it holds.
   */
  public boolean hasRemaining(){
    return arrayPos < arrayCount && arrays[arrayPos].pos < arrays[arrayPos].limit; 
  }
  
  /**
   * Returns the bytes that this instance holds (starting from this instance's
   * current position).
   */
  public byte[] toByteArray(){
    if(arrayCount == 0 || arrays[0] == null){
      return new byte[0];
    }
    byte[] b = new byte[length()];
    int index = 0;
    for(int i = arrayPos; i < arrayCount; i++){
      for(int j = 0; j < arrays[i].limit; j++){
        b[index] = arrays[i].bytes[j];
        index++;
      }
    }
    return b;
  }
  
  /**
   * @return the next byte that this instance holds, or <code>-1</code>
   * if this instance holds no more bytes.
   */
  public int read(){
    if(arrayCount == 0 || arrayPos >= arrayCount){
      return -1;
    }
    ByteArray arr = arrays[arrayPos];
    if(arr == null) return -1;
    if(arr.pos < arr.limit){
      int b = arr.bytes[arr.pos++] & 0xff;
      if(arr.pos >= arr.limit){
        arrayPos++;
      }      
      return b;
    }
    return -1;
  }  
  
  /**
   * @param b a byte array that will be filled with the bytes that were read.
   *
   * @return the number of bytes that were read.
   */
  public int read(byte[] b){
    return read(b, 0, b.length);
  }
  
  /**
   * @param b a byte array that will be filled with the bytes that were read.
   * @param off the offset from which bytes should be inserted into the given array.
   * @param len the number of bytes to fill in the given array, starting from the 
   * given offset.
   * @return the number of bytes that were read.
   */
  public int read(byte[] b, int off, int len){
    int read = 0;
    int total = 0;
    while(len > 0 && arrayPos < arrayCount){
      read = arrays[arrayPos].get(b, off, len);
      if(read == 0){
        break;
      }
      if(arrays[arrayPos].pos >= arrays[arrayPos].limit){
        arrayPos++;
      }
      len = len - read;
      off = off + read;
      total += read;
    }    
    return total;
  }
  
  /**
   * @param buf a {@link ByteBuffer}.
   * @return the number of bytes that were read.
   */
  public int read(ByteBuffer buf){
    int read = 0;
    int total = 0;
    int len = buf.remaining();
    while(len > 0 && arrayPos < arrayCount){
      read = arrays[arrayPos].get(buf, len);
      if(read == 0){
        break;
      }
      if(arrays[arrayPos].pos >= arrays[arrayPos].limit){
        arrayPos++;
      }
      len = len - read;
      total += read;
    }    
    return total;
    
  }
  
  /**
   * Reads this instance's bytes and transfers them to the given stream.
   *
   * @param out an {@link OutputStream}.
   */
  public void read(OutputStream out) throws IOException{
    int read;
    while(arrayPos <  arrayCount){
      read = arrays[arrayPos].get(out);
      if(read == 0){
        break;
      }
      if(arrays[arrayPos].pos >= arrays[arrayPos].limit){
        arrayPos++;
      }      
    }
  }  
  
  /**
   * Writes the given bytes to this instance. 
   *
   * @param b the array of bytes to write.
   */
  public void write(byte[] b){
    write(b, 0, b.length);
  }
  
  public void write(int b){
    if(arrayPos >= arrayCount)
      increase();
    if(!arrays[arrayPos++].put((byte)b)){
      increase();
      arrays[arrayPos - 1].put((byte)b);      
    }
  }  
  
  /**
   * Writes the given bytes to this instance.
   *
   * @param b the array of bytes to write.
   * @param off the offset from which to start taking the bytes in the given
   * array.
   * @param len the number of bytes to read, starting from the given offset.
   */
  public void write(byte[] b, int off, int len){
    int put = 0;
    while(put < len){
      if(arrayPos >= arrayCount){
        increase(); 
      }      
      put = arrays[arrayPos].put(b, off, len);
      if(put < len){
        arrayPos++;
      }
      off += put;
      len -= put;
      put = 0;      
    }
  }
  
  /**
   * Writes the bytes contained in the given buffer to this instance.
   *
   * @param buf a {@link ByteBuffer}
   */
  public void write(ByteBuffer buf){
    while(buf.hasRemaining()){
      if(arrayPos >= arrayCount){
        increase(); 
      }      
      int len = buf.remaining();
      int put = arrays[arrayPos].put(buf);
      if(put < len){
        arrayPos++;
      }
      put = 0;      
    }    
  }  
  
  /**
   * Skips the number of bytes within this instance. 
   *
   * @return the number of bytes that were actually skipped.
   */
  public long skip(long skip){
    if(arrayCount == 0){
      return 0;
    }
    long total = 0;
    while(total < skip && arrayPos < arrayCount){
      ByteArray arr = arrays[arrayPos];
      
      if(arr == null)
        break;      
      
      int size = arr.limit - arr.pos;
      
      if(size <= 0) 
        break;
      
      if(total + size <= skip){
        arr.pos = arr.limit;
        total = total + size;        
        arrayPos++;      
      }
      else{
        size = (int)(skip - total);
        arr.pos = arr.pos + size;
        total = total + size;
      }
    }
    return total;
  }
  
  private void increase(){
    if(arrayPos >= arrays.length){
      ByteArray[] newArray = new ByteArray[arrays.length + increment];
      System.arraycopy(arrays, 0, newArray, 0, arrays.length);
      arrays = newArray;
    }
    if(arrays[arrayPos] == null){
      arrays[arrayPos] = new ByteArray(capacity);
    }
    arrayCount++;        
  }  
  
  ///////////////////////// ByteArray ///////////////////////////
  
  protected static class ByteArray{
    protected int    pos;
    protected int    limit;
    protected byte[] bytes;
    protected ByteArray(int capacity){
      bytes = new byte[capacity];
    }

    protected boolean put(byte b){
      if(pos < bytes.length){
        bytes[pos++] = b;
        limit++;
      }
      return false;
    }
    
    protected int put(byte[] b, int offset, int len){
      if(pos < bytes.length){
        if(len <= bytes.length - pos){
          System.arraycopy(b, offset, bytes, pos, len);
          pos   += len;
          limit += len;
          return len;
        }
        else{
          System.arraycopy(b, offset, bytes, pos, bytes.length - pos);
          int put = bytes.length - pos; 
          pos   += put;
          limit += put;          
          return put;
        }
      }
      return 0;
    }
    
    protected int put(ByteBuffer buf){
      if(pos < bytes.length){
        if(buf.remaining() <= bytes.length - pos){
          int len = buf.remaining();
          buf.get(bytes, pos, buf.remaining());
          pos   += len;
          limit += len;
          return len;
        }
        else{
          buf.get(bytes, pos, bytes.length - pos);
          int put = bytes.length - pos; 
          pos   += put;
          limit += put;          
          return put;
        }
      }
      return 0;
    }    
    
    protected int get(byte[] b, int offset, int len){
      if(pos < limit){
        if(limit - pos <= len){
          System.arraycopy(bytes, pos, b, offset, limit - pos);
          int read = limit - pos; 
          pos += read;
          return read;
        }
        else{
          System.arraycopy(bytes, pos, b, offset, len);
          pos += len;
          return len;
        }
      }
      return 0;
    }
    
    protected int get(ByteBuffer buf, int len){
      if(pos < limit){
        if(limit - pos <= len){
          buf.put(bytes, pos, limit - pos);
          int read = limit - pos; 
          pos += read;
          return read;
        }
        else{
          buf.put(bytes, pos, len);
          pos += len;
          return len;
        }
      }
      return 0;
    }    
    
    protected int get(OutputStream out) throws IOException{
      if(pos >= limit){
        return 0;
      }
      out.write(bytes, pos, limit);
      int read = limit - pos;
      pos = pos + read;
      return read;
    }
  }

}
