
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
import java.util.*;

import vfork.crypto.*;
import vfork.eio.*;
import vfork.util.*;

/**
 * A simple concrete subclass of {@link PGroupElementArray} that wraps
 * a primitive array of {@link PGroupElement}-instances. Almost all of
 * the methods of this class work even for a subclass that replaces
 * the internal representation, e.g., to implement a file based
 * subclass, the methods provided by this class can be replaced one by
 * one, even if the internal format is replaced. Thus, you may
 * subclass this class to start with and then if needed optimize it
 * step by step and finally subclass {@link PGroupElementArray}
 * directly.
 *
 * @author Douglas Wikstrom
 */
public class BPGroupElementArray extends PGroupElementArray {

    /**
     * Representation of this instance.
     */
    protected PGroupElement[] values;

    /**
     * Constructs an array of elements of the given group.
     *
     * @param pGroup Group to which the elements of this array
     * belong.
     * @param values Elements of this array.
     */
    protected BPGroupElementArray(PGroup pGroup, PGroupElement[] values) {
        super(pGroup);
        this.values = Arrays.copyOfRange(values, 0, values.length);
    }

    /**
     * Constructs an array of elements from the given representation.
     *
     * @param pGroup Group to which the elements of this array
     * belong.
     * @param size Expected number of elements in array.
     * @param btr Representation of an instance.
     * @throws ArithmFormatException If the input does not represent
     * an instance.
     */
    protected BPGroupElementArray(PGroup pGroup, int size, ByteTreeReader btr)
        throws ArithmFormatException {
        super(pGroup);

        if (size == 0) {
            size = btr.getRemaining();
        }

        if (btr.getRemaining() != size) {
            throw new ArithmFormatException("Unexpected size!");
        }

        this.values = new PGroupElement[size];

        try {
            for (int i = 0; i < size; i++) {
                values[i] = pGroup.toElement(btr.getNextChild());
            }
        } catch (EIOException eioe) {
            throw new ArithmFormatException("Malformed array!", eioe);
        }
    }


    // Documented in PGroupElementArray.java


    // These must be replaced in a subclass from start.

    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(values);
    }

    public PGroupElement[] elements() {
        return Arrays.copyOf(values, values.length);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("(" + values[0].toString());
        for (int i = 1; i < values.length; i++) {
            sb.append("," + values.toString());
        }
        sb.append(")");
        return sb.toString();
    }

    // The following methods can be replaced by optimized versions one
    // by one in a subclass.

    // Note that BPGroupElementIterator uses a counter and get(int) to
    // iterate of this instance. Thus, it will work if the rest of the
    // methods of this class are overloaded, but it will be *slow*, so
    // replace the iterator when you replace the rest.
    public PGroupElementIterator getIterator() {
	return new BPGroupElementIterator(this);
    }

    public PGroupElement get(int index) {
        return elements()[index];
    }

    public PGroupElementArray mul(PGroupElementArray factorsArray) {
        return pGroup.toElementArray(pGroup.mul(elements(),
                                                factorsArray.elements()));
    }

    public PGroupElementArray inv() {
        return pGroup.toElementArray(pGroup.inv(elements()));
    }

    public PGroupElementArray exp(PRingElementArray exponentsArray) {
        return pGroup.toElementArray(pGroup.exp(elements(),
                                                exponentsArray.elements()));
    }

    public PGroupElementArray exp(PRingElement exponent) {
        return pGroup.toElementArray(pGroup.exp(elements(), exponent));
    }

    public PGroupElement prod() {
        return pGroup.prod(elements());
    }

    public PGroupElement expProd(PRingElementArray exponentsArray) {
        return pGroup.expProd(elements(), exponentsArray.elements());
    }

    public int compareTo(PGroupElementArray array) {
        if (array instanceof BPGroupElementArray) {

            BPGroupElementArray bparray = (BPGroupElementArray)array;

            if (bparray.pGroup.equals(pGroup)) {

                for (int i = 0; i < values.length; i++) {
                    int cmp = values[i].compareTo(bparray.values[i]);
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                return 0;
            }
        }
        throw new ArithmError("Illegal comparison!");
    }

    public boolean equals(Object otherArray) {
        if (otherArray instanceof BPGroupElementArray) {
            return Arrays.equals(elements(),
                                 ((PGroupElementArray)otherArray).elements());
        }
        return false;
    }

    public boolean[] equalsAll(PGroupElementArray otherArray) {
        if (otherArray instanceof BPGroupElementArray) {

	    PGroupElement[] others = otherArray.elements();
	    PGroupElement[] elements = elements();

	    if (others.length != elements.length) {
		throw new ArithmError("Different lengths!");
	    }

	    boolean[] res = new boolean[elements.length];
	    for (int i = 0; i < elements.length; i++) {
		res[i] = elements[i].equals(others[i]);
	    }
	    return res;
	}
        throw new ArithmError("Illegal comparison!");
    }

    public int size() {
        return elements().length;
    }

    public PGroupElementArray permute(Permutation permutation) {
        PGroupElement[] elements = elements();
        PGroupElement[] permuted = new PGroupElement[elements.length];
        permutation.applyPermutation(elements, permuted);
        return pGroup.toElementArray(permuted);
    }

    public PGroupElementArray shiftPush(PGroupElement el) {
        PGroupElement[] elements = elements();
        PGroupElement[] res = new PGroupElement[elements.length];
        res[0] = el;
        System.arraycopy(elements, 0, res, 1, elements.length - 1);
        return pGroup.toElementArray(res);
    }

    public PGroupElementArray copyOfRange(int startIndex, int endIndex) {
        return pGroup.toElementArray(Arrays.copyOfRange(elements(),
                                                        startIndex,
                                                        endIndex));
    }

    public PGroupElementArray extract(boolean[] valid) {

	if (valid.length != size()) {
	    throw new ArithmError("Wrong size of characteristic vector!");
	}

        // Number of elements to extract.
        int count = 0;
        for (int i = 0; i < valid.length; i++) {
            if (valid[i]) {
                count++;
            }
        }

        // Extract.
        PGroupElement[] elements = elements();
        PGroupElement[] res = new PGroupElement[count];
        for (int i = 0, j = 0; i < valid.length; i++) {
            if (valid[i]) {
                res[j++] = elements[i];
            }
        }
        return pGroup.toElementArray(res);
    }

    public void free() {
        PGroupElement[] elements = elements();
        for (int i = 0; i < elements.length; i++) {
            elements[i].free();
        }
    }
}
