
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

package vfork.arithm;

import java.io.*;

import vfork.crypto.*;
import vfork.eio.*;
import vfork.util.*;

/**
 * Implements an "array" of immutable group elements of a {@link
 * ModPGroup}. This is a wrapper of {@link LargeIntegerArray} in the
 * same way as {@link ModPGroupElement} is a wrapper of {@link
 * LargeInteger}.
 *
 * @author Douglas Wikstrom
 */
public class ModPGroupElementArray extends PGroupElementArray {

    /**
     * Stores canonical representatives of the group elements of this
     * instance.
     */
    public LargeIntegerArray values;

    /**
     * Constructs an instance over the given group and with the group
     * elements derived from the given array of integers. it is the
     * responsibility of the programmer to ensure that all integers
     * are canonical representatives of group elements.
     *
     * @param pGroup Group to which the elements in this instance
     * belong.
     * @param values Integer representatives of group elements.
     */
    protected ModPGroupElementArray(PGroup pGroup, LargeIntegerArray values) {
        super(pGroup);
        this.values = values;
    }

    /**
     * Constructs an instance over the given group and with the given
     * group elements.
     *
     * @param pGroup Group to which the elements in this instance
     * belong.
     * @param elements Group elements.
     */
    protected ModPGroupElementArray(ModPGroup pGroup,
                                    PGroupElement[] elements) {
        super(pGroup);

        LargeInteger[] integers = pGroup.toLargeIntegers(elements);
        this.values = LargeIntegerArray.toLargeIntegerArray(integers);
    }

    /**
     * Constructs the concatenation of the inputs.
     *
     * @param pGroup Group to which the elements in this instance
     * belong.
     * @param elements Group elements.
     */
    protected ModPGroupElementArray(ModPGroup pGroup,
                                    PGroupElementArray ... arrays) {
        super(pGroup);

        LargeIntegerArray[] allValues = new LargeIntegerArray[arrays.length];

        for (int i = 0; i < allValues.length; i++) {
            if (!pGroup.equals(arrays[i].getPGroup())) {
                throw new ArithmError("Mismatching groups!");
            }
            allValues[i] = ((ModPGroupElementArray)arrays[i]).values;
        }
        this.values = LargeIntegerArray.toLargeIntegerArray(allValues);
    }


    /**
     * Creates an instance containing the given number of copies of
     * the input element.
     *
     * @param pGroup Group to which the elements in this instance
     * belong.
     * @param size Number of elements to generate.
     * @param element Element to use for all components of the the
     * resulting array.
     */
    protected ModPGroupElementArray(PGroup pGroup, int size,
                                    PGroupElement element) {
        super(pGroup);
        values = LargeIntegerArray.fill(size,
                                        ((ModPGroupElement)element).value);
    }

    /**
     * Constructs an instance over the given group and derives
     * "heuristically random" group elements using a random oracle
     * based on the given hashfunction.
     *
     * @param pGroup Group to which the elements in this instance
     * belong.
     * @param size Number of elements to generate.
     * @param randomSource Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    protected ModPGroupElementArray(PGroup pGroup,
                                    int size,
                                    RandomSource randomSource,
                                    int statDist) {
        super(pGroup);
        LargeInteger modulus = ((ModPGroup)pGroup).modulus;
        int bitLength = modulus.bitLength() + statDist;

        LargeIntegerArray lia = LargeIntegerArray.random(size,
                                                         modulus,
                                                         statDist,
                                                         randomSource);
        values = lia.modPow(((ModPGroup)pGroup).coOrder, modulus);
        lia.free();
    }

    /**
     * Constructs an instance from the given representation.
     *
     * @param pGroup Group to which the elements in this instance belong.
     * @param size Expected number of elements in array. If this is
     * set to zero, then the input representation can have any size.
     * @param btr Representation of an instance.
     *
     * @throws ArithmFormatException If the contents of the iterator
     * are incorrectly formatted.
     */
    protected ModPGroupElementArray(ModPGroup pGroup, int size,
                                    ByteTreeReader btr)
        throws ArithmFormatException {
        super(pGroup);

        LargeInteger modulus = ((ModPGroup)pGroup).modulus;

        values = LargeIntegerArray.
            toLargeIntegerArray(size, btr, LargeInteger.ONE, modulus);

        if (!values.quadraticResidues(modulus)) {
            throw new ArithmFormatException("Quadratic non-residue!");
        }
    }

    // Documented in PGroupElementArray.java

    public PGroupElementIterator getIterator() {
	return new ModPGroupElementIterator((ModPGroup)pGroup,
					    values.getIterator());
    }

    public String toString() {
        return values.toString();
    }

    public ByteTreeBasic toByteTree() {
        return values.toByteTree(((ModPGroup)pGroup).modulusByteLength);
    }

