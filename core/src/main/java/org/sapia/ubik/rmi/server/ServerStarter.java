package org.sapia.ubik.rmi.server;

import static java.lang.System.out;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Properties;

import org.sapia.ubik.util.PropUtil;
import org.sapia.ubik.util.cli.Cmd;
import org.sapia.ubik.util.cli.Cmd.OptionFilter;
import org.sapia.ubik.util.cli.Opt;

/**
 * Helper class to start <code>main()</code> classes from the command-line in a
 * generic manner. This class expects the first argument to be the
 * fully-qualified name of the class whose <code>main()</code> method is to be
 * invoked.
 * 
 * @author yduchesne
 * 
 */
public class ServerStarter {

  public static void main(String[] args) {

    Cmd cmd = Cmd.fromArgs(args).filter(new OptionFilter() {
      @Override
      public boolean accepts(Opt option) {
        return !option.getName().contains(".sh");
      }
    });

    Properties props = cmd.removeJvmOpts();
    PropUtil.copy(props, System.getProperties());

    if (cmd.getOpts().isEmpty()) {
      out.println("Missing class with main() method");
      help();
    } else if (cmd.getOpts().get(0).getType().isSwitch()) {
      out.println("First argument should be the name of the class to start.");
      help();
    } else {
      try {
        Class<?> serverClass = Class.forName(cmd.getOpts().get(0).getName().trim());
        Method method = serverClass.getDeclaredMethod("main", String[].class);
        if (Modifier.isStatic(method.getModifiers())) {
          out.println("Starting server " + serverClass.getName() + " started...");
          method.invoke(null, new Object[] { args });
        } else {
          out.println("main() method must be static");
        }
      } catch (Exception e) {
        out.println("Could not load/start server class");
        e.printStackTrace();
      }
    }

  }

  private static void help() {
    out.println();
    out.println("Syntax:");
    out.println();
    out.println("./starter.sh <classname> [<arguments>]");
    out.println();
  }
}
