
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

package vfork.crypto;

import java.io.*;

import vfork.eio.*;
import vfork.ui.*;

/**
 * Wrapper class for random sources implemented as random devices in
 * the operating system. This could be a physical source of true
 * randomness, or some heuristically secure source such as
 * <code>/dev/urandom</code> on some Un*xes. It is assumed that the
 * underlying source contains sufficiently many bytes.
 *
 * <p>
 *
 * <b>WARNING!</b> It is prudent to <b>never</b> use a normal file as a
 * random device, due to the risk of reuse.
 *
 * <p>
 *
 * <b>WARNING!<b> Do not create too many instances of this class,
 * since there is no way to release the underlying file descriptor
 * with certainty.
 *
 * @author Douglas Wikstrom
 */
public class RandomDevice extends RandomSource {

    /**
     * Random device from where we read random bits.
     */
    protected File file;

    /**
     * Stream from the random device.
     */
    protected FileInputStream fis;

    /**
     * Constructs an instance corresponding to the input.
     *
     * @param btr Representation of an instance.
     * @return Random device represented by the input.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public static RandomDevice newInstance(ByteTreeReader btr)
        throws CryptoFormatException {
        return new RandomDevice(btr);
    }

    /**
     * Turns a file into an input stream.
     *
     * @param file File representing the random device.
     */
    protected void setupDevice(File file) {
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException fnfe) {
            throw new CryptoError("File not found!", fnfe);
        } catch (SecurityException se) {
            throw new CryptoError("Not allowed to open device!", se);
        }
    }

    /**
     * Returns an instance that extracts its output from
     * <code>/dev/urandom</code>.
     */
    public RandomDevice() {
        this(new File("/dev/urandom"));
    }

    /**
     * Constructs an instance reading from the given random device.
     *
     * @param file Path to a random device.
     */
    public RandomDevice(File file) {
        this.file = file;
        setupDevice(file);
    }

    /**
     * Constructs an instance reading from a device described in the
     * input.
     *
     * @param btr Representation of a random device.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public RandomDevice(ByteTreeReader btr) throws CryptoFormatException {
        try {
            String path = btr.readString();
            this.file = new File(path);
            setupDevice(file);
        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        } catch (CryptoError ce) {
            throw new CryptoFormatException("Unable to create device!", ce);
        }
    }

    // Documented in vfork.crypto.RandomSource.

    public void getBytes(byte[] array) {
        try {
            int index = 0;
            int len = array.length;

            while (index < len) {
                index += fis.read(array, index, len - index);
            }
        } catch (IOException ioe) {
            throw new CryptoError("Unable to read from random device!", ioe);
        }
    }

    // Documented in Marshalizable.java

    public ByteTree toByteTree() {
        return new ByteTree(file.getPath().getBytes());
    }

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) + "(" + this.file.toString() + ")";
    }
}
