
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
import java.util.*;

import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.util.*;

/**
 * Implements an array of instances of {@link PRingElement}.
 *
 * @author Douglas Wikstrom
 */
public class APRingElementArray extends PRingElementArray {

    /**
     * Representation of the content of this instance.
     */
    protected PRingElement[] values;

    /**
     * Constructs an array with the given underlying ring.
     *
     * @param pRing Underlying ring.
     * @param values Content of this element.
     */
    protected APRingElementArray(PRing pRing, PRingElement[] values) {
        super(pRing);
        this.values = values;
    }

    /**
     * Constructs a random array of the given size and with the given
     * underlying ring.
     *
     * @param pRing Underlying ring.
     * @param size Size of array.
     * @param rs Source of randomness.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    protected APRingElementArray(PRing pRing, int size, RandomSource rs,
                                 int statDist) {
        super(pRing);

        // TODO(implementation is missing here)
        this.values = values;
    }

    /**
     * Constructs an array of the given size with the given value for
     * each component with the given underlying ring.
     *
     * @param pRing Underlying ring.
     * @param size Size of array.
     * @param value Value used for each component of the array.
     */
    protected APRingElementArray(PRing pRing, int size, PRingElement value) {
        super(pRing);
        this.values = new PRingElement[size];
        Arrays.fill(this.values, value);
    }

    /**
     * Constructs an array from the given representation.
     *
     * @param pRing Underlying ring.
     * @param size Expected number of elements of array.
     * @param btr Representation of an instance.
     * @throws ArithmFormatException If the input does not represent
     * an array of group elements.
     */
    protected APRingElementArray(PRing pRing, int size, ByteTreeReader btr)
    throws ArithmFormatException {
        super(pRing);

        if (size == 0) {
            size = btr.getRemaining();
        }

        if (btr.getRemaining() != size) {
            throw new ArithmFormatException("Unexpected number of elements!");
        }
        try {
            values = new PRingElement[btr.getRemaining()];
            for (int i = 0; i < values.length; i++) {
                values[i] = pRing.toElement(btr.getNextChild());
            }
        } catch (EIOException eioe) {
            throw new ArithmFormatException("Malformed array!", eioe);
        }
    }


    // Documented in PRingElementArray.java

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(values[0].toString());
        for (int i = 1; i < values.length; i++) {
            sb.append("," + values[i].toString());
        }
        return sb.toString();
    }

    public ByteTreeBasic toByteTree() {
        ByteTreeBasic[] byteTrees = new ByteTreeBasic[values.length];
        for (int i = 0; i < byteTrees.length; i++) {
            byteTrees[i] = values[i].toByteTree();
        }
        return new ByteTreeContainer(byteTrees);
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
    public PRingElementArray add(PRingElementArray terms) {
        PRingElement[] res = new PRingElement[values.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = values[i].add(((APRingElementArray)terms).values[i]);
        }
        return new APRingElementArray(pRing, res);
    }

    public PRingElementArray neg() {
        PRingElement[] res = new PRingElement[values.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = values[i].neg();
        }
        return new APRingElementArray(pRing, res);
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
    public PRingElementArray mul(PRingElement factor) {
        PRingElement[] res = new PRingElement[values.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = values[i].mul(factor);
        }
        return new APRingElementArray(pRing, res);
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
    public PRingElementArray mul(PRingElementArray factors) {
        PRingElement[] res = new PRingElement[values.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = values[i].mul(((APRingElementArray)factors).values[i]);
        }
        return new APRingElementArray(pRing, res);
    }

    public PRingElementArray inv() throws ArithmException {
        PRingElement[] res = new PRingElement[values.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = values[i].inv();
        }
        return new APRingElementArray(pRing, res);
    }

    public PRingElementArray mulAdd(PRingElement scalar,
                                    PRingElementArray terms) {
        PRingElement[] res = new PRingElement[values.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = values[i].mulAdd(scalar,
                                      ((APRingElementArray)terms).values[i]);
        }
        return new APRingElementArray(pRing, res);
    }

    public PRingElementArray mulAdd(PRingElementArray scalars,
                                    PRingElementArray terms) {
        PRingElement[] res = new PRingElement[values.length];
        for (int i = 0; i < res.length; i++) {
            res[i] = values[i].mulAdd(((APRingElementArray)scalars).values[i],
                                      ((APRingElementArray)terms).values[i]);
        }
        return new APRingElementArray(pRing, res);
    }

    public PRingElement innerProduct(PRingElementArray vector) {
        PRingElement res = pRing.getZERO();
        for (int i = 0; i < values.length; i++) {
            res =
                res.add(values[i].mul(((APRingElementArray)vector).values[i]));
        }
        return res;
    }

    public PRingElement sum() {
        PRingElement res = pRing.getZERO();
        for (int i = 0; i < values.length; i++) {
            res = res.add(values[i]);
        }
        return res;
    }

    public PRingElement prod() {
        PRingElement res = pRing.getONE();
        for (int i = 0; i < values.length; i++) {
            res = res.mul(values[i]);
        }
        return res;
    }

    public PRingElementArray prods() {
        PRingElement[] res = new PRingElement[values.length];
        res[0] = values[0];
        for (int i = 1; i < values.length; i++) {
            res[i] = res[i-1].mul(values[i]);
        }
        return new APRingElementArray(pRing, res);
    }

    public int size() {
        return values.length;
    }

    public PRingElementArray permute(Permutation permutation) {
        PRingElement[] res = new PRingElement[values.length];
        permutation.applyPermutation(values, res);
        return new APRingElementArray(pRing, res);
    }

    public Pair<PRingElementArray,PRingElement>
        recLin(PFieldElementArray array) {

	PFieldElement[] elements = array.elements();

        PRingElement[] res = new PRingElement[values.length];
        res[0] = values[0];
        for (int i = 1; i < res.length; i++) {
            res[i] = res[i - 1].mul(elements[i]).add(values[i]);
        }

        return new Pair<PRingElementArray,PRingElement>
            (new APRingElementArray(pRing, res), res[res.length - 1]);

    }

    public PRingElementArray copyOfRange(int startIndex, int endIndex) {
        return new APRingElementArray(pRing, Arrays.copyOfRange(values,
                                                                startIndex,
                                                                endIndex));
    }
    public PRingElement get(int index) {
        return values[index];
    }

    public PRingElement[] elements() {
        return Arrays.copyOfRange(values, 0, values.length);
    }

    public void free() {
        for (int i = 0; i < values.length; i++) {
            values[i].free();
        }
        values = null;
    }
}
