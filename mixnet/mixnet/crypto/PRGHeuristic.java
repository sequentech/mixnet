
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

package mixnet.crypto;

import java.util.*;
import java.util.Random;
import java.security.*;

import mixnet.eio.*;
import mixnet.ui.*;

/**
 * Pseudo-random generator based on a hashfunction evaluated on a seed
 * concatenated with a counter, i.e., a natural construction of a PRG
 * from a pseudo-random function.
 *
 * @author Douglas Wikstrom
 */
public class PRGHeuristic extends PRG {

    /**
     * Underlying hashfunction.
     */
    protected Hashfunction hashfunction;

    /**
     * Buffer holding the seed and the counter as bytes.
     */
    protected byte[] input;

    /**
     * Counter used as input to the hashfunction. This is increased
     * with each invocation of the hashfunction.
     */
    protected int counter;

   /**
     * Data to be output.
     */
    protected byte[] data;

    /**
     * Index to first unused byte in data.
     */
    protected int datapos;

    /**
     * Constructs an instance corresponding to the input.
     *
     * @param btr Instructions for construction of an instance.
     * @return PRG represented by the input.
     *
     * @throws CryptoFormatException This is never thrown, but
     * declared for consistency with the other PRGs.
     */
    public static PRGHeuristic newInstance(ByteTreeReader btr)
    throws CryptoFormatException {
        try {
            Hashfunction hashfunction =
                Marshalizer.unmarshal_Hashfunction(btr);

            return new PRGHeuristic(hashfunction);

        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed representation!", eioe);
        }
    }

    /**
     * Creates an instance using the given hashfunction.
     *
     * @param hashfunction Hashfunction on which the generator is
     * based.
     */
    public PRGHeuristic(Hashfunction hashfunction) {
        this.hashfunction = hashfunction;
        input = new byte[minNoSeedBytes() + 4];
    }

    /**
     * Creates a seeded instance of the PRG based on SHA-256.
     *
     * @param seed Seed to the generator.
     */
    public PRGHeuristic(byte[] seed) {
        this(new HashfunctionHeuristic("SHA-256"));
        setSeed(seed);
    }

    /**
     * Creates an unseeded, i.e., completely insecure, instance of the
     * PRG based on SHA-256.
     */
    public PRGHeuristic() {
        this(new byte[64]);
    }

    /**
     * Set the seed of the generator. If the input contains more bytes
     * than needed then they are xor-ed in a circular fashion.
     *
     * @param seed Seed to generator.
     */
    public void setSeed(byte[] seed) {
        Arrays.fill(input, (byte)0);
        counter = 0;
        for (int i = 0; i < seed.length; i++) {
            input[i % minNoSeedBytes()] ^= seed[i];
        }
        data = new byte[hashfunction.getOutputLength()];
        datapos = data.length;
    }


    // Documented in mixnet.crypto.RandomSource.java.

    public void getBytes(byte[] array) {

        int index = 0;

        while (index < array.length) {

            int len = Math.min(array.length - index, data.length - datapos);

            if (len > 0) {

                // Copy available output bytes.
                System.arraycopy(data, datapos, array, index, len);
                datapos += len;
                index += len;

            } else {

                // Plug in the current counter.
                int i = minNoSeedBytes();
                input[i++] = (byte)(counter >>> 24 & 0xff);
                input[i++] = (byte)(counter >>> 16 & 0xff);
                input[i++] = (byte)(counter >>> 8 & 0xff);
                input[i++] = (byte)(counter & 0xff);

                // Hash the updated input and update counters.
                data = hashfunction.hash(input);
                datapos = 0;
                counter++;
            }
        }
    }

    public int minNoSeedBytes() {
        return hashfunction.getOutputLength() / 8;
    }

    // Documented in Marshalizable.java

    public ByteTreeBasic toByteTree() {
        return Marshalizer.marshal(hashfunction);
    }

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(" + hashfunction.humanDescription(verbose) + ")";
    }
}
