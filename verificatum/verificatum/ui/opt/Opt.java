
/*
 * Copyright 2008 2009 2010 Douglas Wikstrom
 *
 * This file is part of Verificatum.
 *
 * Verificatum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Verificatum is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Verificatum.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package verificatum.ui.opt;

import java.util.*;

import verificatum.ui.*;
import verificatum.util.*;

/**
 * Simple command line parser. It allows the definition both of
 * options that take parameters and options that do not take
 * parameters. It also allows definition of named parameters. The
 * possible ways to invoke the command under consideration are divided
 * into families. Each family is represented by a "usage form"
 * implemented by {@link UsageForm}. A usage form defines a set of
 * required options, a set of optional options, a set of required
 * parameters, and a set of optional parameters. It can also handle
 * variable number of parameters.
 *
 * <p>
 *
 * Options are assumed to start with a hyphen, e.g., -t, or -my_option.
 *
 * <p>
 *
 * After parsing command line arguments the first matching usage form
 * is used to interpret the inputs. Then the inputs can be accessed
 * using the names of options or parameters respectively.
 *
 * <p>
 *
 * Usage forms can be built up by appending new options and parameters
 * to an existing usage form. This allows subclasses to add new
 * options and parameters to those assumed to exist by a superclass.
 *
 * <p>
 *
 * Finally, a usage description of the command under consideration can
 * be generated.
 *
 * @author Douglas Wikstrom
 */
public class Opt {

    /**
     * Type of an option.
     */
    public enum Type {
        /**
         * <code>String</code> option.
         */
        STRING,

        /**
         * <code>int</code> option.
         */
        INT};

    /**
     * Line width of descriptions.
     */
    final static int LINE_WIDTH = 78;

    /**
     * Name of command of which we parse the command line. In a
     * typical application the execution of this class is wrapped in a
     * shell script. The name of the shell script is used to generate
     * proper usage information.
     */
    protected String commandName;

    /**
     * All possible options of the command.
     */
    protected TreeMap<String, Option> options;

    /**
     * All possible parameters of the command.
     */
    protected TreeMap<String, Parameter> parameters;

    /**
     * All possible usage forms of the command.
     */
    protected ArrayList<UsageForm> usageForms;

    /**
     * Actual options extracted from a command line.
     */
    protected TreeMap<String, ArrayList<String>> givenOptions;

    /**
     * Actual parameters extracted from a command line.
     */
    protected ArrayList<String> givenParameters;

    /**
     * First usage form that matches the actual options and parameters
     * extracted from the command line.
     */
    protected UsageForm uf;

    /**
     * Description of this command.
     */
    protected String description;

    /**
     * Brief comment on usage forms.
     */
    protected String usageComment;

    /**
     * Default error string printed when called without options.
     */
    protected String defaultErrorString;

    /**
     * Creates a command line parser for a given command name.
     *
     * @param commandName Name of the command.
     * @param defaultErrorString Default error string output when
     * command line parameters are wrong.
     */
    public Opt(String commandName, String defaultErrorString) {
        this.commandName = commandName;
        this.defaultErrorString = defaultErrorString;
        this.description = "";
        this.uf = null;
        options = new TreeMap<String, Option>();
        parameters = new TreeMap<String, Parameter>();
        givenOptions = new TreeMap<String, ArrayList<String>>();
        givenParameters = new ArrayList<String>();
        usageForms = new ArrayList<UsageForm>();
        usageComment = "";
    }

    /**
     * Expands the description.
     *
     * @param description Information to be added to the description.
     */
    public void appendDescription(String description) {
        this.description += description;
    }

    /**
     * Replaces the description.
     *
     * @param description New description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets a brief comment for the usage forms.
     *
     * @param usageComment Comment.
     */
    public void setUsageComment(String usageComment) {
        this.usageComment = usageComment;
    }

