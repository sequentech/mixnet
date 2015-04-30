
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

import vfork.ui.*;
import vfork.eio.*;

/**
 * Implements a combiner of random sources. If the sources are
 * independent, then the output is at least as hard to distinguish
 * from random as any of its underlying sources. The combiner simply
 * takes the xor of the random sources it combines.
 *
 * @author Douglas Wikstrom
 */
public class RandomSourceCombiner extends RandomSource {

    /**
     * Maximal number of sources that can be combined.
     */
    final static int MAX_RND_SOURCES = 50;

    /**
     * Instances wrapped by this one.
     */
    protected RandomSource[] randomSources;

    /**
     * Creates an instance.
     *
     * @param randomSources Instances wrapped by this one.
     */
    public RandomSourceCombiner(RandomSource ... randomSources) {
        this.randomSources = randomSources;
    }

    /**
     * Constructs an instance from the given representation.
     *
     * @param btr Representation of an instance.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public RandomSourceCombiner(ByteTreeReader btr)
        throws CryptoFormatException {
        try {

            int width = btr.getRemaining();
            if (width > MAX_RND_SOURCES) {
                throw new CryptoFormatException("Too many random sources!");
            }

            randomSources = new RandomSource[width];
            for (int i = 0; i < width; i++) {
                randomSources[i] =
                    Marshalizer.unmarshal_RandomSource(btr.getNextChild());
            }

        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        }
    }

    /**
     * Constructs an instance from the given representation.
     *
     * @param btr Representation of an instance.
     * @return Instance represented by the input.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public static RandomSourceCombiner newInstance(ByteTreeReader btr)
        throws CryptoFormatException {
        return new RandomSourceCombiner(btr);
    }

    // Documented in RandomSource.java

    public void getBytes(byte[] array) {
        randomSources[0].getBytes(array);

        // We xor the outputs of all the random sources.
        byte[] tempArray = new byte[array.length];
        for (int i = 1; i < randomSources.length; i++) {

            randomSources[i].getBytes(tempArray);

            for (int j = 0; j < array.length; j++) {
                array[j] ^= tempArray[j];
            }
        }
    }

    // Documented in Marshalizable.java

    public ByteTreeBasic toByteTree() {
        ByteTreeBasic[] byteTrees = new ByteTreeBasic[randomSources.length];
        for (int i = 0; i < byteTrees.length; i++) {
            byteTrees[i] = Marshalizer.marshal(randomSources[i]);
        }
        return new ByteTreeContainer(byteTrees);
    }

    public String humanDescription(boolean verbose) {
        StringBuilder sb = new StringBuilder();

        sb.append(Util.className(this, verbose) + "(");
        sb.append(randomSources[0].humanDescription(verbose));
        for (int i = 1; i < randomSources.length; i++) {
            sb.append(", " + randomSources[i].humanDescription(verbose));
        }
        sb.append(")");
        return sb.toString();
    }
}
