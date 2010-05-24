package org.sapia.ubik.rmi.examples;

import org.sapia.ubik.rmi.naming.remote.*;
import org.sapia.ubik.rmi.server.Log;

import java.rmi.Naming;
import java.rmi.RemoteException;

import java.util.Properties;

import javax.naming.*;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Bench {
  public static void main(String[] args) {
    //PerfAnalyzer.getInstance().setEnabled(true);
    Log.setWarning();

    //   	System.setProperty(Consts.LOG_LEVEL, "debug");  	
    try {
      Foo f;

      /*
      f = getJdkFoo();
      System.out.println("==== Producing numbers for Java RMI... ====");
      System.out.println();
      doGetBar(f);
      System.out.println();
      doGetMsg(f);*/

      /**
      System.out.println();
      f = getStandardUbikFoo();
      System.out.println("==== Producing numbers for Ubik standard RMI... ====");
      System.out.println();
      doGetBar(f);
      System.out.println();
      doGetMsg(f);*/
      
      
      System.out.println();
      f = getNioUbikFoo();
      System.out.println("==== Producing numbers for Ubik NIO RMI... ====");
      System.out.println();
      doGetBar(f);
      System.out.println();
      doGetMsg(f);

      System.out.println("Press CTRL-C to exit");

      while (true) {
        Thread.sleep(100000);
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  private static Foo getJdkFoo() throws Throwable {
    return (Foo) Naming.lookup("rmi://localhost:1098/Foo");
  }

  private static Foo getStandardUbikFoo() throws Throwable {
    Properties props = new Properties();
    props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099/");
    props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
      RemoteInitialContextFactory.class.getName());

    InitialContext ctx = new InitialContext(props);

    return (Foo) ctx.lookup("Foo");
  }
  
  private static Foo getNioUbikFoo() throws Throwable {
    Properties props = new Properties();
    props.setProperty(InitialContext.PROVIDER_URL, "ubik://localhost:1099/");
    props.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY,
      RemoteInitialContextFactory.class.getName());

    InitialContext ctx = new InitialContext(props);

    return (Foo) ctx.lookup("NioFoo");
  }  

  private static void doGetBar(Foo f) throws RemoteException {
    System.out.println("Calling getBar() on Foo (returns a remote Object)");

    for (int i = 0; i < 4; i++) {
      long start     = 0;
      long total     = 0;
      long maxTime   = 1000;
      int  callCount = 0;

      System.out.println();
      System.out.println(">>run " + (i + 1));

      while (true) {
        start = System.currentTimeMillis();
        f.getBar();
        total = total + (System.currentTimeMillis() - start);
        callCount++;

        if (total >= maxTime) {
          break;
        }
      }

      System.out.println("" + callCount + "/" + total +
        " (number of calls/number of millis)");
    }
  }

  private static void doGetMsg(Foo f) throws RemoteException {
    System.out.println("Calling getMsg() on Bar (getMsg() returns a string)");

    Bar b = f.getBar();

    for (int i = 0; i < 4; i++) {
      long start     = 0;
      long total     = 0;
      long maxTime   = 1000;
      int  callCount = 0;

      System.out.println();
      System.out.println(">>run " + (i + 1));

      while (true) {
        start = System.currentTimeMillis();
        b.getMsg();
        total = total + (System.currentTimeMillis() - start);
        callCount++;

        if (total >= maxTime) {
          break;
        }
      }

      System.out.println("" + callCount + "/" + total +
        " (number of calls/number of millis)");
    }
  }
}
