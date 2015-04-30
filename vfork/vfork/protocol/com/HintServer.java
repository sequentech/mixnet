
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

package vfork.protocol.com;

import java.io.*;
import java.net.*;
import java.util.*;

import vfork.protocol.*;

/**
 * Waits for a "hint" from any of the other servers in the form of a
 * tiny UDP-package. When it is received, this server signals an
 * interrupt on its listener thread if it has one, and then
 * unregisters the listener thread.
 *
 * @author Douglas Wikstrom
 */
public class HintServer implements Runnable {

    /**
     * Socket timeout.
     */
    protected final int SOCKET_TIMEOUT = 2000;

    /**
     * Socket for incoming hint packages.
     */
    protected DatagramSocket socket = null;

    /**
     * Listener thread that should be interrupted when a hint is
     * received.
     */
    protected Thread[] listeners;

    /**
     * Flag indicating if this instance is running or not.
     */
    protected boolean running;

    /**
     * Indicates if a hint was received already.
     */
    protected boolean[] hintReceived;

    /**
     * Index of this party.
     */
    protected int j;

    /**
     * Number of parties.
     */
    protected int k;

    /**
     * A server listening at the given socket address.
     *
     * @param isa Socket address where this server listens when
     * started.
     * @param j Index of party invoking this server.
     * @param k Number of parties.
     */
    public HintServer(InetSocketAddress isa, int j, int k) {
        try {

            this.j = j;
            this.k = k;
            socket = new DatagramSocket(isa);

            listeners = new Thread[k + 1];
            Arrays.fill(listeners, null);

            hintReceived = new boolean[k + 1];
            Arrays.fill(hintReceived, false);

            running = false;

        } catch (SocketException se) {
            throw new ProtocolError("Invalid socket address! (" + isa + ")",
                                    se);
        }
    }

    /**
     * Sends a hint to the remote server at the given socket address.
     *
     * @param j Index of hinting party.
     * @param isa Socket address of remote hint server.
     */
    public static void hint(int j, InetSocketAddress isa) {

        DatagramSocket socket = null;
        try {

            socket = new DatagramSocket();

            byte[] buf = new byte[1];
            buf[0] = (byte)j;
            DatagramPacket packet = new DatagramPacket(buf, buf.length, isa);
            socket.send(packet);

        } catch (IOException ioe) {
            // Hints are optimistic, so failures are ignored.
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    /**
     * Registers a thread as the listener of interrupts
     * from this hint server.
     *
     * @param l Party index of listener.
     * @param listener Listener of interrupts.
     */
    public synchronized void setListener(int l, Thread listener) {
        this.listeners[l] = listener;
    }

    /**
     * Checks if the given listener should be interrupted. If so, then
     * it interrupts the listener and unregisters it.
     *
     * @param l Party index of listener.
     */
    public synchronized void checkHint(int l) {
        if (0 < l && l <= k && hintReceived[l] && listeners[l] != null) {
            listeners[l].interrupt();
            listeners[l] = null;
            hintReceived[l] = false;
        }
    }

    /**
     * Start this hint server.
     */
    public void start() {
        if (!running) {
            running = true;
            (new Thread(this)).start();
        }
    }

    /**
     * Stop this hint server.
     */
    public void stop() {
        running = false;
    }


    // Documented in superclass Thread.java.

    public void run() {

        byte[] buf = new byte[1];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while (running) {
            try {

                socket.setSoTimeout(SOCKET_TIMEOUT);
                socket.receive(packet);
                int l = buf[0];
                if (0 < l && l <= k) {
                    hintReceived[l] = true;
                    checkHint(l);
                }

            } catch (IOException ioe) {
                // Hints are optimistic, so failures are ignored.
            }
        }
    }
}
