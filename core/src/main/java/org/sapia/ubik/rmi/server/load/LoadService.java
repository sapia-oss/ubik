package org.sapia.ubik.rmi.server.load;

public interface LoadService {
	
	public static final String JNDI_NAME = "LoadService";

	public void perform();
}
