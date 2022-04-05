
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

package mixnet.eio;

import java.io.*;

import mixnet.eio.*;

/**
 * Manages temporary files. The reason that this is not implemented
 * using {@link File#createTempFile(String,String, File)} is that we
 * need to control where the files are stored.
 *
 * @author Douglas Wikstrom
 */
public class TempFile {

    /**
     * Directory used to store temporary files.
     */
    public static File storageDir = null;

    /**
     * Counter used to name files unambigously.
     */
    public static int fileNameCounter;

    /**
     * Initializes the storage directory. Any existing files in the
     * storage directory may be overwritten. Only the first call to
     * this method makes any changes.
     *
     * @param theStorageDir Storage directory.
     */
    public static void init(File theStorageDir) {
        if (storageDir == null) {
            storageDir = theStorageDir;
            fileNameCounter = 0;
        }
    }

    /**
     * Returns a uniquely named temporary file.
     *
     * @return Uniquely named temporary file.
     */
    public synchronized static File getFile() {
        File file = new File(storageDir,
                             String.format("%03d", fileNameCounter));
        fileNameCounter++;
        return file;
    }

    /**
     * Removes the entire temporary storage directory.
     */
    public static void free() {
        String[] list = storageDir.list();

        for (int i = 0; i < list.length; i++) {
            File file = new File(storageDir, list[i]);

            file.delete();
        }
        storageDir.delete();
    }
}
