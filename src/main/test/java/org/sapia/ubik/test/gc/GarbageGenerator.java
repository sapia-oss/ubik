package org.sapia.ubik.test.gc;

import java.util.ArrayList;

public class GarbageGenerator implements Runnable {
  private static GarbageGenerator _INSTANCE = null;
  
  public synchronized static void startDaemon() {
    if (_INSTANCE == null) {
      _INSTANCE = new GarbageGenerator();
      Thread t = new Thread(_INSTANCE);
      t.setDaemon(true);
      t.start();
    }
  }
  
  public void run() {
    while (true) {
      try {
        ArrayList<byte[]> list = new ArrayList<byte[]>();
        for (int i = 0; i < 1024; i++) {
          byte[] data = new byte[10240];
          list.add(data);
          for (int j = 0; j < data.length; j++) {
            data[j] = (byte) (j%256);
          }
          Thread.sleep(10);
        }
        System.err.println("==> 10 MB GARBAGE!!!!");
        Thread.sleep(1000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}