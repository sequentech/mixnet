
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
import javax.swing.*;
import java.awt.*;

import vfork.ui.*;
import vfork.ui.tui.*;

/**
 * Implements a simple graphical user interface for cryptographic
 * protocols that essentially encapsulates the textual interface
 * implemented in {@link vfork.ui.tui.TextualUI}.
 *
 * <p>
 *
 * <b>WARNING! Currently, this class is only meant to be used for
 * debugging purposes and instantiated by {@link
 * vfork.protocol.demo.Demo}. The class {@link JConsole} and the
 * associated classes {@link JConsoleInLinker} and {@link
 * JConsoleOutLinker} do not really implement a console, but only a
 * something simpler and the implemented features are not implemented
 * faithfully.</b>
 *
 * @author Douglas Wikstrom
 */
public class JTextualUI extends JInternalFrame implements UI {

    /**
     * Default number of rows in interaction text area.
     */
    final static int DEFAULT_ROWS_INTERACTION = 20;

    /**
     * Default number of columns in interaction text area.
     */
    final static int DEFAULT_COLS_INTERACTION = 60;

    /**
     * Default number of rows in log text area.
     */
    final static int DEFAULT_ROWS_LOG = 20;

    /**
     * Default number of columns in log text area.
     */
    final static int DEFAULT_COLS_LOG = 80;

    /**
     * The description string used to refer to the parties of
     * the executed protocol. By default this is "Party".
     */
    private String descriptionString;

    /**
     * Index of the party this interface should represent.
     */
    private int j;

    /**
     * The internal textual interface.
     */
    private TextualUI textualUI;

    /**
     * The console used by this user interface.
     */
    private JConsole jc;

    /**
     * Linker to allow logging to appear on a separate
     * <code>JTextArea</code>.
     */
    private JConsoleOutLinker linker;

    /**
     * Creates a user interface for party number <code>i</code>.
     *
     * @param j Index of the party this interface should represent.
     */
    public JTextualUI(int j) {

        // Set title and ensure resizability
        super("Party" + j, true);
        this.j = j;

        // Set up the logical part
        FixedJTextArea interactionArea =
            new FixedJTextArea(DEFAULT_ROWS_INTERACTION,
                               DEFAULT_COLS_INTERACTION);
        interactionArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        jc = new JConsole(interactionArea);

        JTextArea logArea = new JTextArea(DEFAULT_ROWS_LOG, DEFAULT_COLS_LOG);

        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setEditable(false);
        PipedOutputStream pout = new PipedOutputStream();
        linker = new JConsoleOutLinker(pout, logArea);
        PrintStream logStream = new PrintStream(pout);

        textualUI = new TextualUI(jc);
        textualUI.getLog().addLogStream(pout);

        // Set up the graphical part
        JSplitPane jSplitPane = new JSplitPane();
        jSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

        JScrollPane interactionJSP = new JScrollPane(interactionArea,
                          ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel ip = new JPanel(new BorderLayout());
        ip.add(new Label("Interaction Area"), BorderLayout.NORTH);
        ip.add(interactionJSP, BorderLayout.CENTER);
        jSplitPane.setLeftComponent(ip);

        JScrollPane logJSP = new JScrollPane(logArea,
                          ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JPanel lp = new JPanel(new BorderLayout());
        lp.add(new Label("Log file"), BorderLayout.NORTH);
        lp.add(logJSP, BorderLayout.CENTER);
        jSplitPane.setRightComponent(lp);

        setContentPane(jSplitPane);

    }

    /**
     * Sets the description string of this interface.
     *
     * @param descriptionString Description of this interface.
     */
    public void setDescriptionString(String descriptionString) {
        this.descriptionString = descriptionString;
        setTitle(descriptionString);
        textualUI.setDescriptionString(descriptionString);
    }

    // The methods below are documented in an implemented interface.

    public String getDescrString() {
        return textualUI.getDescrString();
    }
    public String getDescrString(int i) {
        return textualUI.getDescrString(i);
    }

    public String stringQuery(String msgString) {
        return textualUI.stringQuery(msgString);
    }

    public boolean dialogQuery(String msgString) {
        return textualUI.dialogQuery(msgString);
    }

    public int intQuery(String msgString) {
        return textualUI.intQuery(msgString);
    }

    public int alternativeQuery(String[] alternatives, String descString) {
        return textualUI.alternativeQuery(alternatives, descString);
    }

    public Log getLog() {
        return textualUI.getLog();
    }

    public void print(String str) {
        textualUI.print(str);
    }
}
