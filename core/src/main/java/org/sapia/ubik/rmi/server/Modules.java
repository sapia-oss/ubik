package org.sapia.ubik.rmi.server;

import org.sapia.ubik.module.Module;
import org.sapia.ubik.module.ModuleContainer;
import org.sapia.ubik.rmi.server.command.CommandModule;
import org.sapia.ubik.rmi.server.gc.ClientGC;
import org.sapia.ubik.rmi.server.gc.ServerGC;
import org.sapia.ubik.rmi.server.invocation.InvocationDispatcher;
import org.sapia.ubik.rmi.server.stats.StatsModule;
import org.sapia.ubik.rmi.server.stub.StatelessStubTable;
import org.sapia.ubik.rmi.server.transport.TransportManager;
import org.sapia.ubik.rmi.threads.ThreadModule;
import org.sapia.ubik.taskman.MultiThreadedTaskManager;
import org.sapia.ubik.taskman.TaskManager;

/**
 * Encapsulates a {@link ModuleContainer}, holds references to built-in module -
 * providing methods for retrieving each one.
 * 
 * @author yduchesne
 * 
 */
public class Modules {

  private ModuleContainer delegate = new ModuleContainer();
  private MultiThreadedTaskManager taskManager;
  private TransportManager transportManager;
  private EventChannelTable eventChannelTable;
  private StatelessStubTable statelessStubTable;
  private ObjectTable objectTable;
  private ServerTable serverTable;
  private AsyncCommandSender asyncSender;
  private StubProcessor stubProcessor;
  private ClientRuntime clientRuntime;
  private ServerRuntime serverRuntime;
  private CommandModule callback;
  private InvocationDispatcher invocationDispatcher;
  private ClientGC clientGC;
  private ServerGC serverGC;
  private ThreadModule threads;

  public Modules() {
    bind(new StatsModule());
    bind(threads  = new ThreadModule());
    bind(TaskManager.class, taskManager = new MultiThreadedTaskManager());
    bind(transportManager = new TransportManager());
    bind(eventChannelTable = new EventChannelTable());
    bind(statelessStubTable = new StatelessStubTable());
    bind(objectTable = new ObjectTable());
    bind(serverTable = new ServerTable());
    bind(asyncSender = new AsyncCommandSender());
    bind(stubProcessor = new StubProcessor());
    bind(clientRuntime = new ClientRuntime());
    bind(serverRuntime = new ServerRuntime());
    bind(callback = new CommandModule());
    bind(invocationDispatcher = new InvocationDispatcher());
    bind(clientGC = new ClientGC());
    bind(serverGC = new ServerGC());
  }

  void init() {
    delegate.init();
  }

  void start() {
    delegate.start();
  }

  void stop() {
    delegate.stop();
  }

  boolean isStarted() {
    return delegate.isStarted();
  }

  void bind(Module module) {
    delegate.bind(module);
  }

  void bind(Class<?> moduleInterface, Module module) {
    delegate.bind(moduleInterface, module);
  }

  public <T> T lookup(Class<T> clazz) {
    return delegate.lookup(clazz);
  }

  public TaskManager getTaskManager() {
    return taskManager;
  }

  public TransportManager getTransportManager() {
    return transportManager;
  }

  public ObjectTable getObjectTable() {
    return objectTable;
  }

  public EventChannelTable getEventChannelTable() {
    return eventChannelTable;
  }

  public StatelessStubTable getStatelessStubTable() {
    return statelessStubTable;
  }

  public ServerTable getServerTable() {
    return serverTable;
  }

  public AsyncCommandSender getAsyncSender() {
    return asyncSender;
  }

  public StubProcessor getStubProcessor() {
    return stubProcessor;
  }

  public ClientRuntime getClientRuntime() {
    return clientRuntime;
  }

  public ServerRuntime getServerRuntime() {
    return serverRuntime;
  }

  public InvocationDispatcher getInvocationDispatcher() {
    return invocationDispatcher;
  }

  public ClientGC getClientGC() {
    return clientGC;
  }

  public ServerGC getServerGC() {
    return serverGC;
  }

  public CommandModule getCommandModule() {
    return callback;
  }
  
  public ThreadModule getThreads() {
    return threads;
  }

}
