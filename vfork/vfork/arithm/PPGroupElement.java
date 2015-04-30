
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

/**
 * Represents an element associated with a {@link PPGroup}-instance.
 *
 * @author Douglas Wikstrom
 */
public class PPGroupElement extends PGroupElement {

    /**
     * Representation of this element.
     */
    protected PGroupElement[] values;

    /**
     * Initializes this instance.
     *
     * @param pGroup Group to which this instance will belong.
     * @param values Representation of this element.
     */
    protected PPGroupElement(PGroup pGroup, PGroupElement[] values) {
        super(pGroup);
        this.values = values;
    }

    /**
     * Returns the projection of this element to the product group
     * defined by the given indices.
     *
     * @param indices Indices of subgroup to which we project.
     * @return Projection of this element to subgroup.
     */
    public PGroupElement project(boolean[] indices) {

        if (values.length != indices.length) {
            throw new ArithmError("Mismatching degrees!");
        }

        int count = 0;
        for (int i = 0; i < indices.length; i++) {
            if (indices[i]) {
                count++;
            }
        }

        String e = "Mismatching group and group element!";
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

            PGroupElement[] res = new PGroupElement[count];
            for (int i = 0, j = 0; i < indices.length; i++) {
                if (indices[i]) {
                    res[j++] = values[i];
                }
            }

            PGroup respPGroup = ((PPGroup)pGroup).project(indices);
            return ((PPGroup)respPGroup).toElement(res);
        }
    }

    /**
     * Returns the projection of this element to the subgroup with the
     * given index.
     *
     * @param i Index on which to project
     * @return Element at the given index.
     */
    public PGroupElement project(int i) {
        return values[i];
    }

    /**
     * Returns the "factorization" of this element, i.e., an array
     * containing the underlying elements of this element.
     *
     * @return Factorization of this element.
     */
    public PGroupElement[] getFactors() {
        return Arrays.copyOf(values, values.length);
    }

    /**
     * Returns the {@link PPGroup} to which this instance belongs.
     *
     * @return <code>PPGroup</code> to which this instance belongs.
     */
    public PPGroup getPPGroup() {
        return (PPGroup)pGroup;
    }

    /**
     * Returns the associated {@link PPRing} instance.
     *
     * @return Associated <code>PPRing</code> instance.
     */
    public PPRing getPPRing() {
        return (PPRing)pGroup.pRing;
    }

    // Documented in PGroupElement.java

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(values[i].toString());
        }
        sb.append(")");
        return sb.toString();
    }

    public ByteTreeBasic toByteTree() {
        ByteTreeBasic[] byteTrees = new ByteTreeBasic[values.length];
        for (int i = 0; i < byteTrees.length; i++) {
            byteTrees[i] = values[i].toByteTree();
        }
        return new ByteTreeContainer(byteTrees);
    }

    public int decode(byte[] array, int startIndex) {
        int offset = startIndex;
        for (int i = 0; i < values.length; i++) {
            offset += values[i].decode(array, offset);
        }
        return offset - startIndex;
    }

    public PGroupElement mul(PGroupElement el) {

        PGroupElement[] res = new PGroupElement[values.length];
        if (pGroup.equals(el.pGroup)) {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(((PPGroupElement)el).values[i]);
            }

        } else {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(el);
            }

        }
        return ((PPGroup)pGroup).toElement(res);
    }

    public PGroupElement inv() {
        PGroupElement[] res = new PGroupElement[values.length];
        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].inv();
        }
        return ((PPGroup)pGroup).toElement(res);
    }

    public PGroupElement exp(PRingElement exponent) {

        PGroupElement[] res = new PGroupElement[values.length];
        if (exponent.pRing.equals(pGroup.pRing)) {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(((PPRingElement)exponent).values[i]);
            }

        } else {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(exponent);
            }

        }
        return ((PPGroup)pGroup).toElement(res);
    }

    public PGroupElement[] exp(PRingElement[] exponents) {

        PGroupElement[][] res = new PGroupElement[values.length][];

        PRingElement[][] decExponents = null;

        if (pGroup.pRing.equals(exponents[0].pRing)) {
            decExponents = ((PPRing)pGroup.pRing).decompose(exponents);
        }

        if (decExponents != null) {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(decExponents[i]);
            }

        } else {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(exponents);
            }

        }
        return ((PPGroup)pGroup).compose(res);
    }

    public PGroupElementArray exp(PRingElementArray exponents) {

        PGroupElementArray[] res = new PGroupElementArray[values.length];

        if (pGroup.pRing.equals(exponents.pRing)) {

            for (int i = 0; i < values.length; i++) {
                res[i] =
                    values[i].exp(((PPRingElementArray)exponents).values[i]);
            }

        } else {

            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].exp(exponents);
            }
        }

        return new PPGroupElementArray(pGroup, res);
    }

    public int compareTo(PGroupElement obj) {
        if (obj instanceof PPGroupElement) {

            PGroupElement[] ovalues = ((PPGroupElement)obj).values;

            if (values.length != ovalues.length) {
                throw new ArithmError("Different internal lengths!");
            }

            for (int i = 0; i < values.length; i++) {
                int cmp = values[i].compareTo(ovalues[i]);
                if (cmp != 0) {
                    return cmp;
                }
            }
            return 0;
        } else {
            throw new ArithmError("Not a PPGroupElement!");
        }
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PPGroupElement) {

            PGroupElement[] ovalues = ((PPGroupElement)obj).values;

            if (values.length == ovalues.length) {

                for (int i = 0; i < values.length; i++) {
                    if (!values[i].equals(ovalues[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void free() {
        for (int i = 0; i < values.length; i++) {
            values[i].free();
        }
    }
}
