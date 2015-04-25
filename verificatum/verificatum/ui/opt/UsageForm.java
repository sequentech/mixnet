
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

/**
 * Stores a representation of a family of possible ways to invoke a
 * command. This is a helper class for {@link Opt}.
 *
 * @author Douglas Wikstrom
 */
class UsageForm {

    /**
     * Associated Opt instance.
     */
    Opt opt;

    /**
     * Required options for this usage form.
     */
    TreeSet<String> requiredOptions;

    /**
     * Required option names in a given order.
     */
    ArrayList<String> requiredOptionNames;

    /**
     * Optional options for this usage form.
     */
    TreeSet<String> optionalOptions;

    /**
     * Required parameters for this usage form.
     */
    ArrayList<String> requiredParams;

    /**
     * Optional parameters for this usage form.
     */
    ArrayList<String> optionalParams;

    /**
     * Name of multiparameter if this is an admitted usage form.
     */
    String multiParam;

    /**
     * Number of required parameters.
     */
    int multiParamIndex;

    /**
     * Creates an empty usage form associated with the given
     * <code>Opt</code> instance.
     *
     * @param opt Instance with which this instance is associated.
     */
    UsageForm(Opt opt) {
        this.opt = opt;
        requiredOptions = new TreeSet<String>();
        requiredOptionNames = new ArrayList<String>();
        optionalOptions = new TreeSet<String>();
        requiredParams = new ArrayList<String>();
        optionalParams = new ArrayList<String>();
        multiParam = null;
    }

    /**
     * Returns the index of the parameter with the given name, where
     * the parameters are numbered starting from the required
     * parameters and continuing with the optional parameters.
     *
     * @param name Name of a parameter.
     * @return Index of the given parameter, or -1 if no parameter
     * with the given name exists.
     */
    int parameterIndexFromName(String name) {
        int index = requiredParams.indexOf(name);
        if (index >= 0) {
            return index;
        } else {
            index = optionalParams.indexOf(name);
            if (index >= 0) {
                return requiredParams.size() + index;
            }
        }
        return -1;
    }

    /**
     * Appends the given options and parameters to this
     * <code>UsageForm</code>. The options and parameters must not
     * duplicate existing options.
     *
     * @param ro Additional required options.
     * @param oo Additional optional options.
     * @param rp Additional required parameters.
     * @param op Additional optional parameters.
     */
    void append(String[] ro, String[] oo, String[] rp, String[] op) {

        Set<String> ops = opt.options.keySet();
        carefulAddAll(requiredOptions, ro, ops);
        for (String rop : ro) {
            if (!requiredOptionNames.contains(rop)) {
                requiredOptionNames.add(rop);
            }
        }
        carefulAddAll(optionalOptions, oo, ops);

        Collection<String> c = new HashSet<String>(requiredOptions);
        c.addAll(optionalOptions);
        if (c.size() < requiredOptions.size() + optionalOptions.size()) {
            throw new OptError("An option is both required and optional!");
        }

        Set<String> pars = opt.parameters.keySet();

        carefulAddAll(requiredParams, rp, pars);
        carefulAddAll(optionalParams, op, pars);

        if (multiParam != null) {
            if (optionalParams.size() > 0) {
                throw new OptError("Can not have both multiparameter " +
                                   "and optional parameters!");
            }
        }
    }

    /**
     * Adds all strings in the given array to the collection of
     * strings.
     *
     * @param ts Set to which the strings are added.
     * @param sa Strings to be added.
     * @param possibleNames Set of all valid names.
     */
    void carefulAddAll(AbstractCollection<String> ts, String[] sa,
                       Set<String> possibleNames) {
        for (String s : sa) {
            if (s.startsWith("+")) {
                s = s.substring(1);
                if (multiParam != null) {
                    throw new OptError("Can not add another multi-parameter!");
                }
                multiParam = s;
                multiParamIndex = ts.size();
            }
            if (!possibleNames.contains(s)) {
                throw new OptError("Can not add " + s + " to usage form."
                               + " It does not exist as option or parameter!");
            }

            // Ignore doubles.
            if (!s.equals("") && !ts.contains(s)) {
                ts.add(s);
            }
        }
    }

    /**
     * Formats the options and writes the result on the given
     * <code>StringBuilder</code>.
     *
     * @param sb Destination of formatted output.
     * @param ordered Ordered names of options.
     * @param ts Source of options.
     * @param optional Determines if the options should be formatted
     * as optional or not.
     */
    void writeOptions(StringBuilder sb, Collection<String> ordered,
                      TreeSet<String> ts, boolean optional) {

        for (String name : ordered) {
            sb.append(" ");
            if (optional) {
                sb.append("[");
            }
            sb.append(name);
            Option option = opt.options.get(name);
            if (!option.valueName.equals("")) {
                sb.append(" <" + option.valueName + ">");
            }
            if (optional) {
                sb.append("]");
            }
        }
    }

    /**
     * Formats the parameters and writes the result on the given
     * <code>StringBuilder</code>.
     *
     * @param sb Destination of formatted output.
     * @param ts Source of parameters.
     * @param optional Determines if the parameters should be
     * formatted as optional or not.
     */
    void writeParams(StringBuilder sb,
                     AbstractCollection<String> ts,
                     boolean optional) {
        for (String param : ts) {
            sb.append(" ");
            if (optional) {
                sb.append("[");
            }
            sb.append("<" + param + ">");
            if (param.equals(multiParam)) {
                sb.append(" ...");
            }
            if (optional) {
                sb.append("]");
            }
        }
    }

    /**
     * Returns a formatted description string of the usage form
     * represented by this instance.
     *
     * @return Representation of this usage form.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(opt.commandName);
        writeOptions(sb, requiredOptionNames, requiredOptions, false);
        writeOptions(sb, optionalOptions, optionalOptions, true);
        writeParams(sb, requiredParams, false);
        writeParams(sb, optionalParams, true);
        return sb.toString();
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * this instance matches/does not match, the information parsed by
     * the <code>Opt</code> instance associated with this instance.
     *
     * @return <code>true</code> or <code>false</code> depending on if
     * this usage form matches the values given in the associated
     * option instance.
     */
    boolean matches() {
        for (String name : requiredOptions) {
            if (!opt.givenOptions.containsKey(name)) {
                return false;
            }
        }
        for (String name : opt.givenOptions.keySet()) {
            if (!requiredOptions.contains(name)
                && !optionalOptions.contains(name)) {
                return false;
            }
        }
        if (requiredParams.size() > opt.givenParameters.size()) {
            return false;
        }
        if (multiParam != null) {
            return true;
        }
        if (opt.givenParameters.size()
            > requiredParams.size() + optionalParams.size()) {
            return false;
        }
        return true;
    }
}
