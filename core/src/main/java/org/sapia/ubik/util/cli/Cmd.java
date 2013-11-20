package org.sapia.ubik.util.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.sapia.ubik.util.Strings;
import org.sapia.ubik.util.cli.Opt.Type;

/**
 * Models a command entered on the command line.
 * 
 * @author yduchesne
 * 
 */
public class Cmd {

  public interface OptionFilter {
    public boolean accepts(Opt option);
  }

  private List<Opt> opts = new ArrayList<Opt>();
  private Map<String, Opt> optsByName = new HashMap<String, Opt>();

  private Cmd(List<Opt> opts) {
    this.opts.addAll(opts);
    for (Opt o : opts) {
      optsByName.put(o.getName(), o);
    }
  }

  /**
   * @param name
   *          the name of the {@link Opt} instance to return.
   * @return the {@link Opt} for the given name, or <code>null</code> if no such
   *         instance is found.
   */
  public Opt getOpt(String name) {
    return optsByName.get(name);
  }

  /**
   * @param name
   *          the name of the {@link Opt} instance to return.
   * @return the {@link Opt} corresponding to the given name.
   * @throws IllegalArgumentException
   *           if the option with the given does not exist, or if it has no
   *           value.
   */
  public Opt getOptWithValue(String name) throws IllegalArgumentException {
    Opt opt = optsByName.get(name);
    if (opt == null) {
      throw new IllegalArgumentException("No option found with name: " + name);
    }
    if (opt == null || opt.getTrimmedValueOrBlank().length() == 0) {
      throw new IllegalArgumentException("No value specified for " + name);
    }
    return opt;
  }

  /**
   * @return this instance's {@link List} of options.
   */
  public List<Opt> getOpts() {
    return Collections.unmodifiableList(opts);
  }

  /**
   * Returns a copy of this instance, but only with the options of the given
   * type.
   * 
   * @param type
   *          the {@link Type} of option to return.
   * @return a new {@link Cmd} instance, with the desired options.
   */
  public Cmd filter(final Type type) {
    return filter(new OptionFilter() {
      @Override
      public boolean accepts(Opt option) {
        return option.getType() == type;
      }
    });
  }

  /**
   * Returns a copy of this instance, but only with the options that have been
   * accepted by the given filter.
   * 
   * @param filter
   *          an {@link OptionFilter}.
   * @return a new {@link Cmd} instance, with the desired options.
   */
  public Cmd filter(OptionFilter filter) {
    List<Opt> filtered = new ArrayList<Opt>();
    for (Opt o : this.opts) {
      if (filter.accepts(o)) {
        filtered.add(o);
      }
    }
    return new Cmd(filtered);
  }

  /**
   * @param name
   *          the name of the switch to check for.
   * @return <code>true</code> if this instance contains an {@link Opt} instance
   *         with the given name and of type {@link Type#SWITCH}.
   */
  public boolean hasSwitch(String name) {
    Opt opt = optsByName.get(name);
    if (opt == null) {
      return false;
    }
    return opt.getType() == Type.SWITCH;
  }

  /**
   * @param args
   *          some arguments (typically from a <code>main()</code> method).
   * @return a new {@link Cmd} from the given arguments.
   */
  public static Cmd fromArgs(String[] args) {

    List<Opt> opts = new ArrayList<Opt>();

    for (int i = 0; i < args.length; i++) {
      String argName = args[i];
      if (argName.startsWith("-")) {
        if (argName.length() == 1) {
          continue;
        }
        if (i + 1 < args.length) {
          String argValue = args[i + 1];
          if (!argValue.startsWith("-")) {
            opts.add(new Opt(Opt.Type.SWITCH, argName.substring(1), argValue));
            i++;
          } else {
            opts.add(new Opt(Opt.Type.SWITCH, argName.substring(1), null));
          }
        } else {
          opts.add(new Opt(Opt.Type.SWITCH, argName.substring(1), null));
        }
      } else {
        opts.add(new Opt(Opt.Type.ARG, argName, null));
      }
    }

    return new Cmd(opts);

  }

  /**
   * @return this instance's {@link Opt} instances that correspond to JVM
   *         properties (of the form <code>-Dprop=value</code>).
   */
  public Properties getJvmOpts() {
    Properties props = new Properties();
    for (Opt opt : opts) {
      if (opt.getName().startsWith("D") && opt.getValue() == null && opt.getName().contains("=")) {
        String[] nameAndValue = opt.getName().split("=");
        props.setProperty(nameAndValue[0].substring(1), nameAndValue[1]);
      }
    }
    return props;
  }

  /**
   * @return this instance's {@link Opt} instances that correspond to JVM
   *         properties (of the form <code>-Dprop=value</code>).
   */
  public Properties removeJvmOpts() {
    Properties props = new Properties();
    for (int i = 0; i < opts.size(); i++) {
      Opt opt = opts.get(i);
      if (opt.getName().startsWith("D") && opt.getValue() == null && opt.getName().contains("=")) {
        String[] nameAndValue = opt.getName().split("=");
        props.setProperty(nameAndValue[0].substring(1), nameAndValue[1]);
        opts.remove(i);
        i--;
        optsByName.remove(opt.getName());
      }
    }
    return props;
  }

  /**
   * @param name
   *          the name of the option to remove.
   */
  public void removeOpt(String name) {
    this.optsByName.remove(name);
  }

  /**
   * Returns this instance's {@link Opt}s that are of {@link Type#SWITCH} type
   * in a {@link Properties} instance.
   * 
   * @return this instance's switches in a {@link Properties} instance.
   */
  public Properties getSwitchesAsProperties() {
    Properties props = new Properties();
    copySwitchesTo(props);
    return props;
  }

  /**
   * Copies this instance's {@link Opt}s that are of {@link Type#SWITCH} type to
   * the given {@link Properties} instance.
   */
  public void copySwitchesTo(Properties props) {
    for (Opt opt : opts) {
      if (opt.getType() == Type.SWITCH && opt.getValue() != null) {
        props.setProperty(opt.getName(), opt.getValue());
      }
    }
  }

  @Override
  public String toString() {
    return Strings.toString("opts", opts, "optsByName", optsByName);
  }

}
