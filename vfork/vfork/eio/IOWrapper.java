
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

package vfork.eio;

import java.io.*;

import vfork.ui.*;

/**
 * File IO Interface.
 *
 * @author Eduardo Robles
 */
public interface IOWrapper {

    /**
     * List the files in a directory.
     *
     * @param path Directory path.
     *
     * @return Array of file paths of the files inside the given
     * directory.
     */
    public String[] listFiles(String path);

    /**
     * Checks if a given path is a directory.
     *
     * @param path File path to check.
     * @return Whether the path is a directory or not.
     */
    public boolean isDirectory(String path);

    /**
     * Reads a file as a stream.
     *
     * @param path Path of the file to open.
     * @return File as a generic input stream.
     */
    public DataInputStream open(String path)
        throws FileNotFoundException;

     /**
     * Opens a file as a data input stream but first seeking to the given
     * offset.
     *
     * @param path Path of the file to open.
     * @param offset Offset from which we should start reading.
     * @return File as a generic DataInput.
     */
    public DataInputStream open(String path, int offset)
        throws FileNotFoundException, IOException;

    /**
     * Opens the file at the given path as a data output stream.
     *
     * @param path Path of the file to open.
     * @return File as data output.
     */
    public DataOutputStream openDataOutputStream(String path)
        throws IOException;

    /**
     * Delete the given file.
     *
     * @param path Path of the file to open.
     */
    public boolean delete(String path);

    /**
     * @param path Path of the file to open.
     * @return The file name of the given path.
     */
    public String getFileName(String path);
}
