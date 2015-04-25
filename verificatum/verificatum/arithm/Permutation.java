
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

package verificatum.arithm;

import java.util.*;

import verificatum.crypto.*;
import verificatum.eio.*;

/**
 * Represents an immutable permutation which allows the permutation
 * and its inverse to be applied to arrays of elements.
 *
 * @author Douglas Wikstrom
 */
public class Permutation implements ByteTreeConvertible {

    /**
     * Permutation as a table.
     */
    protected int[] table;

    /**
     * Generates the identity permutation of a given size.
     *
     * @param numberOfElements Number of elements to permute.
     */
    public Permutation(int numberOfElements) {
        if (numberOfElements < 0) {
            throw new ArithmError("Negative number of elements!");
        }
        table = new int[numberOfElements];
        for (int i = 0; i < numberOfElements; i++) {
            table[i] = i;
        }
    }

    /**
     * Generates a random permutation of suitable size using the given
     * source of randomness.
     *
     * @param numberOfElements Number of elements to permute.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @param randomSource Source of randomness used to generate the
     * permutation.
     */
    public Permutation(int numberOfElements, RandomSource randomSource,
                       int statDist) {

        if (statDist < 0) {
            throw new ArithmError("Negative statistical distance parameter!");
        }

        int aprLog = MathExt.log2c(numberOfElements);

        // The union bound gives this overly conservative bit size.
        int bits = statDist + aprLog + aprLog;

        PermutationPair[] randomTable = new PermutationPair[numberOfElements];

        for (int i = 0; i < numberOfElements; i++) {
            randomTable[i] = new PermutationPair(i, bits, randomSource);
        }

        Arrays.sort(randomTable);

        table = new int[numberOfElements];
        for (int i = 0; i < numberOfElements; i++) {
            table[i] = randomTable[i].index;
        }
    }

    /**
     * Creates a permutation from the input representation.
     *
     * @param size Expected size of the permutation.
     * @param btr Representation of permutation.
     * @throws ArithmFormatException If the input is incorrect or has
     * wrong size.
     */
    public Permutation(int size, ByteTreeReader btr)
        throws ArithmFormatException {
        try {
            table = btr.readInts(size);

            // Verify that this is a permutation.
            boolean[] temp = new boolean[table.length];
            Arrays.fill(temp, false);

            for (int i = 0; i < temp.length; i++) {
                if (!(0 <= table[i] && table[i] < size)) {
                    throw new ArithmFormatException("Index outside interval!");
                }
                if (temp[table[i]]) {
                    throw new ArithmFormatException("Index is reused!");
                }
                temp[table[i]] = true;
            }
        } catch (EIOException eioe) {
            throw new ArithmFormatException("Not an integer array!", eioe);
        }
    }

    /**
     * Creates the permutation corresponding to the input table. It is
     * the responsibility of the programmer to make sure that the
     * input is indeed a permutation table.
     *
     * @param permutationTable Permutation table.
     */
    protected Permutation(int[] permutationTable) {
        table = Arrays.copyOf(permutationTable, permutationTable.length);
    }

    /**
     * Returns the inverse of this permutation.
     *
     * @return Inverse of this permutation.
     */
    public Permutation inv() {
        int[] invtable = new int[table.length];
        for (int i = 0; i < table.length; i++) {
            invtable[table[i]] = i;
        }
        return new Permutation(invtable);
    }

    /**
     * Creates a permutation from the input representation. This
     * should only be used if we know that the input is correct.
     *
     * @param size Expected size of the permutation.
     * @param btr Representation of permutation.
     * @return Permutation represented by the input.
     *
     * @throws ArithmError If the input is incorrect or has wrong
     * size.
     */
    public static Permutation unsafePermutation(int size,
                                                ByteTreeReader btr) {
        try {
            return new Permutation(size, btr);
        } catch (ArithmFormatException afe) {
            throw new ArithmError("Fatal error!", afe);
        }
    }

    /**
     * Returns a representation of this instance.
     *
     * @return Representation of this instance.
     */
    public ByteTree toByteTree() {
        return ByteTree.intArrayToByteTree(table);
    }

    /**
     * Returns the number of permuted elements.
     *
     * @return Number of permuted elements.
     */
    public int size() {
        return table.length;
    }

    /**
     * Returns the image of the input under the permutation.
     *
     * @param index Integer to map.
     * @return Image of <code>index</code>.
     */
    public int map(int index) {
        return table[index];
    }

    /**
     * Applies this permutation to the elements in the first input
     * array and stores the result in the second input. The arrays
     * must have the same size as this permutation.
     *
     * @param array Array to be permuted.
     * @param permutedArray Stores the result.
     */
    public void applyPermutation(Object[] array,
                                 Object[] permutedArray) {
        if (array.length != table.length
            || permutedArray.length != table.length) {
            throw new ArithmError("Wrong or different lengths!");
        }
        for (int i = 0; i < table.length; i++) {
            permutedArray[table[i]] = array[i];
        }
    }

    /**
     * Returns <code>true</code> or <code>false</code> depending on if
     * the input represents the same permutation as this instance or
     * not.
     *
     * @param p Permutation to compare with.
     * @return <code>true</code> or <code>false</code> depending on if
     * the input permutation is equal to this permutation or not.
     */
    public boolean equals(Object p) {
        return p instanceof Permutation
            && Arrays.equals(table, ((Permutation)p).table);
    }

    /**
     * Returns a permutation representing this permutation but shrunk
     * to the given size.
     *
     * @param size Size of new permutation.
     * @return A shrunken permutation.
     */
    public Permutation shrink(int size) {
        int[] tmpTable = new int[table.length];
        Arrays.fill(tmpTable, -1);

        for (int i = 0; i < size; i++) {
            tmpTable[map(i)] = i;
        }

        int[] newTable = new int[size];

        int l = 0;
        for (int i = 0; i < tmpTable.length; i++) {
            if (tmpTable[i] != -1) {
                newTable[tmpTable[i]] = l;
                l++;
            }
        }

        return new Permutation(newTable);
    }
}

class PermutationPair implements Comparable {
    LargeInteger randomPrefix;
    int index;

    PermutationPair(int index, int bits, RandomSource randomSource) {
        this.randomPrefix = new LargeInteger(bits, randomSource);
        this.index = index;
    }

    public int compareTo(Object permutationPair) {
        return randomPrefix.
            compareTo(((PermutationPair)permutationPair).randomPrefix);
    }
}
