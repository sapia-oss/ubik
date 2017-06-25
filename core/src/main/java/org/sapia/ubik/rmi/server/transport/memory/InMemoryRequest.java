package org.sapia.ubik.rmi.server.transport.memory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;

import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.Defaults;
import org.sapia.ubik.rmi.server.VmId;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;
import org.sapia.ubik.util.Conf;
import org.sapia.ubik.util.Pause;

/**
 * Models an in-memory request.
 * 
 * @author yduchesne
 * 
 */
class InMemoryRequest {

  private int bufsz = Conf.getSystemProperties().getIntProperty(Consts.MARSHALLING_BUFSIZE, Defaults.DEFAULT_MARSHALLING_BUFSIZE);
  private boolean useMarshalling;
  private Object data;
  private volatile InMemoryResponse response;

  InMemoryRequest(boolean useMarshalling, Object data) throws IOException {
    this.useMarshalling = useMarshalling;
    if (useMarshalling) {
      ByteArrayOutputStream byteOs = new ByteArrayOutputStream(bufsz);
      ObjectOutputStream marshalOs = MarshalStreamFactory.createOutputStream(byteOs);
      ((RmiObjectOutput) marshalOs).setUp(VmId.getInstance(), InMemoryAddress.TRANSPORT_TYPE);
      marshalOs.writeObject(data);
      this.data = byteOs.toByteArray();
    } else {
      this.data = data;
    }
  }

  public boolean isUseMarshalling() {
    return useMarshalling;
  }

  /**
   * @return this instance's data, in the form of an arbitrary {@link Object}.
   */
  Object getData() throws IOException, ClassNotFoundException {
    if (useMarshalling) {
      ByteArrayInputStream byteIs = new ByteArrayInputStream((byte[]) data);
      ObjectInputStream marshalIs = MarshalStreamFactory.createInputStream(byteIs);
      return marshalIs.readObject();
    } else {
      return data;
    }
  }

  /**
   * Blocks until this instance's response is set.
   * 
   * @return the response that was returned.
   * @throws InterruptedException
   *           if the calling thread is interrupted while waiting.
   */
  synchronized Object waitForResponse() throws InterruptedException, IOException, ClassNotFoundException {
    while (response == null) {
      wait();
    }
    return response.getData();
  }
  
  /**
   * Blocks until this instance's response is set.
   * 
   * @param timeout a timeout to wait for, in millis.
   * @return the response that was returned.
   * @throws InterruptedException
   *           if the calling thread is interrupted while waiting.
   */
  synchronized Object waitForResponse(long timeout) throws InterruptedException, IOException, ClassNotFoundException, SocketTimeoutException {
    Pause pause = new Pause(timeout);
    while (!pause.isOver() && response == null) {
      wait(pause.remainingNotZero());
    }
    if (response == null) {
      throw new SocketTimeoutException("Response not received within specified timeout");
    }
    return response.getData();
  }

  /**
   * @param responseData
   *          the response data (in the form of an arbitratry {@link Object}) to
   *          assign to this instance.
   */
  synchronized void setResponse(Object responseData) throws IOException {
    response = new InMemoryResponse(responseData);
    notify();
  }

  private class InMemoryResponse {

    private Object data;

    public InMemoryResponse(Object data) throws IOException {
      if (useMarshalling) {
        ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
        ObjectOutputStream marshalOs = MarshalStreamFactory.createOutputStream(byteOs);
        ((RmiObjectOutput) marshalOs).setUp(VmId.getInstance(), InMemoryAddress.TRANSPORT_TYPE);
        marshalOs.writeObject(data);
        this.data = byteOs.toByteArray();
      } else {
        this.data = data;
      }
    }

    public Object getData() throws IOException, ClassNotFoundException {
      if (useMarshalling) {
        ByteArrayInputStream byteIs = new ByteArrayInputStream((byte[]) data);
        ObjectInputStream marshalIs = MarshalStreamFactory.createInputStream(byteIs);
        return marshalIs.readObject();
      } else {
        return data;
      }
    }
  }

}
