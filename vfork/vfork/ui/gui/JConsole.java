
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

package vfork.ui.gui;

import java.io.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;

import vfork.ui.*;

/**
 * Implements a simple console which connects standard input, output
 * and error to a {@link JTextArea}. One would expect that such
 * functionality existed in the standard Java API, but it seems this
 * is not the case.
 *
 * <b>WARNING! Read the documentation of {@link JTextualUI} before use.</b>
 *
 * @author Douglas Wikstrom
 */
public class JConsole extends vfork.ui.Console {

    /**
     * Connects a <code>JTextArea</code> to an input pipe stream.
     */
    private JConsoleInLinker inLinker;

    /**
     * Connects a <code>JTextArea</code> to an output pipe stream.
     */
    private JConsoleOutLinker outLinker;

    /**
     * Connects a <code>JTextArea</code> to an error pipe stream.
     */
    private JConsoleOutLinker errLinker;

    /**
     * Creates a console and connects it to the given <code>JTextArea</code>.
     *
     * @param jTextArea <code>JTextArea</code> to which this instance
     * is connected.
     */
    public JConsole(FixedJTextArea jTextArea) {

        // Make sure user can not enter anything directly on the
        // JTextArea.
        jTextArea.setEditable(false);

        // Create pipes.
        PipedInputStream pin = new PipedInputStream();
        PipedOutputStream pout = new PipedOutputStream();
        PipedOutputStream perr = new PipedOutputStream();

        // Link them to the JTextArea.
        inLinker = new JConsoleInLinker(jTextArea, pin);
        outLinker = new JConsoleOutLinker(pout, jTextArea);
        errLinker = new JConsoleOutLinker(perr, jTextArea);

        // Connect the pipes to workable streams.
        in = pin;
        out = new PrintStream(pout);
        err = new PrintStream(perr);

    }
}
