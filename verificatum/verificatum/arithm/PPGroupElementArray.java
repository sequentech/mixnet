
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

import java.io.*;
import java.util.*;

import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.util.*;

/**
 * An array of elements associated with a {@link PPGroup}-instance.
 *
 * @author Douglas Wikstrom
 */
public class PPGroupElementArray extends PGroupElementArray {

    /**
     * Representation of this array.
     */
    protected PGroupElementArray[] values;

    /**
     * Constructs an array of elements of the given group. This does
     * not copy the input array.
     *
     * @param pGroup Group to which the elements of this array
     * belongs.
     * @param values Elements of this array.
     */
    protected PPGroupElementArray(PGroup pGroup, PGroupElementArray[] values) {
        super(pGroup);
        this.values = values;
    }

    /**
     * Returns the projection of this array to the subgroup defined by
     * the given indices.
     *
     * @param indices Indices on which we project.
     * @return Projection of this array to the subgroup.
     */
    public PGroupElementArray project(boolean[] indices) {

        if (values.length != indices.length) {
            throw new ArithmError("Mismatching degrees!");
        }

        int count = 0;
        for (int i = 0; i < indices.length; i++) {
            if (indices[i]) {
                count++;
            }
        }

        String e = "Mismatching group and group element array!";
        if (count < 1) {

            throw new ArithmError("Empty projection!");

        } else if (count == 1) {

            for (int i = 0; i < indices.length; i++) {

                if (indices[i]) {
                    return values[i];
                }
            }
            throw new ArithmError("Indices are empty! (this can not happen)");

        } else {

            PGroupElementArray[] res = new PGroupElementArray[count];
            for (int i = 0, j = 0; i < indices.length; i++) {
                if (indices[i]) {
                    res[j++] = values[i];
                }
                i++;
            }

            PGroup respPGroup = ((PPGroup)pGroup).project(indices);
            return new PPGroupElementArray(respPGroup, res);
        }
    }

    /**
     * Returns the projection of this element at the given index.
     *
     * @param i Index on which to project
     * @return Element array at the given index.
     */
    public PGroupElementArray project(int i) {
        return values[i];
    }

    /**
     * Returns the "factorization" of this element, i.e., an array
     * containing the underlying arrays of this element.
     *
     * @return Factorization of this element.
     */
    public PGroupElementArray[] getFactors() {
        return Arrays.copyOf(values, values.length);
    }


    // Documented in PGroupElementArray.java

    public PGroupElementIterator getIterator() {
	PGroupElementIterator[] iterators =
	    new PGroupElementIterator[values.length];
	for (int i = 0; i < values.length; i++) {
	    iterators[i] = values[i].getIterator();
	}
	return new PPGroupElementIterator((PPGroup)pGroup, iterators);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(values[0].toString());
        for (int i = 1; i < values.length; i++) {
            sb.append("," + values.toString());
        }
        return sb.toString();
    }

    public ByteTreeBasic toByteTree() {
        ByteTreeBasic[] btb = new ByteTreeBasic[values.length];
        for (int i = 0; i < values.length; i++) {
            btb[i] = values[i].toByteTree();
        }
        return new ByteTreeContainer(btb);
    }

