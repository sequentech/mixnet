
/*
 * Copyright 2008 2009 2010 Douglas Wikstrom
 *
 * This file is part of Vfork.
 *
 * Vfork is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Vfork is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Vfork.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package vfork.ui.opt;

/**
 * Test Opt.java. This is not executed by the unit testing framework.
 *
 * @author Douglas Wikstrom
 */
public class TestOpt {

    public static void main(String[] args) {

        Opt opt = new Opt("command", "My default error!");
        try {
            opt.addOption("-a", "aval", "Where a goes.");
            opt.addOption("-b", "bval", "Where b goes.");
            opt.addOption("-c", "cval", "Where c goes.");
            opt.addOption("-v", "", "Verbose output.");

            opt.addParameter("input", "Source of input");
            opt.addParameter("output", "Destination of output");
            opt.addParameter("log", "Logfile");

            String[] ro0 = {"-a"};
            String[] oo0 = {"-v"};
            String[] rp0 = {"input"};
            String[] op0 = {"output"};
            opt.addUsageForms(1);
            opt.appendToUsageForm(0, ro0, oo0, rp0, op0);

            String[] ro1 = {"-b"};
            String[] oo1 = {"-c"};
            String[] rp1 = {"input"};
            String[] op1 = {};
            opt.addUsageForms(1);
            opt.appendToUsageForm(1, ro1, oo1, rp1, op1);

            String d = "This command is the most important command in the " +
                "history of mankind. Perhaps this will give the Nobel prize " +
                "one day, and I would really appreciate that.";
            opt.appendDescription(d);

            opt.parse(args);

            if (opt.getUsageFormIndex() == 0) {
                System.out.println("-a " + opt.getStringValue("-a"));
                System.out.println("-v " + opt.getBooleanValue("-v"));
                System.out.println("input = " + opt.getStringValue("input"));
                if (opt.getBooleanValue("output")) {
                    System.out.println("output = " +
                                       opt.getStringValue("output"));
                }
            } else {
                System.out.println("-b " + opt.getStringValue("-b"));
                System.out.println("-c " + opt.getBooleanValue("-c"));
                System.out.println("input = " + opt.getStringValue("input"));
            }

        } catch (OptException oe) {
            System.out.println(oe.getMessage() + "\n");
            System.out.println(opt.usage());
        }
    }
}
