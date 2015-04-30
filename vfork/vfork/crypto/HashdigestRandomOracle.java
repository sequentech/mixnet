
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

import java.security.*;
import java.util.*;

import vfork.arithm.*;
import vfork.eio.*;

/**
 * Digest for a random oracle.
 *
 * @author Douglas Wikstrom
 */
public class HashdigestRandomOracle implements Hashdigest {

    /**
     * Underlying hashfunction.
     */
    protected Hashfunction hashfunction;

    /**
     * Message digest wrapped by this instance.
     */
    protected Hashdigest hd;

    /**
     * Output bit length.
     */
    protected int outputLength;

    /**
     * Creates an instance using the given hashfunction and with the
     * given output bit length.
     *
     * @param hashfunction Underlying hashfunction.
     * @param outputLength Output bit length.
     */
    public HashdigestRandomOracle(Hashfunction hashfunction, int outputLength) {

        this.hashfunction = hashfunction;
        this.outputLength = outputLength;

        byte[] prefix = new byte[4];
        ExtIO.writeInt(prefix, 0, outputLength);

        this.hd = hashfunction.getDigest();
        hd.update(prefix);
    }

    // Documented in Hashdigest.java

    public void update(byte[] ... data) {
        hd.update(data);
    }

    public void update(byte[] data, int offset, int length) {
        hd.update(data, offset, length);
    }

    public byte[] digest() {

        PRGHeuristic prg = new PRGHeuristic(hashfunction);

        byte[] seed = hd.digest();

        prg.setSeed(seed);

        int len = (outputLength + 7) / 8;

        byte[] res = prg.getBytes(len);

        if (outputLength % 8 != 0) {
            res[0] = (byte)(res[0] & (0xFF >>> (8 - (outputLength % 8))));
        }

        return res;
    }
}