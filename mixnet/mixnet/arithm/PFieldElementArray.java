
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

package mixnet.arithm;

import java.io.*;

import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.util.*;

/**
 * Implements an array of field elements, i.e., elements {@link
 * PFieldElement} from {@link PField}. This is essentially a wrapper
 * of {@link LargeIntegerArray} in the same way that {@link
 * PFieldElement} is a wrapper of {@link LargeInteger}.
 *
 * @author Douglas Wikstrom
 */
public class PFieldElementArray extends PRingElementArray {

    /**
     * Field to which the elements of this array belong.
     */
    PField pField;

    /**
     * Representatives of the field elements.
     */
    protected LargeIntegerArray values;

    /**
     * Constructs an instance with the given elements. This assumes
     * that the inputs are canonically reduced.
     *
     * @param pField Field to which the elements of this array belong.
     * @param elements Field elements.
     */
    protected PFieldElementArray(PField pField, PRingElement[] elements) {
        super(pField);
        this.pField = pField;
        values = LargeIntegerArray.
            toLargeIntegerArray(pField.toLargeIntegers(elements));
    }

    /**
     * Constructs an array with the elements corresponding to the
     * given representatives.
     *
     * <p>
     *
     * WARNING! Assumes that the input values are in the right
     * interval. The input array is not copied.
     *
     * @param pField Field to which the elements of this array belong.
     * @param values Representatives of the field elements.
     */
    protected PFieldElementArray(PField pField, LargeIntegerArray values) {
        super(pField);
        this.pField = pField;
        this.values = values;
    }

    /**
     * Constructs an instance from the given representation.
     *
     * @param pField Field to which the elements of this array belong.
     * @param size Expected number of elements in array.
     * @param btr Representation of an instance.
     *
     * @throws ArithmFormatException If the contents of the file are
     * incorrectly formatted.
     */
    protected PFieldElementArray(PField pField, int size, ByteTreeReader btr)
        throws ArithmFormatException {
        super(pField);
        this.pField = pField;
        values = LargeIntegerArray.toLargeIntegerArray(size,
                                                       btr,
                                                       LargeInteger.ZERO,
                                                       pField.getOrder());
    }

    /**
     * Constructs an instance with the given number of randomly chosen
     * elements.
     *
     * @param pField Field to which the elements of this array belong.
     * @param size Number of elements to generate.
     * @param randomSource Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    protected PFieldElementArray(PField pField,
                                 int size,
                                 RandomSource randomSource,
                                 int statDist) {
        super(pField);
        this.pField = pField;
        this.values = LargeIntegerArray.random(size,
                                               pField.getOrder(),
                                               statDist,
                                               randomSource);
    }

    /**
     * Returns an array of canonical integer representatives of the
     * elements of this instance.
     *
     * @return Array of canonical integer representatives of the
     * elements of this instance.
     */
    public LargeIntegerArray toLargeIntegerArray() {
        return values;
    }


    // Documented in PRingElementArray.java

    public String toString() {
        return values.toString();
    }

    public PFieldElementArray add(PRingElementArray terms) {
        if (pRing.equals(terms.getPRing())) {
            LargeIntegerArray newValues =
                values.modAdd(((PFieldElementArray)terms).values, pField.order);
            return new PFieldElementArray(pField, newValues);
        }
        throw new ArithmError("Mismatching fields!");
    }

    public PRingElementArray neg() {
        return new PFieldElementArray(pField, values.modNeg(pField.order));
    }

    public PFieldElementArray mul(PRingElement factor) {
        if (pRing.equals(factor.getPRing())) {
            LargeInteger integerFactor = ((PFieldElement)factor).value;
            return new PFieldElementArray(pField,
                                          values.modMul(integerFactor,
							pField.order));
        }
        throw new ArithmError("Mismatching fields!");
    }

    public PFieldElementArray mul(PRingElementArray factors) {
        if (pRing.equals(factors.getPRing())) {
            LargeIntegerArray integers = ((PFieldElementArray)factors).values;
            return new PFieldElementArray(pField,
                                          values.modMul(integers,
                                                        pField.order));
        }
        throw new ArithmError("Mismatching fields!");
    }

    public PRingElementArray inv() throws ArithmException {
        return new PFieldElementArray(pField, values.modInv(pField.order));
    }

    public PFieldElement innerProduct(PRingElementArray vector) {
        if (pRing.equals(vector.getPRing())) {
            LargeInteger res =
                values.modInner(((PFieldElementArray)vector).values,
                                pField.order);
            return new PFieldElement(pField, res);
        }
        throw new ArithmError("Mismatching fields!");
    }

    public PFieldElement sum() {
        return pField.toElement(values.modSum(pField.order));
    }

    public PFieldElement prod() {
        return pField.toElement(values.modProd(pField.order));
    }

    public PRingElementArray prods() {
        return pField.unsafeToElementArray(values.modProds(pField.order));
    }

    public int size() {
        return values.size();
    }

    public PFieldElementArray permute(Permutation permutation) {
        LargeIntegerArray plia = values.permute(permutation);
        return new PFieldElementArray(pField, plia);
    }

    public Pair<PRingElementArray,PRingElement>
        recLin(PFieldElementArray array) {

        Pair<LargeIntegerArray,LargeInteger> p =
            values.modRecLin(((PFieldElementArray)array).values, pField.order);

        return new Pair<PRingElementArray,PRingElement>
            (new PFieldElementArray(pField, p.first),
             new PFieldElement(pField, p.second));

    }

    public PFieldElementArray copyOfRange(int startIndex, int endIndex) {
        LargeIntegerArray cia = values.copyOfRange(startIndex, endIndex);
        return new PFieldElementArray(pField, cia);
    }

    public PFieldElement get(int index) {
        return new PFieldElement(pField, values.get(index));
    }

    public PFieldElement[] elements() {
        LargeInteger[] li = values.integers();
        PFieldElement[] elements = new PFieldElement[li.length];
        for (int i = 0; i < li.length; i++) {
            elements[i] = new PFieldElement(pField, li[i]);
        }
        return elements;
    }

    public void free() {
        values.free();
        values = null;
    }

    public ByteTreeBasic toByteTree() {
        return values.toByteTree(pField.orderByteLength);
    }
}