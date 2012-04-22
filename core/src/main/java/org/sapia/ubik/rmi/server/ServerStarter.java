package org.sapia.ubik.rmi.server;

import static java.lang.System.out;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.sapia.ubik.util.cli.Cmd;

/**
 * Helper class to start <code>main()</code> classes from the command-line in a generic manner. This class
 * expects a <code>-c</code> switch to be specified, which is expected to provide the fully-qualified
 * name of the class whose <code>main()</code> method is to be invoked.
 * 
 * @author yduchesne
 *
 */
public class ServerStarter {

	public static void main(String[] args) {
	  
		Cmd cmd = Cmd.fromArgs(args);
		
		if (!cmd.hasSwitch("c")) {
			out.println("Missing -c switch");
			help();
		} else {
			try {
				Class<?> serverClass = Class.forName(cmd.getOptWithValue("c").getTrimmedValueOrBlank());
				Method method = serverClass.getDeclaredMethod("main", String[].class);
				if (Modifier.isStatic(method.getModifiers())) {
					
					method.invoke(null, (Object[]) args);
					
					out.println("Server " + serverClass.getName() + " started...");
					
					while (true) {
						try {
							Thread.sleep(100000);
						} catch (InterruptedException e) {
							out.println("Thread interrupted, exiting");
							break;
						}
					}
					
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
		out.println("Command-line options:");
		out.println();
		out.println("-c : The name of the Server class whose main() method should be called.");
		out.println();
	}
}
