
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

package mixnet.ui.gui;

import java.io.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.text.*;

import mixnet.ui.*;

/**
 * Links a <code>PipedInputStream</code> to read from a
 * <code>JTextArea</code>, i.e., anything typed into the text area is
 * written to the input stream.
 *
 * <b>WARNING! Read the documentation of {@link JTextualUI} before use.</b>
 *
 * @author Douglas Wikstrom
 */
public class JConsoleInLinker extends NavigationFilter
    implements KeyListener, SelectionListener {

    /**
     * Writer to the stream.
     */
    protected OutputStreamWriter outWriter;

    /**
     * Source of inputs.
     */
    protected JTextArea jTextArea;

    /**
     * Internal buffer.
     */
    protected StringBuilder buffer;

    /**
     * Creates an instance based on the given text area and stream.
     *
     * @param jTextArea Source of input.
     * @param pin Where inputs are written.
     */
    public JConsoleInLinker(FixedJTextArea jTextArea, PipedInputStream pin) {

        try {
            outWriter = new OutputStreamWriter(new PipedOutputStream(pin));
        } catch (IOException ioe) {
            throw new UIError("Unable to create output stream!");
        }
        this.jTextArea = jTextArea;

        buffer = new StringBuilder();
        jTextArea.setEditable(true);
        jTextArea.setNavigationFilter(this);
        jTextArea.addKeyListener(this);
        jTextArea.addSelectionListener(this);
    }

    // The following methods are documented in the super class or in
    // an interface.

    public void setDot(NavigationFilter.FilterBypass fb,
                       int dot,
                       Position.Bias bias) {
        dot = Math.max(dot, jTextArea.getText().length() - buffer.length());
        super.setDot(fb, dot, bias);
    }

    public void moveDot(NavigationFilter.FilterBypass fb,
                        int dot,
                        Position.Bias bias) {
        dot = Math.max(dot, jTextArea.getText().length() - buffer.length());
            super.moveDot(fb, dot, bias);
    }

    public void keyPressed(KeyEvent e) {
        int dot = jTextArea.getCaret().getDot();

        if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            e.consume();
        }
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (dot <= (jTextArea.getText().length() - buffer.length())) {
                e.consume();
            } else {
                if (buffer.length() > 0) {
                    buffer.deleteCharAt(dot - jTextArea.getText().length()
                                        + buffer.length() - 1);
                }
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        int dot = jTextArea.getCaret().getDot();

        if (dot <= (jTextArea.getText().length() - buffer.length())
            && e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            e.consume();
        }
    }


    public void keyTyped(KeyEvent e) {

         char theChar = e.getKeyChar();
        int dot = jTextArea.getCaret().getDot();

        if (theChar == '\n') {
            buffer.insert(buffer.length()
                          - (jTextArea.getText().length() - dot),
                          '\n');
             String bufferString = buffer.toString();
            int endIndex = bufferString.indexOf('\n');
            bufferString = bufferString.substring(0, endIndex + 1);

            try {
                outWriter.write(bufferString, 0, bufferString.length());
                outWriter.flush();
                buffer = new StringBuilder();
            } catch (IOException ioe) {
                // Any exception conditions are caught on the reader side
            }
         }
    }

    public void replaceSelection(String content) {
        int jTextLen = jTextArea.getText().length();
        int bufLen = buffer.length();
        int start = jTextArea.getSelectionStart() - jTextLen + bufLen;
        int end = jTextArea.getSelectionEnd() - jTextLen + bufLen;

        buffer.replace(start, end, content);
    }
}
