package org.sapia.ubik.test.gc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.test.gc.EchoService.EchoMode;

public class CallbackClientStarter {
  
  public static long SEQUENCE = ((CallbackClientStarter.class.hashCode() % 1000000) / 1000);
  public static long CALL_COUNTER = 0;
  
  public static void main(String[] args) {
    System.setProperty("ubik.rmi.log.level", "info");
    System.setProperty("ubik.rmi.server.gc.interval", "20000");
    System.setProperty("ubik.rmi.server.gc.timeout", "41000");

    Runtime.getRuntime().addShutdownHook(new ShutdownTask());

    GarbageGenerator.startDaemon();

    CallbackClientFrame frame = new CallbackClientFrame();
    frame.init();
    frame.show();
    
    try {
//      EchoService echoService = (EchoService) Hub.connect("192.168.1.102", 9800);
//      System.out.println("CallbackClientStarter ===> Got remote echo service from port 9800");
//      
//      long idSequence = ((CallbackClientStarter.class.hashCode() % 1000000) / 1000);
//      int callCounter = 0;
//      
//      CallbackServiceImpl service = new CallbackServiceImpl(String.valueOf(++idSequence));
//      echoService.registerCallback(service);
//      System.out.println("CallbackClientStarter ===> Exported remote callback service at address " + Hub.serverRuntime.server.getServerAddress());
//      System.out.println("CallbackClientStarter ===> Registered callback with remote echo service");

      while (true) {

//        Thread.sleep(10000);
//        System.out.println("CallbackClientStarter ===> Calling echo server...");
//        echoService.echo("Call #" + ++callCounter + " from vm id " + VmId.getInstance(), EchoMode.SYNCHRONOUS);
//        
//        System.out.println("CallbackClientStarter ===> Waiting 70 seconds");
        Thread.sleep(70000);
//
//        System.out.println("CallbackClientStarter ===> Unregister callback with remote echo service");
//        echoService.unregisterCallback(service);
//        service = null;
//        
//        System.out.println("CallbackClientStarter ===> Waiting 70 seconds");
//        Thread.sleep(70000);
//
//        System.out.println("CallbackClientStarter ===> Running GC...");
//        System.gc();
//        Thread.sleep(5000);
//
//        service = new CallbackServiceImpl(String.valueOf(++idSequence));
//        echoService.registerCallback(service);
//        System.out.println("CallbackClientStarter ===> Registered callback with remote echo service");
      }
      
    } catch (Exception e) {
      System.err.println("CallbackClientStarter ===> error running echo server: " + e.getMessage());
      e.printStackTrace();
    }
    
  }
  
  public static class ShutdownTask extends Thread {
    public void run() {
      try {
        Hub.shutdown(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
  
  public static class CallbackClientFrame implements WindowListener, ActionListener, CallbackService {
    private JFrame _frame;
    private JButton _btnRegister;
    private JButton _btnUnregister;
    private JButton _btnDoEcho;
    private JTextArea _txaCallbackValues;
    private Executor _executor = Executors.newFixedThreadPool(2);
    
    private EchoService _echoService;
    private CallbackServiceImpl _callback;

    public void init() {
      if (!SwingUtilities.isEventDispatchThread()) {
        try {
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              init();
            }
          });
        } catch (Exception e) {
          e.printStackTrace();
        }
        return;
      }
      
      //  Use the System look and feel.
      try {
          UIManager.setLookAndFeel(
              UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
      }

      //Make sure we have nice window decorations.
      JFrame.setDefaultLookAndFeelDecorated(true);
      JDialog.setDefaultLookAndFeelDecorated(true);

      _btnRegister = new JButton("Register Callback");
      _btnRegister.addActionListener(this);
      _btnUnregister = new JButton("Unregister Callback");
      _btnUnregister.addActionListener(this);
      _btnDoEcho = new JButton("Do Echo");
      _btnDoEcho.addActionListener(this);
      _txaCallbackValues = new JTextArea(10, 60);
      _txaCallbackValues.setEditable(false);
      _txaCallbackValues.setText("[" + new Date() + "] Client started");
      
      _frame = new JFrame("Callback Client");
      _frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      _frame.addWindowListener(this);
      _frame.getContentPane().setLayout(new GridBagLayout());
      _frame.getContentPane().add(_btnRegister, new GridBagConstraints(0, 0, 1, 1, 0.3, 0.0, 
              GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5,5,6,6), 0, 0));
      _frame.getContentPane().add(_btnDoEcho, new GridBagConstraints(1, 0, 1, 1, 0.4, 0.0, 
              GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5,5,6,6), 0, 0));
      _frame.getContentPane().add(_btnUnregister, new GridBagConstraints(2, 0, 1, 1, 0.3, 0.0, 
              GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(5,5,6,6), 0, 0));
      _frame.getContentPane().add(new JScrollPane(_txaCallbackValues), new GridBagConstraints(0, 1, 3, 1, 1.0, 1.0, 
              GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(5,5,6,6), 0, 0));
      _frame.pack();
    }
    