    public PPGroupElement get(int index) {
        PGroupElement[] res = new PGroupElement[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].get(index);
        }
        return ((PPGroup)pGroup).toElement(res);
    }

    public PGroupElementArray mul(PGroupElementArray factors) {
        PGroupElementArray[] res = new PGroupElementArray[values.length];

        if (factors.pGroup.equals(pGroup)) {

            PGroupElementArray[] fvalues =
                ((PPGroupElementArray)factors).values;

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(fvalues[i]);
            }

        } else {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(factors);
            }

        }
        return new PPGroupElementArray(pGroup, res);
    }

    public PGroupElementArray inv() {
        PGroupElementArray[] res = new PGroupElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].inv();
        }
        return new PPGroupElementArray(pGroup, res);
    }

    public PGroupElementArray exp(PRingElementArray exponents) {
        PGroupElementArray[] res = new PGroupElementArray[values.length];

        if (exponents.pRing.equals(pGroup.pRing)) {

            PRingElementArray[] evalues =
                ((PPRingElementArray)exponents).values;

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(evalues[i]);
            }

        } else {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(exponents);
            }

        }
        return new PPGroupElementArray(pGroup, res);
    }

    public PGroupElementArray exp(PRingElement exponent) {
        PGroupElementArray[] res = new PGroupElementArray[values.length];

        if (exponent.pRing.equals(pGroup.pRing)) {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(((PPRingElement)exponent).values[i]);
            }

        } else {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(exponent);
            }

        }
        return new PPGroupElementArray(pGroup, res);
    }

    public PGroupElement prod() {
        PGroupElement[] res = new PGroupElement[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].prod();
        }
        return ((PPGroup)pGroup).toElement(res);
    }

    public PGroupElement expProd(PRingElementArray exponents) {
        PGroupElement[] res = new PGroupElement[values.length];

        if (exponents.pRing.equals(pGroup.pRing)) {

            PRingElementArray[] exponentss =
                ((PPRingElementArray)exponents).values;

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].expProd(exponentss[i]);
            }

        } else {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].expProd(exponents);
            }

        }
        return ((PPGroup)pGroup).toElement(res);
    }

    public int compareTo(PGroupElementArray array) {
        if (array instanceof PPGroupElementArray) {
            PPGroupElementArray pparray = (PPGroupElementArray)array;
            if (pparray.pGroup.equals(pGroup)) {
                for (int i = 0; i < values.length; i++) {
                    int cmp = values[i].compareTo(pparray.values[i]);
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                return 0;
            }
        }
        throw new ArithmError("Illegal comparison!");
    }

    public boolean equals(Object array) {
        if (array instanceof PPGroupElementArray) {
            PPGroupElementArray pparray = (PPGroupElementArray)array;

            if (pparray.pGroup.equals(pGroup)) {
                for (int i = 0; i < values.length; i++) {
                    if (!pparray.values[i].equals(values[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean[] equalsAll(PGroupElementArray array) {
        if (array.pGroup.equals(pGroup) && array.size() == size()) {

            PGroupElementArray[] arrays =
                ((PPGroupElementArray)array).values;

            boolean[] res = new boolean[values.length];
            Arrays.fill(res, false);

            for (int i = 0; i < values.length; i++) {

                boolean[] tmp = values[i].equalsAll(arrays[i]);
                for (int j = 0; j < res.length; j++) {
                    res[j] &= tmp[j];
                }
            }
            return res;
        }
        throw new ArithmError("Wrong group or size!");
    }

    public int size() {
        return values[0].size();
    }

    public PGroupElementArray permute(Permutation permutation) {
        PGroupElementArray[] res = new PGroupElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].permute(permutation);
        }
        return new PPGroupElementArray(pGroup, res);
    }

    public PGroupElementArray shiftPush(PGroupElement el) {
        PGroupElementArray[] res = new PGroupElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].shiftPush(((PPGroupElement)el).values[i]);
        }
        return new PPGroupElementArray(pGroup, res);
    }

    public PPGroupElementArray copyOfRange(int startIndex, int endIndex) {
        PGroupElementArray[] res = new PGroupElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].copyOfRange(startIndex, endIndex);
        }
        return new PPGroupElementArray(pGroup, res);
    }

    public PGroupElementArray extract(boolean[] valid) {
        PGroupElementArray[] res = new PGroupElementArray[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].extract(valid);
        }
        return new PPGroupElementArray(pGroup, res);
    }

    public PGroupElement[] elements() {
        PGroupElement[][] tmp = new PGroupElement[values.length][];
        for (int i = 0; i < values.length; i++) {
            tmp[i] = values[i].elements();
        }
        return ((PPGroup)pGroup).compose(tmp);
    }

    public void free() {
        for (int i = 0; i < values.length; i++) {
            values[i].free();
        }
        values = null;
    }
}