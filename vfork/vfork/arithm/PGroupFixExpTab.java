
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
import java.lang.reflect.*;
import java.util.*;

import vfork.crypto.*;
import vfork.eio.*;
import vfork.util.*;

/**
 * Implementation of fixed base exponentiation. A good reference for
 * this technique is Menezes et al., Handbook of Cryptography.
 *
 * @author Douglas Wikstrom
 */
public class PGroupFixExpTab {

    /**
     * Width of table of pre-computed values.
     */
    protected PGroupSimExpTab tab;

    /**
     * Bit length of each slice of an exponent.
     */
    protected int sliceSize;

    /**
     * Theoretically optimal width of pre-computed table.
     *
     * @param bitLength Bit length of exponents used to compute
     * power-products.
     * @param size Number of exponentiations that will be computed.
     * @return Theoretical optimal width.
     */
    public static int optimalWidth(int bitLength, int size) {

        int width = 2;
        double cost = 1.5 * bitLength;
        double oldCost;
        do {

            oldCost = cost;

            // Amortized cost for table.
            double t = ((1 << width) - width + bitLength) / size;

            // Cost for multiplication.
            double m = (bitLength / width);

            cost = t + m;

            width++;

        } while (width < 31 && cost < oldCost);

        // We reduce the theoretical value by one to account for the
        // overhead.
        return width - 1;
    }

    /**
     * Creates a pre-computed table.
     *
     * @param basis Fixed basis used for pre-computation.
     * @param bitLength Bit length of exponents used to compute
     * power-products.
     * @param width Number of bases elements to use.
     */
    public PGroupFixExpTab(PGroupElement basis, int bitLength, int width) {

        PGroup pGroup = basis.getPGroup();
        PGroupElement ONE = pGroup.getONE();

        // Determine the number of bits associated with each bases.
        sliceSize = (bitLength + (width - 1)) / width;

        // Create radix element.
        PField pField = pGroup.getPRing().getPField();
        PFieldElement b =
            pField.toElement(LargeInteger.ONE.shiftLeft(sliceSize));

        // Create generators.
        PGroupElement[] bases = new PGroupElement[width];
        bases[0] = basis;
        for (int i = 1; i < bases.length; i++) {
            bases[i] = bases[i - 1].exp(b);
        }

        // Invoke the pre-computation of the simultaneous
        // exponentiation code.
        tab = new PGroupSimExpTab(bases, 0, width);
    }

    /**
     * Cuts an integer into the appropriate number of slices.
     *
     * @param exponent Exponent to be slized.
     */
    protected int[] slice(LargeInteger exponent) {

        int[] res = new int[sliceSize];

        for (int i = 0; i < sliceSize; i++) {

            res[i] = 0;

            for (int j = tab.width - 1; j >= 0; j--) {

                res[i] <<= 1;
                res[i] |= (exponent.testBit(j * sliceSize + i) ? 1 : 0);
            }
        }

        return res;
    }

    /**
     * Compute power using the given integer.
     *
     * @param integer Integer exponent.
     */
    public PGroupElement exp(LargeInteger integer) {

        int[] sliced = slice(integer);

        PGroupElement res = tab.pre[0].getPGroup().getONE();
        for (int i = sliced.length - 1; i >= 0; i--) {
            res = res.mul(res);
            res = res.mul(tab.pre[sliced[i]]);
        }
        return res;
    }
}
