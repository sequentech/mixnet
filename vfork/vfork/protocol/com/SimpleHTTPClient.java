
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

import vfork.eio.*;
import vfork.ui.*;
import vfork.protocol.*;

/**
 * Simplistic HTTP client. It can only download files as
 * binary/octet-streams.
 *
 * @author Douglas Wikstrom
 */
public class SimpleHTTPClient {

    /**
     * Size in bytes of buffer used for streaming.
     */
    public static int BUFFER_SIZE = 4096;

    /**
     * Fetches a remote file and writes it to the given output stream.
     *
     * @param os Stream where the fetched data is written.
     * @param url URL of file to be fetched.
     * @param readTimeOut Longest waiting time in milliseconds before
     * assuming that transfer failed.
     * @param maximalByteLength Maximal number of bytes to be downloaded.
     * @param log Logging context
     * @return <code>true</code> or <code>false</code> depending on if
     * the download succeeded or not.
     */
    public static boolean fetchFile(OutputStream os,
                                    URL url,
                                    int readTimeOut,
                                    long maximalByteLength,
                                    Log log) {

        boolean result = true;

        HttpURLConnection connection = null;
        InputStream is = null;
        try {

            Timer timer = new Timer(readTimeOut);

            // Connect using URL.
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(readTimeOut);
            connection.connect();

            if (!timer.timeIsUp() &&
                connection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                int contentLength = connection.getContentLength();

                if (contentLength < 0) {

                    log.info("Unknown content length!");
                    result = false;

                } else if (contentLength > maximalByteLength) {

                    log.info("Requested file is too long! (more than " +
                             maximalByteLength + " bytes)");
                    result = false;

                } else {

                    is = connection.getInputStream();

                    byte[] buf = new byte[BUFFER_SIZE];
                    int remaining = contentLength;
                    for (;;) {

                        int rlen = Math.min(remaining, BUFFER_SIZE);
                        int len = is.read(buf, 0, rlen);

                        if (timer.timeIsUp() || len == -1 || remaining == 0) {

                            break;

                        } else {

                            os.write(buf, 0, len);
                            remaining -= len;

                        }
                    }

                    // It is possible to send files using the HTTP
                    // protocol without any length embedded. If you
                    // use an external HTTP-server and your protocol
                    // fails at this point, then the likely cause is
                    // that your server incorrectly sets the header to
                    // 0 or -1.

                    if (remaining > 0) {
                        log.info("Expected " + contentLength +
                                 " bytes, but " + remaining +
                                 " bytes are missing! Does your HTTP " +
                                 "server set the content length correctly?");
                        result = false;
                    }
                }

            } else {
                result = false;
            }
        } catch (MalformedURLException murle) {
            throw new ProtocolError("Not a valid URL!", murle);
        } catch (SocketTimeoutException ste) {
            log.info("Socket timed out while waiting for data!");
            result = false;
        } catch (java.net.ConnectException ce) {
            // log.info("Unable to connect to " +
            //          url.getHost() + ":" + url.getPort() + "!");
            result = false;
        } catch (java.net.ProtocolException pe) {
            log.info("Exception in the underlying network stack!");
            log.register(pe);
            result = false;
        } catch (IOException ioe) {
            log.info("Exception while reading or writing!");
            log.register(ioe);
            result = false;
        } finally {
            ExtIO.strictClose(is);
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }

    /**
     * Fetches a remote file and stores it under the same name in the
     * given directory. The reason for any failing to do so is logged.
     *
     * @param rootURL Location of remote file.
     * @param destinationDir Local directory where the file is stored
     * after download.
     * @param fileName Name of file.
     * @param readTimeOut Longest waiting time in milliseconds before
     * assuming that transfer failed.
     * @param maximalByteLength Maximal number of bytes to be downloaded.
     * @param log Logging context
     * @return <code>true</code> or <code>false</code> depending on if
     * the download succeeded or not.
     */
    public static boolean fetchFile(URL rootURL,
                                    File destinationDir,
                                    String fileName,
                                    int readTimeOut,
                                    long maximalByteLength,
                                    Log log) {

        FileOutputStream fos = null;
        boolean result = true;

        try {
            File tmp = new File(fileName);
            File parent = tmp.getParentFile();
            String name = tmp.getName();
            File fullDestinationDir =
                new File(destinationDir, parent.toString());
            File tmpDestinationFile =
                new File(fullDestinationDir, "_" + name);

            // Make sure destination directory exists.
            if (!fullDestinationDir.exists() &&
                !fullDestinationDir.mkdirs()) {
                throw new ProtocolError("Unable to make directories! (" +
                                        fullDestinationDir + ")");
            }

            // Open temporary destination file.
            fos = new FileOutputStream(tmpDestinationFile);

            // Attempt to fetch data.
            URL url = new URL(rootURL, fileName);
            result = fetchFile(fos, url, readTimeOut, maximalByteLength, log);
            ExtIO.strictClose(fos);

            // Final destination of data.
            File destinationFile = new File(fullDestinationDir, name);

            // Delete destination file if it exists.
            if (destinationFile.exists()) {
                destinationFile.delete();
            }

            // Rename temporary file to target file.
            if (!tmpDestinationFile.renameTo(destinationFile)) {
                String description =
                    "Unable to rename temporary file \""
                    + tmpDestinationFile + " to \""
                    + destinationFile + "\"!";
                throw new ProtocolError(description);
            }
        } catch (MalformedURLException murle) {
            throw new ProtocolError("Not a valid URL!", murle);
        } catch (IOException ioe) {
            log.info("Exception while performing IO!");
            log.register(ioe);
            result = false;
        } finally {
            ExtIO.strictClose(fos);
        }
        return result;
    }

    /**
     * Fetches a remote file and returns the content as a byte array.
     *
     * @param rootURL Location of remote file.
     * @param fileName Name of file.
     * @param readTimeOut Longest waiting time in milliseconds before
     * assuming that transfer failed.
     * @param maximalByteLength Maximal number of bytes to be downloaded.
     * @param log Logging context
     * @return A <code>byte[]</code> containing the data, or
     * <code>null</code> if the download failed.
     */
    public static byte[] fetchFile(URL rootURL,
                                   String fileName,
                                   int readTimeOut,
                                   long maximalByteLength,
                                   Log log) {

        boolean result = false;
        ByteArrayOutputStream baos = null;

        byte[] contents = null;
        try {
            baos = new ByteArrayOutputStream();
            URL url = new URL(rootURL, fileName);
            result = fetchFile(baos, url, readTimeOut, maximalByteLength, log);
            ExtIO.strictClose(baos);

            if (result) {
                contents = baos.toByteArray();
            }
        } catch (MalformedURLException murle) {
            throw new ProtocolError("Not a valid URL!", murle);
        } finally {
            ExtIO.strictClose(baos);
        }
        return contents;
    }
}

