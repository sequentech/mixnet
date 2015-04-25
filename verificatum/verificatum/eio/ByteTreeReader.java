
/*
 * Copyright 2008 2009 2010 Douglas Wikstrom
 *
 * This file is part of Verificatum.
 *
 * Verificatum is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Verificatum is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Verificatum.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package verificatum.eio;

import java.io.*;

import verificatum.crypto.*;

/**
 * Abstract class of a reader of {@link ByteTree} instances. Reading
 * corresponds to a depth-first traversal of the tree, and such a
 * traversal is enforced. Public methods are provided for accessing
 * children and data of a byte tree. If such a method throws a {@link
 * EIOException}, then the underlying resources are deallocated and
 * further processing is illegal (undefined behavior). This poses no
 * restriction, since an application can query an instance to traverse
 * the tree without any exceptions.
 *
 * @author Douglas Wikstrom
 */
public abstract class ByteTreeReader {

    /**
     * Number of children/bytes remaining to be read.
     */
    protected int remaining;

    /**
     * Instance that spawned this one (null if this is the root).
     */
    protected ByteTreeReader parent;

    /**
     * Indicates that there is an active child, i.e., there is more to
     * read in the child that was most recently returned, so it is
     * illegal to ask for another child.
     */
    protected boolean activeChild;

    /**
     * Creates an uninitialized instance.
     */
    protected ByteTreeReader() {}

    /**
     * Creates an instance with the given parent and number of
     * children/bytes to read.
     *
     * @param parent Parent of this instance.
     * @param remaining Number of children/bytes remaining to be read.
     */
    protected ByteTreeReader(ByteTreeReader parent, int remaining) {
        this.parent = parent;
        this.remaining = remaining;
        this.activeChild = false;
    }

    /**
     * Returns true or false depending on if this reader points to a
     * leaf or not.
     *
     * @return true or false depending on if this reader points to a
     * leaf or not.
     */
    public abstract boolean isLeaf();

    /**
     * Returns a reader of the next child to read. Subclasses
     * implementing this method may assume that there is a next child
     * to read and that previous children of this instance have
     * already been processed fully.
     *
     * @return Next child to read.
     */
    protected abstract ByteTreeReader getNextChildInner();

    /**
     * Reads data into the given array. Subclasses implementing this
     * method may assume that the requested number of bytes is less or
     * equal to the number of remaining bytes and that the result fits
     * in the given array, and that all previous children have been
     * processed fully.
     *
     * @param destination Destination array of data.
     * @param offset Where to start writing in destination array.
     * @param length Number of bytes to read.
     */
    protected abstract void readInner(byte[] destination, int offset,
                                      int length);

    /**
     * Deallocates any resources allocated by this instance, e.g.,
     * opened files. This must be called if reading is interrupted due
     * to bad formatting, and otherwise it is called automatically
     * upon the end of reading. Spurious calls to close must be
     * ignored.
     */
    public abstract void close();



    // Implemented in terms of the above.

    /**
     * Returns a reader of the next child to read.
     *
     * @return Next child to read.
     *
     * @throws EIOException If there are no more children to read or
     * if data remains to be read in the child preceeding the
     * requested one in a depth-first traversal of the underlying byte
     * tree.
     */
    public ByteTreeReader getNextChild() throws EIOException {

        // These problems may occur if reading a maliciously
        // constructed byte tree. This should not give a fatal error.
        if (isLeaf()) {
            close();
            throw new EIOException("Requesting child from leaf!");
        }
        if (remaining == 0) {
            close();
            throw new EIOException("There are no more children!");
        }

        // This problem can only occur due to bad programming.
        if (activeChild) {
            throw new EIOError("Violating depth-first traversal!");
        }

        activeChild = true;
        ByteTreeReader res = getNextChildInner();

        remaining--;
        return res;
    }

    /**
     * Skips one child when reading.
     */
    public void skipChild() throws EIOException {
        ByteTreeReader btr = getNextChild();
        if (btr.isLeaf()) {
            btr.read();
        } else {
            for (int i = 0; i < btr.getRemaining(); i++) {
                btr.skipChild();
            }
        }
    }

    /**
     * Skips a number of children when reading.
     *
     * @param n Number of children to skip.
     */
    public void skipChildren(int n) throws EIOException {
        for (int i = 0; i < n; i++) {
            skipChild();
        }
    }