    public void show() {
      if (!SwingUtilities.isEventDispatchThread()) {
        try {
          SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
              show();
            }
          });
        } catch (Exception e) {
          e.printStackTrace();
        }
        return;
      }
      
      _frame.setLocationByPlatform(true);
//      _frame.setSize(400, 200);
      _frame.setVisible(true);
    }
    
    public void closeAndExit() {
      _frame.setVisible(false);
      System.exit(0);
    }
    
    public void registerCallback() {
      if (_callback == null) {
        try {
          _echoService = (EchoService) Hub.connect("192.168.1.102", 9800);
          System.out.println("CallbackClientStarter ===> Got remote echo service from port 9800");
  
          _callback = new CallbackServiceImpl(String.valueOf(++SEQUENCE));
          _callback.setDelegate(this);
          _echoService.registerCallback(_callback);
          System.out.println("CallbackClientStarter ===> Exported remote callback service at address " + Hub.serverRuntime.server.getServerAddress());
          System.out.println("CallbackClientStarter ===> Registered callback with remote echo service");
          appendText("Registered callback with echo service");

        } catch (Exception e) {
          _callback = null;
          e.printStackTrace();
        }
      }
    }
    
    public void unregisterCallback() {
      if (_callback != null) {
        try {
          _echoService.unregisterCallback(_callback);
          System.out.println("CallbackClientStarter ===> Unregister callback from remote echo service");
          appendText("Unregistered callback from echo service");
  
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          _callback = null;
          _echoService = null;
        }
      }
    }
    
    public void sendEcho() {
      if (_echoService != null) {
        try {
          System.out.println("CallbackClientStarter ===> Calling echo server...");          
          _echoService.echo("Call #" + ++CALL_COUNTER + " from " + _callback.getServiceId(), EchoMode.SYNCHRONOUS);
  
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    
    public void appendText(final String aValue) {
      if (!SwingUtilities.isEventDispatchThread()) {
        try {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              appendText(aValue);
            }
          });
        } catch (Exception e) {
          e.printStackTrace();
        }
        return;
      }
      
      _txaCallbackValues.setText(_txaCallbackValues.getText() + "\n[" + new Date() + "] " + aValue);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent anEvent) {
      if (anEvent.getSource() == _btnRegister) {
        _executor.execute(new Runnable() {
          public void run() {
            registerCallback();
          }
        });
        
      } else if (anEvent.getSource() == _btnUnregister) {
        _executor.execute(new Runnable() {
          public void run() {
            unregisterCallback();
          }
        });
        
      } else if (anEvent.getSource() == _btnDoEcho) {
        _executor.execute(new Runnable() {
          public void run() {
            sendEcho();
          }
        });
        
      }
    }

    /* (non-Javadoc)
     * @see org.sapia.ubik.test.gc.CallbackService#callback(java.lang.String)
     */
    public void callback(String aValue) {
      appendText(aValue);
    }

    /* (non-Javadoc)
     * @see org.sapia.ubik.test.gc.CallbackService#getServiceId()
     */
    public String getServiceId() {
      if (_callback != null) {
        return _callback.getServiceId();
      } else {
        return ""+hashCode();
      }
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
     */
    public void windowActivated(WindowEvent anEvent) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
     */
    public void windowClosed(WindowEvent anEvent) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
     */
    public void windowClosing(WindowEvent anEvent) {
      closeAndExit();
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
     */
    public void windowDeactivated(WindowEvent anEvent) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
     */
    public void windowDeiconified(WindowEvent anEvent) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
     */
    public void windowIconified(WindowEvent anEvent) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
     */
    public void windowOpened(WindowEvent anEvent) {
    }
  }

}
