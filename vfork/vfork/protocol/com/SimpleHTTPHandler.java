
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

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

import com.sun.net.httpserver.*;

import vfork.eio.*;
import vfork.protocol.*;

/**
 * Handles HTTP requests by a clients in a simplistic way.
 *
 * @author Douglas Wikstrom
 */
public class SimpleHTTPHandler implements HttpHandler {

    /**
     * Size of buffer used for streaming.
     */
    final static int BUFFER_SIZE = 4096;

    /**
     * Directory containing files that may be requested by clients.
     */
    protected File directory;

    /**
     * Handles requests by clients.
     *
     * @param directory Directory containing files that may be
     * requested by clients.
     */
    public SimpleHTTPHandler(File directory) {
        this.directory = directory;
    }

    /**
     * Handler of exchanges. If the name of the requested file
     * consists only of digits, letters, and the special symbols "/",
     * "_", and ".", and does not have any subsequences of more than
     * one ".", then it is checked if the file exists in our
     * directory. If so, the file is streamed to the client. Otherwise
     * a failure message is streamed instead.
     *
     * @param exchange Exchange to be handled.
     * @throws IOException If the handler fails due to IO problems.
     */
    public void handle(HttpExchange exchange) throws IOException {

        // We expect this to be "GET".
        String requestMethod = exchange.getRequestMethod();

        // Name of requested file without leading slash.
        String requestString = exchange.getRequestURI().getPath().substring(1);

        // The name of the requested file must not contain any
        // characters beyond digits, letters, "/", "_", ".", and must
        // not contain any sequence of more than one ".". If it does,
        // then we replace the entire filename by "/" before
        // continuing processing.
        for (int i = 0; i < requestString.length(); i++) {
            char c = requestString.charAt(i);

            if ('0' <= c && c <= '9') {
                continue;
            }
            if ('a' <= c && c <= 'z') {
                continue;
            }
            if ('A' <= c && c <= 'Z') {
                continue;
            }
            if (c == '_') {
                continue;
            }
            if (c == '/') {
                continue;
            }
            if (c == '.' && (i == 0 || requestString.charAt(i-1) != '.')) {
                continue;
            }
            requestString = "/";
            break;
        }

        // Full path of requested file assuming it resides in our
        // directory.
        File requestFile = new File(directory, requestString);

        // Initialize datastructures for our response.
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "binary/octet-stream");

        IOException ee = null;
        OutputStream os = null;
        BufferedInputStream bis = null;
        FileInputStream fis = null;
        try {

            // Write either requested file if it exists in our directory
            // and is readable, or inform client that the request failed.
            if (requestMethod.equals("GET") && requestFile.canRead()) {

                long requestLen = requestFile.length();

                exchange.sendResponseHeaders(HTTP_OK, requestLen);
                os = exchange.getResponseBody();

                fis = new FileInputStream(requestFile);

                byte[] buf = new byte[BUFFER_SIZE];
                int remaining = (int)requestLen;
                for (;;) {

                    int rlen = Math.min(remaining, BUFFER_SIZE);
                    int len = fis.read(buf, 0, rlen);

                    if (len == -1) {
                        break;
                    } else {
                        os.write(buf, 0, len);
                        remaining -= len;
                    }
                }

                if (remaining > 0) {
                    throw new IOException("Could not read complete file, " +
                                          remaining + " bytes remain!");
                }

            } else {

                String response = "No such file!";
                byte[] responseBytes = response.getBytes();
                exchange.sendResponseHeaders(HTTP_NOT_FOUND,
                                             responseBytes.length);
                os = exchange.getResponseBody();
                os.write(responseBytes);

            }
        } finally {

            ExtIO.strictClose(fis);
            ExtIO.strictClose(bis);
            exchange.close();
        }
    }
}
