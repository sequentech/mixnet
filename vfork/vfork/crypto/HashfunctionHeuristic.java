
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
import java.security.*;

import vfork.eio.*;
import vfork.ui.*;

/**
 * Implements a wrapper for standardized hashfunctions. Currently,
 * this means the SHA-2 family of hashfunctions.
 *
 * @author Douglas Wikstrom
 */
public class HashfunctionHeuristic implements Hashfunction {

    /**
     * Maximal byte length of an algorithm name.
     */
    protected static final int MAX_ALGORITHM_BYTELENGTH = 100;

    /**
     * Name of underlying algorithm.
     */
    protected String algorithm;

    /**
     * Underlying hash digest.
     */
    protected MessageDigest md;

    /**
     * Length of output.
     */
    protected int outputLength;

    /**
     * Constructs an instance corresponding to the input.
     *
     * @param btr Representation of an instance.
     * @return Hashfunction represented by the input.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public static HashfunctionHeuristic newInstance(ByteTreeReader btr)
        throws CryptoFormatException {
        try {

            if (btr.getRemaining() > MAX_ALGORITHM_BYTELENGTH) {
                throw new CryptoFormatException("Algorithm name is too long!");
            }
            return new HashfunctionHeuristic(btr.readString());

        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        } catch (CryptoError ce) {
            throw new CryptoFormatException("Unable to interpret!", ce);
        }
    }

    /**
     * Creates an instance of a given heuristic hashfunction. The
     * supported algorithms are <code>SHA-256</code>,
     * <code>SHA-384</code>, and <code>SHA-512</code>.
     *
     * @param algorithm Name of algorithm.
     */
    public HashfunctionHeuristic(String algorithm) {
        this.algorithm = algorithm;
        if (algorithm.equals("SHA-256")) {
            outputLength = 256;
        } else if (algorithm.equals("SHA-384")) {
            outputLength = 384;
        } else if (algorithm.equals("SHA-512")) {
            outputLength = 512;
        } else {
            throw new CryptoError("Unsupported algorithm!");
        }
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException nsae) {
            throw new CryptoError("Unsupported algorithm!", nsae);
        }
    }

    // Documented in Hashfunction.java

    public Hashdigest getDigest() {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return new HashdigestHeuristic(md);
        } catch (NoSuchAlgorithmException nsae) {
            throw new CryptoError("Unsupported algorithm!", nsae);
        }
    }

    // Apparently Sun did not make a thread safe implementation of
    // SHA-2. Thus, we need to add some synchronization here.
    public synchronized byte[] hash(byte[] ... datas) {
        md.reset();

        for (int i = 0; i < datas.length; i++) {
            md.update(datas[i]);
        }
        return md.digest();
    }

    public int getOutputLength() {
        return outputLength;
    }

    /**
     * Returns a string representation of this instance. This is
     * useful for debugging.
     */
    public String toString() {
        return algorithm;
    }

    // Documented in Marshalizable.java

    public ByteTree toByteTree() {
        try {
            return new ByteTree(algorithm.getBytes("UTF8"));
        } catch (UnsupportedEncodingException uee) {
            throw new CryptoError("This should never happen!", uee);
        }

    }

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(" + algorithm + ")";
    }
}