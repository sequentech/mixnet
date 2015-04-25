
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

package verificatum.ui.tui;

import java.io.*;
import java.util.logging.*;

import verificatum.ui.*;

/**
 * Implements a simple textual interface for cryptographic protocols.
 *
 * @author Douglas Wikstrom
 */
public class TextualUI implements UI {

    /**
     * Description string used to refer to the parties of the executed
     * protocol.
     */
    private String descriptionString;

    /**
     * Console used by this user interface.
     */
    protected verificatum.ui.Console jc;

    /**
     * Wrapper of the input stream in the console.
     */
    protected BufferedReader bf;

    /**
     * Logging context of this user interface.
     */
    protected Log log;

    /**
     * Creates a textual interface using the given console and logging
     * stream.
     *
     * @param jc Console used by this interface.
     */
    public TextualUI(verificatum.ui.Console jc) {
        this.jc = jc;

        bf = new BufferedReader(new InputStreamReader(jc.in));

        this.log = new Log();

        this.descriptionString = "Party";
    }

    /**
     * Sets the generic description string of a party.
     *
     * @param descriptionString Description string of a party.
     */
    public void setDescriptionString(String descriptionString) {
        this.descriptionString = descriptionString;
    }

    // The methods below are documented in UI.java

    public String getDescrString(int i) {
        return descriptionString + i;
    }

    public String getDescrString() {
        return descriptionString;
    }

    public String stringQuery(String msgString) {

        jc.out.print(msgString);
        try {
            return bf.readLine();
        } catch (IOException ioe) {
            throw new UIError("Input/Output error!");
        }
    }

    public boolean dialogQuery(String msgString) {
        for (;;) {
            String ans = stringQuery(msgString + " (yes/no) ");
            if (ans.toLowerCase().equals("yes")) {
                return true;
            } else if (ans.toLowerCase().equals("no")) {
                return false;
            }
        }
    }

    public int intQuery(String msgString) {
        for (;;) {
            String answerString = stringQuery(msgString);
            try {
                return Integer.parseInt(answerString);
            } catch (NumberFormatException nfe) {
                jc.out.println("The string: " + answerString
                                          + " is not an integer!");
            }
        }
    }

    public int alternativeQuery(String[] alternatives, String descString) {
        StringBuffer sb = new StringBuffer();

        for (;;) {
            sb.append("\nAlternatives:\n");
            for (int i = 0; i < alternatives.length; i++) {
                sb.append(i + ") ").append(alternatives[i]).append('\n');
            }
            sb.append('\n').append("Choose entry: ");

            int theAnswer = intQuery(descString + "\n" + sb.toString());
            if (0 <= theAnswer && theAnswer < alternatives.length) {
                return theAnswer;
            } else {
                jc.out.println("The integer " + theAnswer
                               + " is not a valid alternative!");
            }
        }
    }

    public Log getLog() {
        return log;
    }

    public void print(String str) {
        jc.out.print(str);
    }
}
