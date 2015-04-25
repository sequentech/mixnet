
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
 * Implements an immutable element of a {@link PPRing} instance.
 *
 * @author Douglas Wikstrom
 */
public class PPRingElement extends PRingElement {

    /**
     * Underlying ring elements.
     */
    protected PRingElement[] values;

    /**
     * Creates an instance. The input array is not copied.
     *
     * @param pRing Ring to which the created instance belongs.
     * @param values Representative of the created ring element.
     * @throws ArithmFormatException If the input does not represent a
     * ring element.
     */
    protected PPRingElement(PPRing pRing, PRingElement ... values) {
        super(pRing);
        this.values = values;
    }

    /**
     * Returns the projection of this element to the given indices.
     *
     * @param indices Indices on which we project.
     * @return Projection of this element to the chosen indices.
     */
    public PRingElement project(boolean[] indices) {

        if (values.length != indices.length) {
            throw new ArithmError("Mismatching degrees!");
        }

        int count = 0;
        for (int i = 0; i < indices.length; i++) {
            if (indices[i]) {
                count++;
            }
        }

        String e = "Mismatching ring and ring element!";
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

            PRingElement[] res = new PRingElement[count];
            for (int i = 0, j = 0; i < indices.length; i++) {
                if (indices[i]) {
                    res[j++] = values[i];
                }
            }

            PRing respPRing = ((PPRing)pRing).project(indices);
            return new PPRingElement((PPRing)respPRing, res);
        }
    }

    /**
     * Returns the projection of this element at the given index.
     *
     * @param i Index on which to project
     * @return Element at the given index.
     */
    public PRingElement project(int i) {
        return values[i];
    }

    /**
     * Returns the "factorization" of this element, i.e., an array
     * containing the underlying elements of this element.
     *
     * @return Factorization of this element.
     */
    public PRingElement[] getFactors() {
        return Arrays.copyOf(values, values.length);
    }

    /**
     * Returns the ring associated with this element.
     *
     * @return Ring associated with this element.
     */
    public PPRing getPPRing() {
        return (PPRing)pRing;
    }


    // Documented in PRingElement.java

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
        return pRing.toByteTree(values);
    }

    /**
     * Adds the input to this instance. If the input belongs to the
     * same ring as this instance, then the sum is defined in the
     * natural way and otherwise an attempt is made to add the input
     * to the components of this instance.
     *
     * @param el Element to add to this instance.
     * @return Sum of this instance and the input.
     */
    public PPRingElement add(PRingElement el) {
        PRingElement[] res = new PRingElement[values.length];

        if (el.pRing.equals(pRing)) {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].add(((PPRingElement)el).values[i]);
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].add(el);
            }
        }
        return new PPRingElement((PPRing)pRing, res);
    }

    public PPRingElement neg() {
        PRingElement[] res = new PRingElement[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].neg();
        }
        return new PPRingElement((PPRing)pRing, res);
    }

    /**
     * Multiplies the input with this instance. If the input belongs
     * to the same ring as this instance, then the product is defined
     * in the natural way and otherwise an attempt is made to multiply
     * the input with the components of this instance.
     *
     * @param el Element to multiply with this instance.
     * @return Product of this instance and the input.
     */
    public PPRingElement mul(PRingElement el) {
        PRingElement[] res = new PRingElement[values.length];

        if (el.pRing.equals(pRing)) {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(((PPRingElement)el).values[i]);
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(el);
            }
        }
        return new PPRingElement((PPRing)pRing, res);
    }

    public PPRingElement inv() throws ArithmException {
        PRingElement[] res = new PRingElement[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].inv();
        }

        return new PPRingElement((PPRing)pRing, res);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof PPRingElement) {

            PRingElement[] ovalues = ((PPRingElement)obj).values;

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
