package org.sapia.ubik.rmi.server.oid;

/**
 * This interface defines the behavior of remote object identifiers. It does not
 * specify methods of its own: rather, the methods that implementing classes are
 * expected to provide are the following (which are inherited from the
 * {@link Object} class):
 * <ul>
 * <li> {@link #equals(Object)}: an instance of this class must be able to
 * compare itself against both instances of the same class and of different
 * classes.
 * <li> {@link #hashCode()}: an instance of this class must return a hashcode
 * that allows it to be kept in hashing data structures.
 * </ul>
 * 
 * @author yduchesne
 * 
 */
public interface OID {

}