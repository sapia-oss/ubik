package org.sapia.ubik.rmi.examples.site.transport.nio;

import org.sapia.ubik.rmi.server.transport.mina.MinaServerExporter;

public class NioExporterExample {
	
	public static void main(String[] args) {
	  
		MinaServerExporter exporter = new MinaServerExporter();
		exporter.port(7070);
		// Hello stub = exporter.export(new HelloImpl());
		
		
		
		
  }

}
