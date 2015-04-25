
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
 * This class is part of an implementation of a byte oriented
 * intermediate data format. Documentation is provided in {@link
 * ByteTreeBasic}.
 *
 * @author Douglas Wikstrom
 */
/*
 * Note that the functions reading from/writing to streams are not
 * based on the ones dealing reading from/writing to byte[]. This is
 * to allow objects that may be too large to allow encoding as a
 * byte[].
 */
public class ByteTree extends ByteTreeBasic {

    /**
     * If this instance is a leaf, then value holds its contents and
     * otherwise it is null.
     */
    protected byte[] value;

    /**
     * If this instance is an inner node, then it holds an array of
     * its children and otherwise it is null.
     */
    protected ByteTree[] children;

    /**
     * Recover an instance from its hexadecimal string representation.
     *
     * @param hexString Representation of an instance.
     *
     * @throws EIOException If it is not possible to read a
     * <code>ByteTree</code> from the input.
     */
    public ByteTree(String hexString) throws EIOException {
        this(Hex.toByteArray(hexString), null);
    }

    /**
     * Create a <code>ByteTree</code> with a single zero length
     * <code>byte[]</code> as value, to be used as a default value.
     */
    public ByteTree() {
        value = new byte[0];
        children = null;
    }

    /**
     * Create a leaf containing the given data. The data is not
     * copied.
     *
     * @param value Contents of the leaf.
     */
    public ByteTree(byte[] value) {
        this.value = value;
        this.children = null;
    }

    /**
     * Create an inner node containing the inputs as children. The
     * input array is not copied.
     *
     * @param children Children of this node.
     */
    public ByteTree(ByteTree...children) {
        this.value = null;
        this.children = children;
    }

    public ByteTreeReader getByteTreeReader() {
        return new ByteTreeReaderBT(null, this);
    }

    public long totalByteSize() {

        // One byte for the label and 4 bytes for either the number of
        // children or the number of bytes of data.
        long byteSize = 5;

        // If value is not null, then this is a leaf and we simply add
        // the length of the value byte[].
        if (value != null) {
            byteSize += value.length;

        // Otherwise this is an inner vertex, and we add the total
        // byte sizes of all its children.
        } else {
            for (ByteTree child : children) {
                byteSize += child.totalByteSize();
            }
        }
        return byteSize;
    }

    // ##################################################################
    // ######### Writing/reading this instance to a byte[]. #############
    // ##################################################################

    public int toByteArray(byte[] result, int offset) {

        int origi = offset;

        if (value != null) {  // We are a leaf.

            // Write label
            result[offset] = LEAF;
            offset++;

            // Write size
            ExtIO.writeInt(result, offset, value.length);
            offset += 4;

            // Write contents
            System.arraycopy(value, 0, result, offset, value.length);
            offset += value.length;

        } else { // We are an inner vertex.

            // Write label
            result[offset] = NODE;
            offset++;

            // Write number of children
            ExtIO.writeInt(result, offset, children.length);
            offset += 4;

            // Write the children
            for (ByteTree child : children) {
                offset += child.toByteArray(result, offset);
            }

        }

        // Return the number of bytes written.
        return offset - origi;
    }

    /**
     * Reads this instance from the <code>byte[]</code> given as input
     * starting at the index contained in the second input. In
     * external calls the second parameter is expected to be
     * <code>null</code> meaning that the method reads from the
     * beginning of the input <code>byte[]</code>.
     *
     * @param data Holds representation of an instance.
     * @param ic Container of the index of where to start reading.
     *
     * @throws EIOException If it is not possible to read a
     * <code>ByteTree</code> from the input.
     */
    public ByteTree(byte[] data, IntContainer ic) throws EIOException {
        if (ic == null) {
            ic = new IntContainer(0);
        }
        if (data.length - ic.i < 5) {
            throw new EIOException("Not a representation of ByteTree!");
        }
        if (data[ic.i++] == LEAF) { // We are reading a leaf.

            // Read length of data stored in the leaf.
            int length = ExtIO.readInt(data, ic.i);
            ic.i += 4;

            // Copy data
            if (data.length - ic.i < length) {
                throw new EIOException("Missing data!");
            }
            value = Arrays.copyOfRange(data, ic.i, ic.i + length);
            ic.i += length;

            children = null;

        } else { // We are reading an inner node.

            value = null;

            // Read number of children.
            int length = ExtIO.readInt(data, ic.i);
            ic.i += 4;

            // Read each child.
            children = new ByteTree[length];
            for (int j = 0; j < children.length; j++) {
                children[j] = new ByteTree(data, ic);
            }
        }
    }


