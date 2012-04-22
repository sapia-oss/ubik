package org.sapia.ubik.rmi.examples.load;

import java.io.File;
import java.util.Properties;

import javax.naming.Context;

import org.sapia.ubik.rmi.naming.remote.JNDIConsts;
import org.sapia.ubik.rmi.naming.remote.JNDIContextBuilder;
import org.sapia.ubik.rmi.server.transport.nio.tcp.NioServerExporter;
import org.sapia.ubik.util.PropertiesUtil;
import org.sapia.ubik.util.cli.Cmd;

public class LoadServer {

	public static void main(String[] args) throws Exception {
		
		Cmd cmd = Cmd.fromArgs(args);
		
		if (cmd.hasSwitch("h")) {
			System.out.println("-c: The fully qualified name of the LoadService interface implementation");
			System.out.println("    to bind to the JNDI. Defaults to " + NoopLoadService.class.getName());
			System.out.println("-h: The host of the JNDI server to connect to. Defaults to localhost.");
			System.out.println("-p: The port of the JNDI server to connect to. Defaults to " + JNDIConsts.DEFAULT_PORT);
			System.out.println("-f: The path to the Ubik properties file to load (OPTIONAL)");
			return;
		}
		
		LoadService impl = null;
		
		try {
  		impl = (LoadService) Class.forName(
  				cmd.hasSwitch("c") ? cmd.getOptWithValue("c").getTrimmedValueOrBlank() : NoopLoadService.class.getName()
  		).newInstance();
		} catch (Exception e) {
			System.out.println("Could not instantiate LoadService implementation, aborting");
			e.printStackTrace();
			return;
		}
		
		Properties props = new Properties();
		if (cmd.hasSwitch("f")) {
			File f = new File(cmd.getOptWithValue("f").getTrimmedValueOrBlank());
			PropertiesUtil.loadIntoPropertiesFrom(props, f);
		}
		PropertiesUtil.copy(props, System.getProperties());
		
		Context context = JNDIContextBuilder.newInstance().properties(props)
				.port(cmd.hasSwitch("p") ? cmd.getOptWithValue("p").getIntValue() : JNDIConsts.DEFAULT_PORT)
				.host(cmd.hasSwitch("h") ? cmd.getOptWithValue("h").getValue() : "localhost")
				.build();

		NioServerExporter exporter = new NioServerExporter();
		context.bind(LoadService.JNDI_NAME, exporter.export(impl));
		context.close();
			  
		while (true) {
			Thread.sleep(100000);
		}
		
  }
	
}
