
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
import java.lang.reflect.*;
import java.util.*;

import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.util.*;

/**
 * Implementation of simultaneous exponentiation. A good reference for
 * this technique is Menezes et al., Handbook of Cryptography.
 *
 * @author Douglas Wikstrom
 */
public class LargeIntegerSimModPowTab {

    /**
     * Width of table of pre-computed values.
     */
    protected int width;

    /**
     * Table of pre-computed values.
     */
    protected LargeInteger[] pre;

    /**
     * Modulus.
     */
    protected LargeInteger modulus;

    /**
     * Theoretically optimal width of pre-computed table.
     *
     * @param bitLength Bit length of exponents used to compute
     * power-products.
     * @return Theoretical optimal width.
     */
    public static int optimalWidth(int bitLength) {

        // This computes the theoretical optimum.
        int width = 1;
        double cost = 1.5 * bitLength;
        double oldCost;
        do {

            oldCost = cost;

            width++;
            int widthExp = 1 << width;
            cost = (widthExp + (2 - 1 / widthExp) * bitLength) / width;

        } while (cost < oldCost);

        return Math.max(1, width - 1);
    }

    /**
     * Creates a pre-computed table.
     *
     * @param bases Bases used for pre-computation.
     * @param offset Position of first basis element to use.
     * @param width Number of bases elements to use.
     */
    public LargeIntegerSimModPowTab(LargeInteger[] bases,
                                    int offset,
                                    int width,
                                    LargeInteger modulus) {
        this.width = width;
        this.modulus = modulus;

        // Make room for table.
        pre = new LargeInteger[1 << width];

        // Precalculation Start
        Arrays.fill(pre, LargeInteger.ONE);

        // Init precalc with bases provided.
        for (int i = 1, j = offset; i < pre.length; i = i * 2, j++){
            pre[i] = bases[j];
        }

        // Perform precalculation using masking for efficiency.
        for(int mask = 0; mask < pre.length; mask++){
            int one_mask = mask & (-mask);
            pre[mask] = pre[mask ^ one_mask].mul(pre[one_mask]).mod(modulus);
        }
    }

    /**
     * Compute a power-product using the given integer exponents.
     *
     * @param integers Integer exponents.
     * @param offset Position of first exponent to use.
     * @param width Number of exponents to use.
     */
    public LargeInteger modPowProd(LargeInteger[] integers, int offset,
                                   int bitLength) {

        // Loop over bits in integers starting at bitLength - 1.
        LargeInteger res = LargeInteger.ONE;

        for (int i = bitLength - 1; i >= 0; i--) {

            int I = 0;

            // Loop over integers to form a word from all the bits at
            // a given position.
            for (int j = offset; j < offset + width; j++) {

                if (integers[j].testBit(i)) {

                    I |= (1 << (j - offset));
                }
            }

            // Square.
            res = res.mul(res).mod(modulus);

            // Multiply.
            res = res.mul(pre[I]).mod(modulus);
        }
        return res;
    }
}
