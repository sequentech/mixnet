
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

package verificatum.test;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import verificatum.arithm.*;

/**
 * Simple class for unit testing. Each set of tests is represented by
 * a class and each individual test is represented by a public static
 * method with <code>boolean</code> return type. Each test should
 * simply return true or false depending on the test passes or not. If
 * a test throws an exception, then a stack trace of the throwable is
 * printed.
 *
 * @author Douglas Wikstrom
 */
public class Test {

    /**
     * Stream where the result of the test is written.
     */
    protected static PrintStream ps;

    /**
     * This method simply runs all the tests, i.e., all public static
     * methods of each class which is listed on the command line.
     *
     * @param args Names of classes containing tests.
     */
    public static void main(String[] args) {

        TestParameters tp = new TestParameters("", 300, new File(args[0]));

        int succ = 0;

        ps = System.out;
        ps.println("Executing Test Sequence");
        ps.println("========================================================");
        for (String arg : Arrays.copyOfRange(args, 1, args.length)) {
            try {
                int result = runTests(Class.forName(arg), tp);
                if (result < 0) {
                    ps.println("Aborting test sequence...");
                    System.exit(1);
                } else {
                    succ += result;
                }
            } catch (ClassNotFoundException cnfe) {
                System.err.println(stackTraceToString(cnfe));
                System.exit(1);
            }
        }
        ps.println();
        ps.println("Executed " + succ + " tests successfully.");
        ps.println("========================================================");
        System.exit(0);
    }

    /**
     * Returns a string representation of the stack associated with
     * the given throwable.
     *
     * @param t Throwable with associated stack trace.
     * @return String representation of the stack contained in the
     * input.
     */
    public static String stackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    /**
     * Prints error information.
     *
     * @param testClass Name of tested class.
     * @param t Exception or error that is thrown.
     */
    public static void errorStop(Class testClass, Throwable t) {
        ps.println("FAILED!");
        ps.println("ERROR! Aborting tests in: " + testClass.getName());
        if (t != null) {
            t.printStackTrace(ps);
        }
    }

    /**
     * Excercises the tests in the given class using the given test
     * parameters.
     *
     * @param testClass Class representing a set of tests.
     * @param tp Global test parameters.
     * @return Number of successful tests if all tests pass, or the
     * negative of the number of the first failed test.
     */
    public static int runTests(Class testClass, TestParameters tp) {

        ps.println("--------------------------------------------------------");
        ps.println("Executing tests in: " + testClass.getName());

        int succ = 1;
        Method[] methods = testClass.getDeclaredMethods();
        Object[] paramContainer = new Object[1];
        paramContainer[0] = tp;
        try {
            for (Method method : methods) {

                int mod = method.getModifiers();

                // We consider public static methods, possibly taking
                // a TestParameters-instance as a single parameter.
                if (Modifier.isPublic(mod)
                    && Modifier.isStatic(mod)
                    && method.getReturnType().toString().equals("boolean")) {

                    ps.print("Test: " + method.getName() + "...");
                    int nops = method.getParameterTypes().length;

                    if (nops <= 2) {
                        Boolean result =
                            (Boolean)method.invoke(null, paramContainer);

                        if (result.booleanValue() == false) {
                            errorStop(testClass, null);
                            return -succ;
                        }
                    } else {
                        throw new Exception("Test method takes more than one "
                                            + "parameter!");
                    }
                    ps.println("done.");
                    succ++;
                }
            }
        } catch (Exception e) {
            errorStop(testClass, e);
            return -succ;
        }

        ps.println("--------------------------------------------------------");

        return succ - 1;
    }
}
