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
 * IOWrapper implementation using native Java file IO.
 *
 * @author Eduardo Robles
 */
public class NativeIOWrapper implements IOWrapper {

    // Documented in GenericIO.java

    public String[] listFiles(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        String[] ret = new String[files.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = files[i].getAbsolutePath();
        }
        return ret;
    }

    public boolean isDirectory(String path) {
        File file = new File(path);
        return file.isDirectory();
    }

    public DataInputStream open(String path)
        throws FileNotFoundException {
        File file = new File(path);
        return new DataInputStream(new FileInputStream(path));
    }

    public DataInputStream open(String path, int offset)
        throws FileNotFoundException, IOException {
        File file = new File(path);
        DataInputStream dis = new DataInputStream(new FileInputStream(path));

        // Warning! this is *slow*
        dis.skipBytes(offset);
        return dis;
    }

    public DataOutputStream openDataOutputStream(String path)
        throws IOException {
        return new DataOutputStream(new FileOutputStream(path));
    }

    public boolean delete(String path) {
        File file = new File(path);
        return file.delete();
    }

    public String getFileName(String path) {
        File file = new File(path);
        return file.getName();
    }
}
