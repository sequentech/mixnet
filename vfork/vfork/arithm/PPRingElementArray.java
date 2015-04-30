
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
 * Implements an array of instances of {@link PRingElement}.
 *
 * @author Douglas Wikstrom
 */
public class PPRingElementArray extends PRingElementArray {

    /**
     * Representation of this array.
     */
    PRingElementArray[] values;

    /**
     * Constructs an array with the given underlying ring. This
     * constructor does not copy the input array.
     *
     * @param pRing Underlying ring.
     * @param values Underlying values.
     */
    protected PPRingElementArray(PPRing pRing, PRingElementArray[] values) {
        super(pRing);
        this.values = values;
    }


    // Documented in PRingElementArray.java

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

    /**
     * Adds the input to this instance. If the input belongs to the
     * same ring as this instance, then the sum is defined in the
     * natural way and otherwise an attempt is made to add the input
     * to the components of this instance.
     *
     * @param terms Element to add to this instance.
     * @return Sum of this instance and the input.
     */
    public PPRingElementArray add(PRingElementArray terms) {
        PRingElementArray[] res = new PRingElementArray[values.length];

        if (terms.pRing.equals(pRing)) {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].add(((PPRingElementArray)terms).values[i]);
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].add(terms);
            }
        }

        return new PPRingElementArray((PPRing)pRing, res);
    }

    public PPRingElementArray neg() {
        PRingElementArray[] res = new PRingElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].neg();
        }
        return new PPRingElementArray((PPRing)pRing, res);
    }

    /**
     * Multiplies the input with this instance. If the input belongs
     * to the same ring as this instance, then the product is defined
     * in the natural way and otherwise an attempt is made to multiply
     * the input with the components of this instance.
     *
     * @param factor Element to multiply with this instance.
     * @return Product of this instance and the input.
     */
    public PPRingElementArray mul(PRingElement factor) {
        PRingElementArray[] res = new PRingElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].mul(factor);
        }

        return new PPRingElementArray((PPRing)pRing, res);
    }

    /**
     * Multiplies the input with this instance. If the input belongs
     * to the same ring as this instance, then the product is defined
     * in the natural way and otherwise an attempt is made to multiply
     * the input with the components of this instance.
     *
     * @param factor Element to multiply with this instance.
     * @return Product of this instance and the input.
     */
    public PPRingElementArray mul(PRingElementArray factors) {
        PRingElementArray[] res = new PRingElementArray[values.length];

        if (factors.pRing.equals(pRing)) {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(((PPRingElementArray)factors).values[i]);
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].mul(factors);
            }
        }

        return new PPRingElementArray((PPRing)pRing, res);
    }

    public PPRingElementArray inv() throws ArithmException {
        PRingElementArray[] res = new PRingElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].inv();
        }
        return new PPRingElementArray((PPRing)pRing, res);
    }

    /**
     * Takes the "inner product" of this instance and the input. If
     * the input belongs to the same ring as this instance, then the
     * "inner product" is defined in the natural way and otherwise an
     * attempt is made to multiply the input with the components of
     * this instance.
     *
     * @param vector Element to multiply with this instance.
     * @return Product of this instance and the input.
     */
    public PPRingElement innerProduct(PRingElementArray vector) {
        PRingElement[] res = new PRingElement[values.length];

        if (vector.pRing.equals(pRing)) {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].
                    innerProduct(((PPRingElementArray)vector).values[i]);
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                res[i] = values[i].innerProduct(vector);
            }
        }
        return new PPRingElement((PPRing)pRing, res);
    }

    public PPRingElement sum() {
        PRingElement[] res = new PRingElement[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].sum();
        }
        return new PPRingElement((PPRing)pRing, res);
    }

    public PPRingElement prod() {
        PRingElement[] res = new PRingElement[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].prod();
        }
        return new PPRingElement((PPRing)pRing, res);
    }

    public PPRingElementArray prods() {
        PRingElementArray[] res = new PRingElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].prods();
        }
        return new PPRingElementArray((PPRing)pRing, res);
    }

    public int size() {
        return values[0].size();
    }

    public PPRingElementArray permute(Permutation permutation) {
        PRingElementArray[] res = new PRingElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].permute(permutation);
        }
        return new PPRingElementArray((PPRing)pRing, res);
    }

    public Pair<PRingElementArray,PRingElement>
        recLin(PFieldElementArray array) {

        PRingElementArray[] res = new PRingElementArray[values.length];
        PRingElement[] elementRes = new PRingElement[values.length];

        for (int i = 0; i < values.length; i++) {
            Pair<PRingElementArray,PRingElement> p = values[i].recLin(array);
            res[i] = p.first;
            elementRes[i] = p.second;
        }
        return new Pair<PRingElementArray,PRingElement>
            (new PPRingElementArray((PPRing)pRing, res),
             new PPRingElement((PPRing)pRing, elementRes));
    }

    public PPRingElementArray copyOfRange(int startIndex, int endIndex) {
        PRingElementArray[] res = new PRingElementArray[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].copyOfRange(startIndex, endIndex);
        }
        return new PPRingElementArray((PPRing)pRing, res);
    }

    public PPRingElement get(int index) {
        PRingElement[] res = new PRingElement[values.length];

        for (int i = 0; i < values.length; i++) {
            res[i] = values[i].get(index);
        }
        return new PPRingElement((PPRing)pRing, res);
    }

    public PPRingElement[] elements() {
        PRingElement[][] tmp = new PRingElement[values.length][];
        for (int i = 0; i < values.length; i++) {
            tmp[i] = values[i].elements();
        }

        PPRingElement[] res = new PPRingElement[tmp[0].length];
        for (int j = 0; j < res.length; j++) {

            PRingElement[] els = new PRingElement[values.length];
            for (int i = 0; i < values.length; i++) {
                els[i] = tmp[i][j];
            }
            res[j] = new PPRingElement((PPRing)pRing, els);
        }
        return res;
    }

    public void free() {
        for (int i = 0; i < values.length; i++) {
            values[i].free();
        }
        values = null;
    }
}