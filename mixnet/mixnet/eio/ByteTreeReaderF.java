
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

import mixnet.crypto.*;

/**
 * A reader of {@link ByteTreeF} instances.
 *
 * @author Douglas Wikstrom
 */
public class ByteTreeReaderF extends ByteTreeReader {

    /**
     * Source of data.
     */
    protected DataInputStream dis;

    /**
     * Indicates if this instance points to a leaf or not.
     */
    protected boolean isLeaf;

    /**
     * Indicates that this instance opened the underlying stream.
     */
    protected boolean opener;

    /**
     * Creates an instance with the given parent and underlying byte
     * tree.
     *
     * @param parent Instance that spawned this one.
     * @param bt Underlying byte tree.
     */
    public ByteTreeReaderF(ByteTreeReader parent, ByteTreeF bt) {
        try {

            this.dis = new DataInputStream(new FileInputStream(bt.file));
            ByteTreeReaderF btr = new ByteTreeReaderF(parent, dis);

            this.parent = parent;
            this.isLeaf = btr.isLeaf;
            this.remaining = btr.remaining;
            this.opener = true;

        } catch (FileNotFoundException fnfe) {
            throw new EIOError("File not found!", fnfe);
        }
    }

    /**
     * Creates an instance with the given parent and reading from the
     * given data source.
     *
     * @param parent Instance that spawned this one.
     * @param dis Source of data.
     */
    protected ByteTreeReaderF(ByteTreeReader parent, DataInputStream dis) {
        try {

            this.parent = parent;
            this.dis = dis;
            this.isLeaf = (dis.readByte() == ByteTreeBasic.LEAF);

            this.remaining = dis.readInt();
            this.opener = false;

        } catch (IOException ioe) {
            throw new EIOError("Could not read!", ioe);
        }
    }


    // Documented in ByteTreeReader.java.

    public boolean isLeaf() {
        return isLeaf;
    }

    protected ByteTreeReader getNextChildInner() {
        return new ByteTreeReaderF(this, dis);
    }

    protected void readInner(byte[] destination, int offset, int length) {
        try {

            int end = offset + length;
            while (offset < end) {
                offset += dis.read(destination, offset, end - offset);
            }

        } catch (IOException ioe) {
            throw new EIOError("Unable to read!", ioe);
        }
    }

    public void close() {
        if (opener) {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException ioe) {
                throw new EIOError("Unable to close stream!", ioe);
            }
        }
    }
}
