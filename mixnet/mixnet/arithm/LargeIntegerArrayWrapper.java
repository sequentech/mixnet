
/*
 * Copyright 2011 Eduardo Robles Elvira <edulix AT wadobo DOT com>
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

package mixnet.arithm;

import java.io.*;
import java.math.*;
import java.util.*;

import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.util.*;

/**
 * Wrapper for methods in class that inherit
 * <code>LargeIntegerArray</code>, so that
 * <code>LargeIntegerArray</code> can directly call the methods using
 * one instance of a class inheriting this wrapper.
 *
 * @author Eduardo Robles Elvira
 */
public abstract class LargeIntegerArrayWrapper {

    /**
     * Creates an instance from the given integers.
     *
     * @param integers Integers contained in the returned instance.
     */
    public abstract LargeIntegerArray create(LargeInteger[] integers);

    /**
     * Creates an instance from the given integers.
     *
     * @param arrays Integers to be contained in the returned
     * instance.
     */
    // TODO(this is not safe for very large file based arrays)
    public LargeIntegerArray create(LargeIntegerArray ... arrays) {

        int total = 0;
        for (int i = 0; i < arrays.length; i++) {
            total += arrays[i].size();
        }

        LargeInteger[] res = new LargeInteger[total];

        int offset = 0;
        for (int i = 0; i < arrays.length; i++) {
            int len = arrays[i].size();
            System.arraycopy(arrays[i].integers(), 0,
                             res, offset,
                             len);
            offset += len;
        }
        return create(res);
    }

    /**
     * Returns an instance containing the integers in the input. This
     * requires that each integer falls into the given interval, but
     * also that the representation of each integer is of equal size
     * to the byte tree representation of the upper bound.
     *
     * @param size Expected number of elements in array.
     * @param btr Representation of an instance.
     * @param lb Non-negative inclusive lower bound for integers.
     * @param ub Positive exclusive upper bound for integers.
     * @return Instance containing the integers read from the
     * iterator.
     *
     * @throws ArithmFormatException If the data on file can not be
     * parsed as a large integer array.
     */
    public abstract LargeIntegerArray create(int size,
                                             ByteTreeReader btr,
                                             LargeInteger lb,
                                             LargeInteger ub)
        throws IOException, EIOException, ArithmFormatException;

    /**
     * Generates an array of random integers modulo the given modulus.
     *
     * @param size Number of integers to generate.
     * @param modulus Modulus.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @param randomSource Source of random bits used to initialize
     * the array.
     * @return Array of random integers.
     */
    public abstract LargeIntegerArray create(int size,
                                             LargeInteger modulus,
                                             int statDist,
                                             RandomSource randomSource);

    /**
     * Returns an instance containing the given number random
     * integers of the given bit length.
     *
     * @param size Number of integers to generate.
     * @param bitLength Bit length of random integers.
     * @param randomSource Source of randomness.
     * @return Instance containing the random integers.
     */
    public abstract LargeIntegerArray create(int size,
                                             int bitLength,
                                             RandomSource randomSource);

    /**
     * Returns an instance containing the a number of copies of the
     * given integer value.
     *
     * @param size Number of copies of the integers.
     * @param value Integer value to be copied.
     * @return Instance containing the integers.
     */
    public abstract LargeIntegerArray create(int size, LargeInteger value);
}