    // ##################################################################
    // ## Writing/reading this instance to a Data(Output/Input)Stream. ##
    // ##################################################################


    /**
     * Writes this instance to the output stream given as input.
     *
     * @param dos Destination output stream.
     * @throws EIOException If this instance can not be written to the
     * given output stream.
     */
    public void writeTo(DataOutputStream dos) throws EIOException {
        try {
            if (value != null) { // We are a leaf.

                dos.writeByte(LEAF);
                dos.writeInt(value.length);
                dos.write(value, 0, value.length);

            } else { // We are an inner node.

                dos.writeByte(NODE);
                dos.writeInt(children.length);

                for (ByteTree child : children) {
                    child.writeTo(dos);
                }
            }
        } catch (IOException ioe) {
            throw new EIOException("Unable to write ByteTree to stream!", ioe);
        }
    }

    /**
     * Reads this instance from the input stream given as input.
     *
     * @param dis Source input stream.
     * @throws EIOException If it is not possible to read a
     * <code>ByteTree</code> from the input stream.
     */
    public ByteTree(DataInputStream dis) throws EIOException {
        try {
            if (dis.readByte() == LEAF) { // We are reading a leaf.

                value = new byte[dis.readInt()];
                dis.readFully(value);
                children = null;

            } else { // We are reading an inner node.

                value = null;
                children = new ByteTree[dis.readInt()];

                for (int i = 0; i < children.length; i++) {
                    children[i] = new ByteTree(dis);
                }
            }
        } catch (EOFException eofe) {
            throw new EIOException("Unexpected end of file!", eofe);
        } catch (IOException ioe) {
            throw new EIOException("Can not read from stream!", ioe);
        }
    }


    // ##################################################################
    // ############ Reading this instance to File. ######################
    // ##################################################################