    public PGroupElementArray mul(PGroupElementArray factors) {
        if (!pGroup.equals(factors.pGroup)) {
            throw new ArithmError("Distinct groups!");
        }
        LargeIntegerArray res =
            values.modMul(((ModPGroupElementArray)factors).values,
                          ((ModPGroup)pGroup).modulus);
        return new ModPGroupElementArray(pGroup, res);
    }

    public PGroupElementArray inv() {
        try {
            LargeIntegerArray inverses =
                values.modInv(((ModPGroup)pGroup).modulus);
            return new ModPGroupElementArray(pGroup, inverses);
        } catch (ArithmException ae) {
            throw new ArithmError("This is a bug!", ae);
        }
    }

    public PGroupElementArray exp(PRingElementArray exponents) {
        if (!pGroup.pRing.equals(exponents.pRing)) {
            throw new ArithmError("Distinct groups!");
        }
        return exp(((PFieldElementArray)exponents).values);
    }

    /**
     * Takes this instance to the element-wise power of the input
     * integers.
     *
     * @param exponents Exponents used to take powers.
     * @return Array of results.
     */
    public PGroupElementArray exp(LargeIntegerArray exponents) {
        LargeIntegerArray res =
            values.modPow(exponents, ((ModPGroup)pGroup).modulus);
        return new ModPGroupElementArray(pGroup, res);

    }

    public PGroupElementArray exp(PRingElement exponent) {
        if (!pGroup.pRing.equals(exponent.pRing)) {
            throw new ArithmError("Distinct groups!");
        }
        return exp(((PFieldElement)exponent).value);
   }

    /**
     * Takes this instance to the element-wise power of the input
     * integer.
     *
     * @param exponent Exponent used to take powers.
     * @return Array of results.
     */
    public PGroupElementArray exp(LargeInteger exponent) {
        LargeIntegerArray res =
            values.modPow(exponent, ((ModPGroup)pGroup).modulus);
        return new ModPGroupElementArray(pGroup, res);
    }

    public PGroupElement expProd(PRingElementArray exponents) {
        if (!pGroup.pRing.equals(exponents.pRing)) {
            throw new ArithmError("Distinct groups!");
        }
        return expProd(((PFieldElementArray)exponents).values);
    }

    /**
     * Takes this instance to the product power of the input integers.
     *
     * @param exponents Exponents used to take powers.
     * @return Resulting product power.
     */
    public PGroupElement expProd(LargeIntegerArray exponents) {
        LargeInteger li =
            values.modPowProd(exponents, ((ModPGroup)pGroup).modulus);
        return new ModPGroupElement((ModPGroup)pGroup, li);
    }

    public PGroupElement prod() {
        LargeInteger li =
            values.modProd(((ModPGroup)pGroup).modulus);
        return new ModPGroupElement((ModPGroup)pGroup, li);
    }

    public PGroupElement get(int index) {
        return new ModPGroupElement((ModPGroup)pGroup, values.get(index));
    }

    public int compareTo(PGroupElementArray array) {
        if (array instanceof ModPGroupElementArray) {
            ModPGroupElementArray modarray = (ModPGroupElementArray)array;
            if (modarray.pGroup.equals(pGroup)) {
                return values.compareTo(modarray.values);
            }
        }
        throw new ArithmError("Illegal comparison!");
    }

    public boolean equals(Object bArray) {
        if (this == bArray) {
            return true;
        }
        if (!(bArray instanceof ModPGroupElementArray)) {
            return false;
        }

        ModPGroupElementArray array = (ModPGroupElementArray)bArray;
        return values.equals(array.values) && pGroup.equals(array.pGroup);
    }

    public boolean[] equalsAll(PGroupElementArray bArray) {
        return values.equalsAll(((ModPGroupElementArray)bArray).values);
    }

    public int size() {
        return values.size();
    }

    public PGroupElementArray permute(Permutation permutation) {
        LargeIntegerArray plia = values.permute(permutation);
        return new ModPGroupElementArray(pGroup, plia);
    }

    public PGroupElementArray shiftPush(PGroupElement el) {
        LargeIntegerArray sia = values.shiftPush(((ModPGroupElement)el).value);
        return new ModPGroupElementArray(pGroup, sia);
    }

    public PGroupElementArray copyOfRange(int startIndex, int endIndex) {
        LargeIntegerArray cia = values.copyOfRange(startIndex, endIndex);
        return new ModPGroupElementArray(pGroup, cia);
    }

    public PGroupElementArray extract(boolean[] valid) {
        return new ModPGroupElementArray(pGroup, values.extract(valid));
    }

    public PGroupElement[] elements() {
        return ((ModPGroup)pGroup).toElements(values.integers());
    }

    public void free() {
        values.free();
        values = null;
    }
}
