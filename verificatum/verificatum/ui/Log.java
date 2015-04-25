
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

package verificatum.ui;

import java.io.*;
import java.text.*;
import java.util.*;

import verificatum.protocol.*;
import verificatum.util.*;

/**
 * Simple hierarchical logging class. This allows adding output
 * streams.
 *
 * @author Douglas Wikstrom
 */
public class Log {

    /**
     * List of streams to which we should log events.
     */
    protected ArrayList<PrintStream> pout;

    /**
     * Name of this log.
     */
    protected String name;

    /**
     * Indent string.
     */
    protected String depthString;

    /**
     * Format for outputting dates.
     */
    protected SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HH:mm:ss");

    /**
     * Creates an empty log.
     */
    public Log() {
        this.name = "";
        this.depthString = "";
        this.pout = new ArrayList<PrintStream>();
    }

    /**
     * Creates a new log.
     *
     * @param name Name of log.
     * @param depthString Indent string.
     * @param pout List of output streams.
     */
    protected Log(String name, String depthString,
                  ArrayList<PrintStream> pout) {
        this.pout = pout;
        this.name = name;
        this.depthString = depthString;
    }

    /**
     * Add an additional output stream.
     *
     * @param ps Additional output stream.
     */
    public synchronized void addLogStream(OutputStream ps) {
        pout.add(new PrintStream(ps));
    }

    /**
     * Creates a child log with a given name.
     *
     * @param postfix Postfix added to name of child log.
     * @return Child log.
     */
    public synchronized Log newChildLog(String postfix) {
        String childName = this.name + "." + postfix;
        return new Log(childName, this.depthString + "| ", this.pout);
    }

    /**
     * Creates an anonymous child log.
     *
     * @return Child log.
     */
    public synchronized Log newChildLog() {
        return newChildLog("#");
    }

    /**
     * Prints an info entry to the logs.
     *
     * @param message Message for user.
     */
    public synchronized void info(String message) {
        StringBuffer sb = new StringBuffer();

        Date date = new Date();
        FieldPosition fp = new FieldPosition(DateFormat.Field.DAY_OF_MONTH);
        sdf.format(date, sb, fp);

        sb.append(" ");
        sb.append(depthString);
        sb.append(message);

        plainInfo(sb.toString());
    }

    /**
     * Prints a message to the logs as it is given.
     *
     * @param message Message for user.
     */
    public synchronized void plainInfo(String message) {
        for (PrintStream ps : pout) {
            ps.println(message);
            ps.flush();
        }
    }

    /**
     * Registers a throwable (exceptions and errors) in the log.
     *
     * @param throwable Event to log.
     */
    public synchronized void register(Throwable throwable) {
        for (PrintStream ps : pout) {
            throwable.printStackTrace(ps);
            ps.flush();
        }
    }
}
