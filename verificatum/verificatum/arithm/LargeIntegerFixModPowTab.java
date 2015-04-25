




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

// Enabled calls to native code begins here.

import jgmpmee.*;

// Enabled calls to native code ends here

import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.util.*;

/**
 * Implementation of fixed base exponentiation. A good reference for
 * this technique is Menezes et al., Handbook of Cryptography.
 *
 * @author Douglas Wikstrom
 */
public class LargeIntegerFixModPowTab {

// Removed pure java code here.
// Enabled calls to native code begins here.

    /**
     * Table containing the precomputed values.
     */
    protected FpowmTab tab;

// Enabled calls to native code ends here

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

        } while (cost < oldCost);

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
     * @param modulus Modulus.
     */
    public LargeIntegerFixModPowTab(LargeInteger basis,
                                    int bitLength,
                                    int width,
                                    LargeInteger modulus) {

// Removed pure java code here.
// Enabled calls to native code begins here.

        tab = new FpowmTab(basis.value, modulus.value, width, bitLength);

// Enabled calls to native code ends here

    }

// Removed pure java code here.

    /**
     * Compute power using the given integer.
     *
     * @param integer Integer exponent.
     */
    public LargeInteger modPow(LargeInteger integer) {

// Removed pure java code here.
// Enabled calls to native code begins here.

        return new LargeInteger(tab.fpowm(integer.value));

// Enabled calls to native code ends here

    }

    /**
     * Explicitly free allocated resources. If you instantiate this
     * class often in an application you must call this method to
     * avoid exhausting the memory.
     */
    public void free() {

// Enabled calls to native code begins here.

        tab.free();

// Enabled calls to native code ends here

    }
}
