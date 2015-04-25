
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
import java.util.*;

import verificatum.crypto.*;
import verificatum.ui.*;

/**
 * This class is the base class of an intermediate byte oriented
 * format used for communication and storing data to file. Let us call
 * this format byte tree. In contrast to a raw byte array, a byte tree
 * has ordered tree structure. This simplifies parsing any one of the
 * numerous formats needed in other classes and allows performing
 * basic verification of an input. We use our own format and not the
 * {@link java.io.Serializable} class to avoid the overhead imposed by
 * this interface and to simplify writing a compatible implementation
 * in some other language.
 *
 * <p>
 *
 * A byte tree is defined as follows. A byte tree either holds data or
 * other byte trees. In the former case the data is simply stored as a
 * <code>byte[]</code>. In the latter case the instance holds an array
 * of byte trees, that may themselves either hold data or be byte
 * trees. A byte tree can be turned into an array of bytes using depth
 * first traversal. If a byte tree stores data, it is converted to a
 * byte array consisting of: a single byte (one) signalling that this
 * is a data-storing byte tree (a leaf), four bytes giving the number
 * of bytes of data (an int), and the actual data bytes. If a byte
 * tree stores other byte trees, then it is converted into a byte
 * array containing: a single byte (zero) saying that this is a
 * tree-storing byte tree (an inner node), four bytes storing the
 * number of byte trees (an int), and then recursively the byte arrays
 * of the child byte trees. Conversely, data on file can be recovered
 * from an array of bytes. A byte tree can of course be read/written
 * to/from a file without first explicitly converting it into/from an
 * array of bytes. This is needed to allow very large byte trees.
 *
 * <p>
 *
 * The implementation consists of the following classes.
 *
 * <ul>
 *
 * <li> ByteTreeBasic (this class): Provides basic writing
 * functionality and hashing functionality; partially in terms of
 * abstract methods.
 *
 * <li> {@link ByteTree}: Provides an in-memory implementation of a
 * handler of the format. This should be used to pack/unpack
 * reasonable amounts of data that fits comfortably in memory.
 *
 * <li> {@link ByteTreeF}: Provides a file based byte tree.
 *
 * <li> {@link ByteTreeContainer}: In protocols, the parties may need
 * to send structured data where some subtrees are fairly small and
 * thus most easily handled using <code>ByteTree</code> and others are
 * huge and handled by <code>ByteTreeLazy</code>. For the receiver
 * this is not a problem. He can read such data using
 * <code>ByteTreeF</code>. <code>ByteTreeContainer</code> provides the
 * needed functionality of the sender. Several
 * <code>ByteTreeBasic</code> instances can be combined. This
 * corresponds to structured data where some subtrees are stored on
 * file and some are in memory.
 *
 * <li> {@link ByteTreeReader}: Abstract class representing a reader
 * of byte trees
 *
 * <li> {@link ByteTreeReaderF}: Reader of {@link ByteTreeF}.
 *
 * <li> {@link ByteTreeReaderC}: Reader of {@link ByteTreeContainers}.
 *
 * <li> {@link ByteTreeReaderBT}: Reader of {@link ByteTree}.
 *
 * <li> {@link ByteTreeWriterF}: Writer of {@link ByteTree}
 * instance. This should only be used <em>inside</em> classes, e.g.,
 * in {@link LargeIntegerArrayF}.
 *
 * </ul>
 *
 * @author Douglas Wikstrom
 */
public abstract class ByteTreeBasic {

    /**
     * Tag used to label the instance as a node.
     */
    public final static byte NODE = 0;

    /**
     * Tag used to label the instance as a leaf.
     */
    public final static byte LEAF = 1;

    /**
     * Returns a reader of this instance.
     *
     * @return Reader of this instance.
     */
    public abstract ByteTreeReader getByteTreeReader();

    /**
     * Update the given hash digest with the content of this byte
     * tree.
     *
     * @param digest Digest to be updated.
     */
    public abstract void update(Hashdigest digest);

    /**
     * Writes this instance to the output stream given as input.
     *
     * @param dos Destination output stream.
     *
     * @throws EIOException If this instance can not be written to the
     * given output stream.
     * @throws IOException If this instance can not be written to the
     * given output stream.
     */
    public abstract void writeTo(DataOutputStream dos) throws EIOException;

    /**
     * Outputs the total number of bytes needed to flatten this
     * instance, i.e., to represent this instance as a
     * <code>byte[]</code>.
     *
     * @return Total number of bytes needed.
     */
    public abstract long totalByteSize();

    /**
     * Writes a <code>byte[]</code> representation of this instance to
     * <code>result</code> starting at the index <code>i</code>.
     *
     * @param result Destination array.
     * @param offset Index where to start writing.
     * @return Number of bytes written.
     */
    public abstract int toByteArray(byte[] result, int offset);



    // Implemented in terms of the above.

    /**
     * Writes this instance to the output stream given as input.
     *
     * @param dos Destination output stream.
     *
     * @throws EIOError If this instance can not be written to the
     * given output stream.
     */
    public void unsafeWriteTo(DataOutputStream dos) {
        try {
            writeTo(dos);
        } catch (EIOException eioe) {
            throw new EIOError("Internal error!", eioe);
        }
    }

    /**
     * Writes this instance to the file given as input.
     *
     * @param file Destination output file.
     *
     * @throws EIOException If this instance can not be written to the
     * given file.
     * @throws IOException If this instance can not be written to the
     * given file.
     */
    public void writeTo(File file) throws EIOException {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(file));
            writeTo(dos);
        } catch (IOException ioe) {
            throw new EIOException("Can not write file!", ioe);
        } finally {
            ExtIO.strictClose(dos);
        }
    }

    /**
     * Writes this instance to the file given as input.
     *
     * @param file Destination output file.
     *
     * @throws EIOError If this instance can not be written to the
     * given file.
     */
    public void unsafeWriteTo(File file) {
        try {
            writeTo(file);
        } catch (EIOException eioe) {
            throw new EIOError("Internal error!", eioe);
        }
    }

    /**
     * Outputs a <code>byte[]</code> representation of this
     * instance. WARNING! A <code>byte[]</code> can have at most
     * <i>2<sup>31</sup>-1</i> elements. Thus, if
     * <code>ByteTree</code>s that do not fit into a
     * <code>byte[]</code> must be handled, then use {@link
     * #writeTo(DataOutputStream)} instead.
     *
     * @return Representation of this instance.
     * @throws EIOError If this instance is too large to convert
     * to a <code>byte[]</code>.
     */
    public byte[] toByteArray() {
        long total = totalByteSize();

        if (total > Integer.MAX_VALUE) {
            throw new EIOError("Too big to convert to byte[]!");
        }

        byte[] result = new byte[(int)total];
        toByteArray(result, 0);
        return result;
    }
}