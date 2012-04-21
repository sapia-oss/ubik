package org.sapia.ubik.rmi.server.oid;

import org.sapia.ubik.rmi.server.StubProcessor;

/**
 * This interface specifies {@link OID} creation behavior. It allows plugging in
 * custom logic.
 * <p>
 * Implementations of this interface must be registered with the {@link StubProcessor} module (using either the
 * {@link StubProcessor#appendOIDCreationStrategy(OIDCreationStrategy)} and {@link StubProcessor#insertOIDCreationStrategy(OIDCreationStrategy)}
 * methods).
 * 
 * @author yduchesne
 *
 */
public interface OIDCreationStrategy {

	/**
	 * @param toExport the {@link Object} to export as a remote object, and for which
	 * to create a {@link OID}.
	 * @return <code>true</code> if this instance can generate an {@link OID} for the
	 * given object.
	 */
	public boolean apply(Object toExport);

	/**
	 * Creates a new {@link OID} for the given object.
	 * 
	 * @param toExport the {@link Object} to export as remote object.
	 * @return a new {@link OID} for the given object.
	 */
	public OID createOID(Object toExport);
}
