package org.sapia.ubik.rmi.server.command;

/**
 * A command class - implements the Command pattern.
 * 
 * @author Yanick Duchesne
 */
public abstract class Command implements Executable, java.io.Serializable {

  static final long serialVersionUID = 1L;

  /**
   * Constructor for Command.
   */
  public Command() {
    super();
  }

  /***
   * This method executes this command.
   * 
   * @return any value returned by this method.
   * @throws Throwable
   *           if an error occurs executing this command.
   */
  public abstract Object execute() throws Throwable;
}
