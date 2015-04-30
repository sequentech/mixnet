
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

package vfork.eio;

import java.io.*;
import java.util.*;

import vfork.crypto.*;
import vfork.ui.*;

/**
 * This class is part of an implementation of a byte oriented
 * intermediate data format. Documentation is provided in {@link
 * ByteTreeBasic}.
 *
 * @author Douglas Wikstrom
 */
public class ByteTreeF extends ByteTreeBasic {

    /**
     * Number of bytes in buffer used for computing digests.
     */
    final static int DIGEST_BUFFER_SIZE = 4096;

    /**
     * Number of bytes in buffer used for converting data on file to a
     * byte[].
     */
    final static int BUFFER_SIZE = 4096;

    /**
     * File holding the data of this instance.
     */
    protected File file;

    /**
     * Constructs an instance with the given data. Note that the data
     * on file is <em>not</em> copied. It is the responsibility of the
     * programmer to make sure that the underlying file remains intact
     * during the life time of this instance. It is also the
     * responsibility of the programmer to remove the underlying file
     * when this is no longer needed.
     *
     * @param file File containing a byte array representation of a
     * byte tree.
     */
    public ByteTreeF(File file) {
        this.file = file;
    }

    // Documented in ByteTreeBasic.java.

    public ByteTreeReader getByteTreeReader() {
        return new ByteTreeReaderF(null, this);
    }

    public void update(Hashdigest digest) {
        FileInputStream fis = null;

        try {

            fis = new FileInputStream(file);

            byte[] buf = new byte[DIGEST_BUFFER_SIZE];
            int len = 0;
            while ((len = fis.read(buf)) != -1) {
                digest.update(buf, 0, len);
            }

        } catch (IOException ioe) {
            throw new EIOError("Internal error!", ioe);
        } finally {
            ExtIO.strictClose(fis);
        }
    }


    /*
     * Overrides method in ByteTreeBasic.java
     */
    public void writeTo(DataOutputStream dos)
        throws EIOException {
        try {

            ExtIO.copyFile(file, dos);

        } catch (FileNotFoundException fnfe) {
            throw new EIOException("File not found!", fnfe);
        } catch (SecurityException se) {
            throw new EIOException("Not allowed to open file!", se);
        } catch (IOException ioe) {
            throw new EIOException("Unable to write file!", ioe);
        }
    }

    public long totalByteSize() {
        return file.length();
    }

    public int toByteArray(byte[] result, int offset) {
        try {
            FileInputStream fis = new FileInputStream(file);

            byte[] buf = new byte[BUFFER_SIZE];

            int tmpOffset = offset;
            int len = 0;
            while ((len = fis.read(buf)) != -1) {
                System.arraycopy(buf, 0, result, tmpOffset, len);
                tmpOffset += len;
            }
            return tmpOffset - offset;

        } catch (IOException ioe) {
            throw new EIOError("Unable to convert to byte[]!", ioe);
        }
    }



    // ###############################################################

    /**
     * Returns true or false depending on if the contents of the given
     * file is a valid byte tree or not. This provides the first
     * shield against malformed inputs.
     *
     * @param file File to verify.
     * @param maximalRecursiveDepth Maximal recursion depth of the given byte
     * tree.
     * @return true or false depending on if the contents of the given
     * file is a valid byte tree or not.
     */
    public static boolean verifyFormat(File file, int maximalRecursiveDepth) {

        DataInputStream dis = null;
        boolean res = true;
        try {

            dis = new DataInputStream(new FileInputStream(file));

            // Check that there is a properly constructed byte tree.
            verifyFormat(dis, maximalRecursiveDepth);

            // Check that there is nothing more.
            if (dis.read() != -1) {
                res = false;
            }

        } catch (IOException ioe) {
            res = false;
        } catch (EIOException eioe) {
            res = false;
        } finally {
            ExtIO.strictClose(dis);
            return res;
        }
    }

    /**
     * Returns true or false depending on if the contents of the given
     * stream is a valid byte tree or not. This provides the first
     * shield against malformed inputs.
     *
     * @param dis Stream to verify.
     * @param maximalRecursiveDepth Maximal recursion depth of the given byte
     * tree.
     * @throws IOException If the stream can not be read.
     * @throws EIOException If the format of the input file is incorrect.
     */
    public static void verifyFormat(DataInputStream dis,
                                    int maximalRecursiveDepth)
        throws IOException, EIOException {

        int type = dis.readByte();
        int length = dis.readInt();

        if (type == ByteTreeBasic.LEAF) {

            // If we are supposed to be a leaf, we attempt to skip a
            // suitable number of bytes.
            int len = length;
            while (len > 0) {
                len -= dis.skipBytes(len);
            }

        } else if (type == ByteTreeBasic.NODE) {

            // If a we are a node, then we attempt to call ourselves
            // recursively to read the correct number of children.
            if (maximalRecursiveDepth == 0) {
                throw new EIOException("Too deep recursion!");
            }

            for (int i = 0; i < length; i++) {

                verifyFormat(dis, maximalRecursiveDepth - 1);

            }
        } else {
            throw new EIOException("Malformed type!");
        }
    }
}
