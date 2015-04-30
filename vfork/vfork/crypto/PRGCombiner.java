
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

import java.util.*;

import vfork.ui.*;
import vfork.eio.*;

/**
 * Implements a combiner of pseudo-random generators (PRG). If the
 * PRGs are independent, then the output is at least as hard to
 * distinguish from random as any of its underlying PRGs. The combiner
 * simply takes the xor of the PRGs it combines.
 *
 * <p>
 *
 * This class allows combining instances of <code>RandomSource</code>
 * as well.
 *
 * @author Douglas Wikstrom
 */
public class PRGCombiner extends PRG {

    /**
     * Underlying combiner.
     */
    protected RandomSourceCombiner combiner;

    /**
     * Constructs an instance from the given representation.
     *
     * @param btr Representation of an instance.
     * @return Instance represented by the input.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public static PRGCombiner newInstance(ByteTreeReader btr)
        throws CryptoFormatException {
        return new PRGCombiner(new RandomSourceCombiner(btr));
    }

    /**
     * Creates an instance with the given underlying combiner.
     *
     * @param combiner Underlying combiner.
     */
    protected PRGCombiner(RandomSourceCombiner combiner) {
        this.combiner = combiner;
    }

    /**
     * Creates an instance.
     *
     * @param randomSources Instances wrapped by this one.
     */
    public PRGCombiner(RandomSource ... randomSources) {
        combiner = new RandomSourceCombiner(randomSources);
    }

    /**
     * Constructs an instance from the given representation.
     *
     * @param btr Representation of an instance.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public PRGCombiner(ByteTreeReader btr)
        throws CryptoFormatException {
        combiner = new RandomSourceCombiner(btr);
    }


    // Documented in PRG.java

    public void setSeed(byte[] seed) {

        if (seed.length >= minNoSeedBytes()) {
            int offset = 0;
            for (int i = 0; i < combiner.randomSources.length; i++) {

                if (combiner.randomSources[i] instanceof PRG) {

                    int end = offset +
                        ((PRG)combiner.randomSources[i]).minNoSeedBytes();

                    byte[] tmpSeed =
                        Arrays.copyOfRange(seed, offset, end);

                    ((PRG)combiner.randomSources[i]).setSeed(tmpSeed);
                    offset += tmpSeed.length;
                }
            }
        } else {
            throw new CryptoError("Seed is too short!");
        }
    }

    public int minNoSeedBytes() {
        int total = 0;
        for (int i = 0; i < combiner.randomSources.length; i++) {
            if (combiner.randomSources[i] instanceof PRG) {
                total += ((PRG)combiner.randomSources[i]).minNoSeedBytes();
            }
        }
        return total;
    }

    // Documented in RandomSource.java

    public void getBytes(byte[] array) {
        combiner.getBytes(array);
    }

    // Documented in Marshalizable.java

    public ByteTreeBasic toByteTree() {
        return combiner.toByteTree();
    }

    public String humanDescription(boolean verbose) {
        StringBuilder sb = new StringBuilder();

        sb.append(Util.className(this, verbose) + "(");
        sb.append(combiner.randomSources[0].humanDescription(verbose));
        for (int i = 1; i < combiner.randomSources.length; i++) {
            sb.append(", " +
                      combiner.randomSources[i].humanDescription(verbose));
        }
        sb.append(")");

        return sb.toString();
    }
}
