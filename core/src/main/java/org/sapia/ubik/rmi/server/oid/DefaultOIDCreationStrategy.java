package org.sapia.ubik.rmi.server.oid;

import org.sapia.ubik.rmi.server.UIDGenerator;

/**
 * Provides a default {@link OIDCreationStrategy} - which creates {@link DefaultOID} instances.
 * 
 * @author yduchesne
 *
 */
public class DefaultOIDCreationStrategy implements OIDCreationStrategy {

	/**
	 * @return <code>true</code>.
	 */
	@Override
	public boolean apply(Object toExport) {
	  return true;
	}
	
	/**
	 * @param toExport the {@link Object} that will be exported as a remote object.
	 * @return a new {@link DefaultOID}.
	 */
	@Override
	public OID createOID(Object toExport) {
    return new DefaultOID(UIDGenerator.createUID());
	}

}
