
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

/**
 * A reader of {@link ByteTreeContainer} instances.
 *
 * @author Douglas Wikstrom
 */
public class ByteTreeReaderC extends ByteTreeReader {

    /**
     * Byte tree from which this instance reads.
     */
    protected ByteTreeContainer btc;

    /**
     * Position of reader within the underlying byte tree.
     */
    protected int index;

    /**
     * Creates an instance that reads from the given byte tree and
     * with the given parent.
     *
     * @param parent Instance that spawned this one.
     * @param btc Byte tree from which this instance reads.
     */
    protected ByteTreeReaderC(ByteTreeReader parent, ByteTreeContainer btc) {
        super(parent, btc.children.length);
        this.btc = btc;
        this.index = 0;
    }


    // Documented in ByteTreeReader.java

    public boolean isLeaf() {
        return false;
    }

    protected ByteTreeReader getNextChildInner() {
        ByteTreeReader btr = btc.children[index++].getByteTreeReader();
        btr.parent = this;
        return btr;
    }

    protected void readInner(byte[] destination, int offset, int length) {
        throw new EIOError("A ByteTreeContainer is always a node!");
    }

    public void close() {
        btc = null;
    }
}
