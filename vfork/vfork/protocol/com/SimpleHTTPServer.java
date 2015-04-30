
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

import com.sun.net.httpserver.*;

import vfork.protocol.*;

/**
 * A simple server with only minimal understanding of the HTTP
 * protocol. It can only process requests where the file resides in
 * the root directory of the server, and where the filename consists
 * exclusively of the letters A-Z, a-z, 0-9, and the special letters
 * "_" and ".", and contains no sequence of more than one
 * ".". Furthermore, all files are considered to have the content type
 * "binary/octet-stream", regardless of extensions.
 *
 * @author Douglas Wikstrom
 */
public final class SimpleHTTPServer {

    /**
     * Address at which this server can be accessed.
     */
    protected InetSocketAddress socketAddress;

    /**
     * Root directory containing files that may be requested by
     * clients.
     */
    protected File directory;

    /**
     * Maximal number of concurrent clients.
     */
    protected int backLog;

    /**
     * Underlying HTTP server.
     */
    protected HttpServer server;

    /**
     * Creates a server.
     *
     * @param directory Root directory containing files that may be
     * requested by clients.
     * @param hostname Hostname of this server.
     * @param port Port at which this server listens.
     * @param backLog Maximal number of concurrent clients.
     */
    public SimpleHTTPServer(File directory, String hostname, int port,
                            int backLog) {
        this(directory, new InetSocketAddress(hostname, port), backLog);
    }

    /**
     * Creates a server.
     *
     * @param directory Directory containing files that may be
     * requested by clients.
     * @param url URL of this server.
     * @param backLog Maximal number of concurrent clients.
     */
    public SimpleHTTPServer(File directory, URL url, int backLog) {
        this(directory,
             new InetSocketAddress(url.getHost(), url.getPort()),
             backLog);
    }

    /**
     * Creates a server.
     *
     * @param directory Directory containing files that may be
     * requested by clients.
     * @param socketAddress Socket address of this server.
     * @param backLog Maximal number of concurrent clients.
     */
    public SimpleHTTPServer(File directory, InetSocketAddress socketAddress,
                            int backLog) {
        this.directory = directory;
        this.socketAddress = socketAddress;
        this.backLog = backLog;
    }

    /**
     * Starts this server.
     */
    public void start() {
        try {
            server = HttpServer.create(socketAddress, backLog);
            server.createContext("/", new SimpleHTTPHandler(directory));
            server.start();
        } catch (IOException ioe) {
            throw new ProtocolError("Unable to start server!", ioe);
        }
    }

    /**
     * Stops this server.
     */
    public void stop() {
        server.stop(0);
        server = null;
    }

    /**
     * Allows executing this server as a standalone application. No
     * verification of command line arguments are performed.
     *
     * <p>
     *
     * <b>WARNING!</b> This is only meant to be used for debugging.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        if (args.length != 4) {
            String s = "Usage: <directory> <hostname> <port> <back log>";
            System.out.println(s);
            System.exit(0);
        }
        SimpleHTTPServer shttps =
            new SimpleHTTPServer(new File(args[0]),
                                 args[1],
                                 Integer.parseInt(args[2]),
                                 Integer.parseInt(args[3]));
        shttps.start();
    }
}