    /**
     * Reads data into the given array.
     *
     * @param destination Destination array of data.
     * @param offset Where to start writing in destination array.
     * @param length Number of bytes to read.
     * @return Number of bytes written.
     *
     * @throws IOException If this is not a leaf or if too many bytes
     * are requested.
     */
    public int read(byte[] destination, int offset, int length)
        throws EIOException {

        // This problem occurs if the byte tree is maliciously
        // constructed to have too few bytes. This should not give a
        // fatal error.
        if (length > remaining) {
            close();
            throw new EIOException("Requesting too many bytes!");
        }

        // These problems can occur due to programming errors.
        if (offset < 0 || length < 0) {
            throw new EIOError("Negative offset or length!");
        }
        if (offset + length > destination.length) {
            throw new EIOError("Bytes do not fit in destination array!");
        }

        readInner(destination, offset, length);
        remaining -= length;

        if (remaining == 0) {

            // If there is nothing more to read from this instance,
            // then we move up the tree until we find something that
            // has not been processed fully, or end up at the root.
            ByteTreeReader btr = parent;
            while (btr != null && btr.remaining == 0) {

                // We close children that should never be visited
                // again to the garbage collect.
                ByteTreeReader tmpbtr = btr;
                btr = btr.parent;
                tmpbtr.close();
            }
            if (btr != null) {

                // If there is more to process, then we signal this.
                btr.activeChild = false;
            }
        }
        return length;
    }

    /**
     * Returns the number of children/bytes remaining to be read.
     *
     * @return Number of children/bytes remaining to be read.
     */
    public int getRemaining() {
        return remaining;
    }

    /**
     * Reads data into the given array.
     *
     * @param destination Destination array of data.
     * @return Number of written bytes.
     *
     * @throws IOException If this is not a leaf or if too many bytes
     * are requested.
     */
    public int read(byte[] destination) throws EIOException {
        return read(destination, 0, destination.length);
    }

    /**
     * Reads and returns a given number of bytes.
     *
     * @param length Number of bytes to read.
     * @return Read bytes.
     *
     * @throws IOException If this is not a leaf or if too many bytes
     * are requested.
     */
    public byte[] read(int length) throws EIOException {
        byte[] tmp = new byte[length];
        read(tmp, 0, length);
        return tmp;
    }

    /**
     * Reads and returns all remaining bytes.
     *
     * @return Read bytes.
     *
     * @throws IOException If this is not a leaf.
     */
    public byte[] read() throws EIOException {
        byte[] tmp = new byte[remaining];
        read(tmp, 0, remaining);
        return tmp;
    }

    /**
     * Read four bytes and return them as an int.
     *
     * @return Integer read.
     *
     * @throws IOException If this is not a leaf or if the remaining
     * number of bytes is less than four.
     */
    public int readInt() throws EIOException {
        return ExtIO.readInt(read(4), 0);
    }

    /**
     * Read byte[] return it as an int[].
     *
     * @param size Number of integers to read.
     * @return Array of integers
     * @throws EIOException If there are not enough bytes to read.
     */
    public int[] readInts(int size) throws EIOException {
        int[] res = new int[size];
        ExtIO.readInts(res, 0, read(4 * size), 0, size);
        return res;
    }

    /**
     * Reads a boolean value from this reader.
     */
    public boolean readBoolean() throws EIOException {
        byte[] res = read(1);
        if (res[0] == 0) {
            return false;
        } else if (res[0] == 1) {
            return true;
        }
        throw new EIOException("Not a boolean value!");
    }

    /**
     * Reads a <code>boolean[]</code> value from this reader.
     *
     * @param size Number of booleans to read.
     * @return Array of boolean.
     */
    public boolean[] readBooleans(int size) throws EIOException {
        boolean[] res = new boolean[size];
        byte[] tmp = read(size);
        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i] == 1) {
                res[i] = true;
            } else if (tmp[i] == 0) {
                res[i] = false;
            } else {
                throw new EIOException("Not an array of booleans!");
            }
        }
        return res;
    }

    /**
     * Reads at most the given number of bytes and interprets them as
     * a UTF-8 encoded string.
     *
     * @param size Maximal number of characters read.
     * @return String representing the read data.
     *
     * @throws IOException If this is not a leaf or if too many bytes
     * are requested.
     */
    public String readString(int size) throws EIOException {
        try {

            return new String(read(size), "UTF8");

        } catch (UnsupportedEncodingException uee) {

            // This should never happen, since UTF8 is a valid
            // encoding.
            throw new EIOError("This is a bug!", uee);
        }
    }

    /**
     * Reads all remaining bytes and interprets them as a UTF-8
     * encoded string.
     *
     * @return String representing the read data.
     *
     * @throws IOException If this is not a leaf.
     */
    public String readString() throws EIOException {
        return readString(remaining);
    }
}
