
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
 * Links a <code>PipedOutputStream</code> to write to a
 * <code>JTextArea</code>, i.e., it allows us to write to a text area
 * by means of a stream.
 *
 * <b>WARNING! Read the documentation of {@link JTextualUI} before use.</b>
 *
 * @author Douglas Wikstrom
 */
public class JConsoleOutLinker implements Runnable {

    /**
     * Internal buffer.
     */
    protected static int BUFFER_SIZE = 1024;

    /**
     * A pipe to allow writing to the text area.
     */
    protected PipedInputStream pin;

    /**
     * Text area to which we write.
     */
    protected JTextArea jTextArea;

    /**
     * A thread that keeps writing from the stream onto the text
     * area.
     */
    protected Thread thisThread;

    /**
     * Creates an instance.
     *
     * @param pout Source of inputs
     * @param jTextArea Where the inputs are written.
     */
    public JConsoleOutLinker(PipedOutputStream pout, JTextArea jTextArea) {
        try {
            this.pin = new PipedInputStream(pout);
        } catch (IOException ioe) {
            throw new UIError("Unable to create input stream!");
        }
        this.jTextArea = jTextArea;

         thisThread = new Thread(this);
         thisThread.setDaemon(true);
         thisThread.start();
    }

    // Documented in super class or interface.

    public synchronized void run() {

        byte[] buffer = new byte[BUFFER_SIZE];

        for (;;) {
            try {
                this.wait(100);
            } catch (InterruptedException ie) {}

            int len = 0;
            try {
                int noBytes = pin.available();

                if (noBytes > 0) {
                    len = pin.read(buffer, 0, Math.min(noBytes, BUFFER_SIZE));
                    if (len > 0) {
                        jTextArea.append(new String(buffer, 0, len));
                        jTextArea.
                            setCaretPosition(jTextArea.getText().length());
                    }
                }
            } catch (IOException ioe) {
                throw new UIError("Unable to read from input stream! "
                                  + ioe.getMessage());
            }

        }
    }
}
