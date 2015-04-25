
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

/**
 * A reader of {@link ByteTree} instances.
 *
 * @author Douglas Wikstrom
 */
public class ByteTreeReaderBT extends ByteTreeReader {

    /**
     * Underlying byte tree.
     */
    protected ByteTree bt;

    /**
     * Creates a reader of the given byte tree with the given parent.
     *
     * @param bt Underlying byte tree.
     */
    public ByteTreeReaderBT(ByteTree bt) {
        this(null, bt);
    }

    /**
     * Creates a reader of the given byte tree with the given parent.
     *
     * @param parent Instance that spawned this one.
     * @param bt Underlying byte tree.
     */
    public ByteTreeReaderBT(ByteTreeReader parent, ByteTree bt) {
        super(parent, getRemaining(bt));
        this.bt = bt;
    }

    /**
     * Returns the number of children/bytes in the given byte tree.
     *
     * @param bt Underlying byte tree.
     * @return Number of children/bytes
     */
    private static int getRemaining(ByteTree bt) {
        if (bt.value != null) {
            return bt.value.length;
        } else {
            return bt.children.length;
        }
    }


    // Documented in ByteTreeReader.java

    public boolean isLeaf() {
        return bt.value != null;
    }

    protected ByteTreeReader getNextChildInner() {
        ByteTree child = bt.children[bt.children.length - remaining];
        return new ByteTreeReaderBT(this, child);
    }

    protected void readInner(byte[] destination, int offset, int length) {
        System.arraycopy(bt.value,
                         bt.value.length - remaining,
                         destination,
                         offset,
                         length);
    }

    public void close() {

        // Potentially this helps the garbage collect.
        this.bt = null;
    }
}

