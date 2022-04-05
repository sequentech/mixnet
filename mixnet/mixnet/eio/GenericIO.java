/*
 * Copyright 2011 Eduardo Robles Elvira <edulix AT wadobo DOT com>
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

package mixnet.eio;

import java.io.*;

import mixnet.ui.*;

/**
 * Generic class that allows to abstract the IO implementation in use.
 * By default it uses normal files.
 *
 * @author Eduardo Robles
 */
public class GenericIO {

    /**
     * Contains the IOWrapper delegate to be used.
     */
    private static IOWrapper delegate = new NativeIOWrapper();


    // TODO
    // private static Log log = null;

    /**
     * Sets the IOWrapper instance to be used.
     *
     * @param delegator Delegator.
     */
    public static void setIOWrapper(IOWrapper newDelegate) {
        delegate = newDelegate;
    }

    /**
     * Returns the current IO wrapper.
     *
     * @return IOWrapper instance in use.
     */
    public static IOWrapper getIOWrapper() {
        return delegate;
    }

    // TODO
    // /**
    //  * Sets the Log instance to be used.
    //  *
    //  * @param log Logging context.
    //  */
    // public static void setLog(Log log) {
    //     GenericIO.log = log;
    // }

    // /**
    //  * Logging context of this IO wrapper.
    //  *
    //  * @return Log instance to be used.
    //  */
    // public static Log getLog() {
    //     if (log == null) {
    //         log = new Log();
    //     }
    //     return log;
    // }

    /**
     * List the files in a directory.
     *
     * @param path Directory path.
     *
     * @return Array of file paths of the files inside the given
     * directory.
     */
    public static String[] listFiles(String path) {
        return delegate.listFiles(path);
    }

    /**
     * Checks if a given path is a directory.
     *
     * @param path File path to check.
     * @return Whether the path is a directory or not.
     */
    public static boolean isDirectory(String path) {
        return delegate.isDirectory(path);
    }

    /**
     * Reads a file as a stream.
     *
     * @param path Path of the file to open.
     * @return File as a generic input stream.
     */
    public static DataInputStream open(String path)
        throws FileNotFoundException {
        return delegate.open(path);
    }

    /**
     * Opens a file as a data input stream but first seeking to the given
     * offset.
     *
     * @param path Path of the file to open.
     * @param offset Offset from which we should start reading.
     * @return File as a generic DataInput.
     */
    public static DataInputStream open(String path, int offset)
        throws FileNotFoundException, IOException {
        return delegate.open(path, offset);
    }

    /**
     * Opens the file at the given path as a data output stream.
     *
     * @param path Path of the file to open.
     * @return File as data output.
     */
    public static DataOutputStream openDataOutputStream(String path)
        throws IOException {
        return delegate.openDataOutputStream(path);
    }

    /**
     * Delete the given file.
     *
     * @param path Path of the file to open.
     */
    public static void delete(String path) {
        delegate.delete(path);
    }

    /**
     * @param path Path of the file to open.
     * @return The file name of the given path.
     */
    public static String getFileName(String path) {
        return delegate.getFileName(path);
    }
}