    /**
     * Adds an option with the given name, value-name, and
     * description. The value-name is used to illustrate an option
     * parameter in the usage description.
     *
     * @param name Name of option, e.g., -f, or -t.
     * @param valueName Name used to illustrate the value of the
     * option. This must be the empty string if the option does not
     * take any parameter, and it must not be the empty string if the
     * option takes a parameter.
     * @param description Description of this option.
     */
    public void addOption(String name, String valueName, String description) {
        options.put(name, new Option(description, valueName));
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * there is an option with the given name.
     *
     * @param name Name of candidate option.
     * @return <code>true</code> or <code>false</code> depending on if
     * there is an option with the given name.
     */
    public boolean hasOption(String name) {
        return options.containsKey(name);
    }

    /**
     * Adds an option with the given name, value-name, and
     * description. The value-name is used to illustrate an option
     * parameter in the usage description.
     *
     * @param name Name of option, e.g., -f, or -t.
     * @param valueName Name used to illustrate the value of the
     * option. This must be the empty string if the option does not
     * take any parameter, and it must not be the empty string if the
     * option takes a parameter.
     * @param description Description of this option.
     * @param optionType Type of option added.
     */
    public void addOption(String name, String valueName,
                          String description, Opt.Type optionType) {
        options.put(name, new Option(description, valueName, optionType));
    }

    /**
     * Adds a named parameter with the given name and description.
     *
     * @param name Name used to illustrate the value of the parameter
     * in the usage description. This should be the empty string if
     * the option does not take any parameter.
     * @param description Description of this parameter.
     */
    public void addParameter(String name, String description) {
        parameters.put(name, new Parameter(description));
    }

    /**
     * Adds an additional usage form to this instance.
     */
    public void addUsageForm() {
        usageForms.add(new UsageForm(this));
    }

    /**
     * Adds the given number of additional usage forms to this
     * instance.
     *
     * @param n Number of usage forms to add.
     */
    public void addUsageForms(int n) {
        for (int i = 0; i < n; i++) {
            addUsageForm();
        }
    }

    /**
     * Extends the <code>index</code>th usage form with additional
     * options and parameters.
     *
     * @param index Index of a usage form.
     * @param ro Additional required options.
     * @param oo Additional optional options.
     * @param rp Additional required parameters.
     * @param op Additional optional parameters.
     */
    public void appendToUsageForm(int index, String[] ro, String[] oo,
                                  String[] rp, String[] op) {
        usageForms.get(index).append(ro, oo, rp, op);
    }

    /**
     * Extends the <code>index</code>th usage form with additional
     * options and parameters.
     *
     * @param index Index of a usage form.
     * @param optionsAndParameters Additional options and
     * parameters. The options and parameters string should be given
     * on the following form: <required options>#<optional
     * options>#<required parameters>#<optional parameters>, where
     * each group is a comma-separated list of options and parameters.
     */
    public void appendToUsageForm(int index, String optionsAndParameters) {
        String[] a = Util.split(optionsAndParameters, "#");
        if (a.length != 4) {
            throw new OptError("Incorrect number of option and "
                               + "parameter groups!");
        }
        String[] ro = Util.split(a[0], ",");
        String[] oo = Util.split(a[1], ",");
        String[] rp = Util.split(a[2], ",");
        String[] op = Util.split(a[3], ",");

        appendToUsageForm(index, ro, oo, rp, op);
    }

    /**
     * Returns a usage string for the command for which this instance
     * is used.
     *
     * @return Usage description.
     */
    public String usage() {
        StringBuilder sb = new StringBuilder();

        sb.append("Usage: \n");
        for (UsageForm uf : usageForms) {
            sb.append(uf.toString()).append("\n");
        }
        sb.append(usageComment).append("\n");

        sb.append("\nDescription:\n\n");
        sb.append(Util.breakLines(description, LINE_WIDTH));
        sb.append("\n");

        if (parameters.keySet().size() > 0) {
            sb.append("\nParameters:\n");
            for (String parameter : parameters.keySet()) {
                parameters.get(parameter).write(sb, parameter);
                sb.append("\n");
            }
        }
        if (options.keySet().size() > 0) {
            sb.append("\nOptions:\n");
            for (String option : options.keySet()) {
                options.get(option).write(sb, option);
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    /**
     * Store an option value.
     *
     * @param name Name of option.
     * @param value Value to store.
     */
    public void storeOptionValue(String name, String value) {
        if (!givenOptions.containsKey(name)) {
            givenOptions.put(name, new ArrayList<String>());
        }
        givenOptions.get(name).add(0, value);
    }

    /**
     * Parse the given command line parameters and store the result.
     *
     * @param args Command line parameters.
     * @throws OptException If the parsing fails, in which case the
     * message string in the exception explains why it failed.
     */
    public void parse(String[] args) throws OptException {
        parse(args, 0);
    }

    /**
     * Parse the first command line parameters and store the
     * result. Parse until <code>noParameters</code> have been found.
     *
     * @param args Command line parameters.
     * @param maxNoParameters Number of parameters that decides when to
     * return, i.e., we parse until <code>maxNoParameters</code> are
     * encountered and ignore the remainder of the command line
     * arguments.
     * @return Parsed command line entries.
     * @throws OptException If the parsing fails, in which case the
     * message string in the exception explains why it failed.
     */
    public int parse(String[] args, int maxNoParameters) throws OptException {

        String currentOption = null;

        int noArgsConsidered = 0;

        int noParameters = 0;

        for (String arg : args) {

            if (currentOption != null) {
                if (!arg.startsWith("-")) {
                    storeOptionValue(currentOption, arg);
                    currentOption = null;
                } else {
                    throw new OptException("Expected a parameter for option "
                                           + currentOption + "!");
                }
            } else if (options.containsKey(arg)) {
                Option option = options.get(arg);
                if (option.valueName.equals("")) {
                    storeOptionValue(arg, null);
                } else {
                    currentOption = arg;
                }
            } else if (arg.startsWith("-")) {
                throw new OptException("Unknown option: " + arg + "!");
            } else {
                givenParameters.add(arg);
                noParameters++;
                if (maxNoParameters != 0 && noParameters >= maxNoParameters) {
                    break;
                }
            }
            noArgsConsidered++;
        }

        if (currentOption != null) {
            throw new OptException("Expected parameter for option "
                                   + currentOption + "!");
        }

        validate();

        return noArgsConsidered;
    }

    /**
     * Verifies that there exists a usage form that matches the
     * options and parameters extracted from the command line, and if
     * this is the case store the matching usage form internally.
     *
     * @throws OptException If no matching usage form exists.
     */
    protected void validate() throws OptException {
        if (uf == null) {
            int i = 0;
            for (UsageForm uf : usageForms) {
                if (uf.matches()) {
                    this.uf = uf;
                    return;
                }
                i++;
            }
            throw new OptException(defaultErrorString);
        }

        for (String name : givenOptions.keySet()) {
            Option option = options.get(name);
            if (option.optionType == Opt.Type.INT) {
                try {
                    Integer.parseInt(givenOptions.get(name).get(0));
                } catch (NumberFormatException nfe) {
                    throw new OptException("Option " + name
                                           + " takes an integer parameter!");
                }
            }
        }
        ArrayList<String> tmp = new ArrayList<String>(uf.requiredParams);
        tmp.addAll(uf.optionalParams);

        for (int i = 0; i < givenParameters.size(); i++) {
            String name = tmp.get(i);
            Parameter p = parameters.get(name);
            if (p.paramType == Opt.Type.INT) {
                try {
                    Integer.parseInt(givenParameters.get(i));
                } catch (NumberFormatException nfe) {
                    throw new OptException("Parameter " + name
                                           + " must be an integer!");
                }
            }
        }
    }

    /**
     * Throws an error if this instance is not validated.
     */
    protected void checkValidated() {
        if (uf == null) {
            throw new OptError("Attempting extraction from unvalidated "
                               + "instance!");
        }
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * the given option or named parameter was extracted from the
     * command line.
     *
     * @param name Name of option or parameter.
     * @return <code>true</code> or <code>false</code> depending on if
     * a value named <code>name</code> was given.
     */
    public boolean valueIsGiven(String name) {
        checkValidated();
        int ufi = uf.parameterIndexFromName(name);
        return givenOptions.containsKey(name)
            || (0 <= ufi && ufi < givenParameters.size());
    }

    /**
     * Returns the value of the given option or named parameter, if
     * one exists. If more than one value exists, the last value
     * listed on the command line is returned.
     *
     * @param name Name of option or parameter.
     * @return Value of the given option.
     */
    public String getStringValue(String name) {
        if (!valueIsGiven(name)) {
            throw new OptError("Attempting to access option or parameter ("
                               + name + ") not given!");
        }
        if (givenOptions.containsKey(name)) {
            return givenOptions.get(name).get(0);
        } else {
            int index = uf.parameterIndexFromName(name);
            if (uf.multiParam != null) {
                if (index > uf.multiParamIndex) {
                    index +=
                        givenParameters.size() - uf.requiredParams.size();
                }
            }
            return givenParameters.get(index);
        }
    }

    /**
     * Returns array of all parameters.
     *
     * @return Array of parameters.
     */
    public String[] getMultiParameters() {
        checkValidated();
        if (uf.multiParam == null) {
            throw new OptError("No multiparameter is given!");
        }
        String[] pa = givenParameters.toArray(new String[0]);
        int len = givenParameters.size() - uf.requiredParams.size() + 1;
        return Arrays.copyOfRange(pa,
                                  uf.multiParamIndex,
                                  uf.multiParamIndex + len);
    }

    /**
     * Returns the value of the given option or named parameter, if
     * one exists. If more than one value exists, the last value
     * listed on the command line is returned.
     *
     * @param name Name of option or parameter.
     * @param defaultValue Value returned if not option was given.
     * @return Value of the given option.
     */
    public String getStringValue(String name, String defaultValue) {
        if (valueIsGiven(name)) {
            return getStringValue(name);
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns a list of all values of the given option or named
     * parameter, if one exists. This can be used if the user repeats
     * the same option with different parameters.
     *
     * @param name Name of option or parameter.
     * @return Value of the given option.
     */
    public ArrayList<String> getStringValues(String name) {
        checkValidated();
        if (givenOptions.containsKey(name)) {
            return (ArrayList<String>)givenOptions.get(name);
        } else {
            throw new OptError("Attempting to access option or parameter ("
                               + name + ") not given!");
        }
    }

    /**
     * Returns the value of the given option or named parameter, if
     * one exists. If more than one value exists, the last value
     * listed on the command line is returned.
     *
     * @param name Name of option or parameter.
     * @return Value of the given option.
     */
    public int getIntValue(String name) {
        String value = getStringValue(name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            throw new OptError("Value \"" + value
                               + "\" is not an integer!", nfe);
        }
    }

    /**
     * Returns the value of the given option or named parameter, if
     * one exists. If more than one value exists, the last value
     * listed on the command line is returned.
     *
     * @param name Name of option or parameter.
     * @param defaultValue Value returned if not option was given.
     * @return Value of the given option.
     */
    public int getIntValue(String name, int defaultValue) {
        if (valueIsGiven(name)) {
            return getIntValue(name);
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * the given option was given on the command line or not. This is
     * used for options that do not take parameters. Note that this
     * can not be called with a named parameter.
     *
     * @param name Name of option or parameter.
     * @return <code>true</code> or <code>false</code> depending on if
     * the given option was given on the command line.
     */
    public boolean getBooleanValue(String name) {
        checkValidated();
        return givenOptions.containsKey(name);
    }

    /**
     * Returns the index of the usage form that matches the options
     * and parameters extracted from the command line.
     *
     * @return Index of matching usage form.
     */
    public int getUsageFormIndex() {
        checkValidated();
        return usageForms.indexOf(uf);
    }
}

/**
 * Container class of an option.
 *
 * @author Douglas Wikstrom
 */
class Option {
    String description;
    String valueName;
    Opt.Type optionType;

    Option(String description, String valueName) {
        this.description = description;
        this.valueName = valueName;
        this.optionType = Opt.Type.STRING;
    }

    Option(String description, String valueName, Opt.Type optionType) {
        this.description = description;
        this.valueName = valueName;
        this.optionType = optionType;
    }

    void write(StringBuilder sb, String name) {
        Formatter f = new Formatter(sb, Locale.US);
        String s = "";
        if (!valueName.equals("")) {
            s += "<" + valueName + ">";
        }
        if (description.equals("")) {
            f.format("%10s %-8s", name, s);
        } else {
            String broken = Util.breakLines(description, 59);
            String[] lines = Util.split(broken, "\n");
            f.format("%10s %-8s - %s", name, s, lines[0]);
            for (int i = 1; i < lines.length; i++) {
                f.format("\n%10s %-8s   %s", "", "", lines[i]);
            }
        }
    }
}

/**
 * Container class of a parameter.
 *
 * @author Douglas Wikstrom
 */
class Parameter {
    String description;
    Opt.Type paramType;

    Parameter(String description) {
        this.description = description;
    }

    Parameter(String description, Opt.Type paramType) {
        this.description = description;
        this.paramType = paramType;
    }

    void write(StringBuilder sb, String name) {
        Formatter f = new Formatter(sb, Locale.US);

        String broken = Util.breakLines(description, 59);
        String[] lines = Util.split(broken, "\n");
        f.format("%2s %-14s - %s", "", "<" + name + ">", lines[0]);
        for (int i = 1; i < lines.length; i++) {
            f.format("\n%2s %-14s   %s", "", "", lines[i]);
        }
    }
}