    /**
     * Creates an instance from the representation given on file.
     *
     * @param file File containing representation of byte tree.
     * @throws IOException If there is an IO error.
     * @throws EIOException If some input could not be read.
     */
    public ByteTree(File file) throws IOException, EIOException {
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new FileInputStream(file));
            ByteTree byteTree = new ByteTree(dis);
            value = byteTree.value;
            children = byteTree.children;
        } finally {
            ExtIO.strictClose(dis);
        }
    }


    // ##################################################################
    // Converting primitive types, Strings, and arrays to and from
    // ByteTrees.
    // ##################################################################


    /**
     * Translates a <code>boolean</code> to its <code>ByteTree</code>
     * representation.
     *
     * @param b Boolean value.
     * @return Representation of the given boolean value.
     */
    public static ByteTree booleanToByteTree(boolean b) {
        byte[] byteb = new byte[1];
        byteb[0] = (byte)(b ? 1 : 0);
        return new ByteTree(byteb);
    }

    /**
     * Translates a <code>ByteTree</code> to a <code>boolean</code>
     * value if possible and throws an exception otherwise.
     *
     * @param bt Representation of the boolean.
     * @return Boolean value represented by the input.
     * @throws EIOException If the input does not represent a
     * boolean.
     */
    public static boolean byteTreeToBoolean(ByteTree bt)
    throws EIOException {
        if (bt.value == null) {
            throw new EIOException("No data!");
        }
        if (bt.value.length != 1) {
            throw new EIOException("Wrong length!");
        }
        if (bt.value[0] == 0) {
            return false;
        } else if (bt.value[0] == 1) {
            return true;
        } else {
            throw new EIOException("Illegal byte value!");
        }
    }

    /**
     * Translates a <code>ByteTree</code> to a <code>boolean</code>
     * value if possible and throws an error otherwise. WARNING! This
     * method assumes that the input <code>ByteTree</code> is
     * correctly formed.
     *
     * @param bt Representation of the <code>boolean</code>.
     * @return Boolean value represented by the input.
     *
     * @throws EIOError If the input does not represent an
     * <code>boolean</code>.
     */
    public static boolean unsafeByteTreeToBoolean(ByteTree bt) {
        try {
            return byteTreeToBoolean(bt);
        } catch (EIOException bfe) {
            throw new EIOError("Fatal error!", bfe);
        }
    }

    /**
     * Translates an <code>int</code> value to a <code>ByteTree</code>
     * representation.
     *
     * @param n The <code>int</code> value.
     * @return Representation of <code>int</code> value.
     */
    public static ByteTree intToByteTree(int n) {
        byte[] intBytes = new byte[4];
        ExtIO.writeInt(intBytes, 0, n);
        return new ByteTree(intBytes);
    }

    /**
     * Translates a <code>ByteTree</code> to an <code>int</code> value
     * if possible and throws an exception otherwise.
     *
     * @param bt Representation of the <code>int</code>.
     * @return The <code>int</code> value represented by the input.
     * @throws EIOException If the input does not represent an
     * <code>int</code>.
     */
    public static int byteTreeToInt(ByteTree bt)
    throws EIOException {
        if (bt.value == null) {
            throw new EIOException("No data!");
        }
        if (bt.value.length != 4) {
            throw new EIOException("Wrong length!");
        }
        return ExtIO.readInt(bt.value, 0);
    }

    /**
     * Translates a <code>ByteTree</code> to an <code>int</code> value
     * if possible and throws an error otherwise. WARNING! This method
     * assumes that the input <code>ByteTree</code> is correctly
     * formed.
     *
     * @param bt Representation of the <code>int</code>.
     * @return The <code>int</code> value represented by the input.
     * @throws EIOError If the input does not represent an
     * <code>int</code>.
     */
    public static int unsafeByteTreeToInt(ByteTree bt) {
        try {
            return byteTreeToInt(bt);
        } catch (EIOException eioe) {
            throw new EIOError("Fatal error!", eioe);
        }
    }

    /**
     * Translates a <code>short</code> value to a
     * <code>ByteTree</code> representation.
     *
     * @param n <code>short</code> value.
     * @return Representation of <code>short</code> value.
     */
    public static ByteTree shortToByteTree(short n) {
        byte[] shortBytes = new byte[2];
        ExtIO.writeShort(shortBytes, 0, n);
        return new ByteTree(shortBytes);
    }

    /**
     * Translates a <code>ByteTree</code> to a <code>short</code>
     * value if possible and throws an exception otherwise.
     *
     * @param bt Representation of the <code>short</code>.
     * @return The <code>short</code> value represented by the input.
     * @throws EIOException If the input does not represent a
     * <code>short</code>.
     */
    public static short byteTreeToShort(ByteTree bt)
    throws EIOException {
        if (bt.value == null) {
            throw new EIOException("No data!");
        }
        if (bt.value.length != 2) {
            throw new EIOException("Wrong length!");
        }
        return ExtIO.readShort(bt.value, 0);
    }

    /**
     * Translates a <code>ByteTree</code> to a <code>short</code>
     * value if possible and throws an error otherwise. WARNING!  This
     * method assumes that the input <code>ByteTree</code> is
     * correctly formed.
     *
     * @param bt Representation of the <code>short</code>.
     * @return The <code>short</code> value represented by the input.
     * @throws EIOError If the input does not represent a
     * <code>short</code>.
     */
    public static short unsafeByteTreeToShort(ByteTree bt) {
        try {
            return byteTreeToShort(bt);
        } catch (EIOException eioe) {
            throw new EIOError("Fatal error!", eioe);
        }
    }

    /**
     * Translates an <code>int[]</code> into a <code>ByteTree</code>.
     *
     * @param array Array to be translated.
     * @return Representation of the input array.
     */
    public static ByteTree intArrayToByteTree(int[] array) {
        byte[] byteArray = new byte[4 * array.length];

        for (int j = 0, i = 0; i < array.length; j += 4, i++) {
            ExtIO.writeInt(byteArray, j, array[i]);
        }
        return new ByteTree(byteArray);
    }

    /**
     * Translates a <code>ByteTree</code> to an <code>int[]</code> if
     * possible and throws an exception otherwise.
     *
     * @param bt Representation of the <code>int[]</code>.
     * @return Array represented by the input.
     * @throws EIOException If the input does not represent a
     * <code>int[]</code>.
     */
    public static int[] byteTreeToIntArray(ByteTree bt)
    throws EIOException {
        if (bt.value == null) {
            throw new EIOException("No data!");
        }
        if (bt.value.length % 4 != 0) {
            throw new EIOException("Length is not multiple of 4!");
        }
        byte[] byteArray = bt.value;
        int[] array = new int[byteArray.length / 4];

        for (int j = 0, i = 0; i < array.length; j += 4, i++) {
            array[i] = ExtIO.readInt(byteArray, j);
        }
        return array;
    }

    /**
     * Translates a <code>boolean[]</code> into a
     * <code>ByteTree</code>.
     *
     * @param array Array to be translated.
     * @return Representation of the input array.
     */
    public static ByteTree booleanArrayToByteTree(boolean[] array) {
        byte[] byteArray = new byte[array.length];

        for (int i = 0; i < array.length; i++) {
            byteArray[i] = (byte)(array[i] ? 1 : 0);
        }
        return new ByteTree(byteArray);
    }

    /**
     * Translates a <code>ByteTree</code> to an <code>boolean[]</code>
     * if possible and throws an exception otherwise.
     *
     * @param bt Representation of the <code>int[]</code>.
     * @return Array represented by the input.
     * @throws EIOException If the input does not represent a
     * <code>boolean[]</code>.
     */
    public static boolean[] byteTreeToBooleanArray(ByteTree bt)
    throws EIOException {
        if (bt.value == null) {
            throw new EIOException("No data!");
        }
        byte[] byteArray = bt.value;
        boolean[] array = new boolean[byteArray.length];

        for (int i = 0; i < array.length; i++) {
            if (byteArray[i] == 0) {
                array[i] = true;
            } else if (byteArray[i] == 1) {
                array[i] = false;
            } else {
                throw new EIOException("Malformed array of booleans!");
            }
        }
        return array;
    }

    /**
     * Translates a <code>String</code> to its <code>ByteTree</code>
     * representation.
     *
     * @param s The <code>String</code> to be translated.
     * @return Representation of the <code>String</code>.
     */
    public static ByteTree stringToByteTree(String s) {
        try {
            return new ByteTree(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException uee) {
            throw new EIOError("This is a bug in the VM!", uee);
        }
    }

    /**
     * Translates a <code>ByteTree</code> to a <code>String</code>.
     *
     * @param bt Representation of the <code>String</code>.
     * @return <code>String</code> element represented by the input.
     * @throws EIOException If the input does not represent a
     * <code>String</code>.
     */
    public static String byteTreeToString(ByteTree bt)
    throws EIOException {
        if (bt.value == null) {
            throw new EIOException("No data!");
        }
        try {
            return new String(bt.value, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new EIOError("This is a bug in the VM!", uee);
        }
    }

    /**
     * Translates a <code>String[]</code> into a <code>ByteTree</code>.
     *
     * @param array Array to be translated.
     * @return Representation of the input array.
     */
    public static ByteTree stringArrayToByteTree(String[] array) {
        ByteTree[] byteTreeStrings = new ByteTree[array.length];

        for (int i = 0; i < array.length; i++) {
            byteTreeStrings[i] = ByteTree.stringToByteTree(array[i]);
        }
        return new ByteTree(byteTreeStrings);
    }

    /**
     * Translates a <code>ByteTree</code> to a <code>String</code>
     * array if this is possible and throws an exception if this is
     * not possible.
     *
     * @param bt Representation of the <code>String[]</code>.
     * @return Array represented by the input.
     * @throws EIOException If the input does not represent a
     * <code>String[]</code>.
     */
    public static String[] byteTreeToStringArray(ByteTree bt)
    throws EIOException {
        if (bt.children == null) {
            throw new EIOException("No children in ByteTree!");
        }
        String[] array = new String[bt.children.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = ByteTree.byteTreeToString(bt.children[i]);
        }
        return array;
    }


    // Documented in ByteTreeBasic.java

    public void update(Hashdigest digest) {
        byte[] prefix = new byte[5];
        if (value != null) {

            prefix[0] = ByteTree.LEAF;
            ExtIO.writeInt(prefix, 1, value.length);
            digest.update(prefix);
            digest.update(value);

        } else {

            prefix[0] = ByteTree.NODE;
            ExtIO.writeInt(prefix, 1, children.length);
            digest.update(prefix);
            for (int i = 0; i < children.length; i++) {
                children[i].update(digest);
            }

        }
    }
}

/**
 * We can not use {@link Integer}, since that class is immutable and
 * we need a container for a mutable integer.
 */
class IntContainer {
    int i;

    IntContainer(int i) {
        this.i = i;
    }
}
