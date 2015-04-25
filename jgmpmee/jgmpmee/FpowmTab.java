/*

Copyright 2008 2009 Douglas Wikstrom

This file is part of Java GMP Modular Exponentiation Extension
(JGMPMEE).

JGMPMEE is free software: you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

JGMPMEE is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
License for more details.

You should have received a copy of the GNU Lesser General Public
License along with JGMPMEE.  If not, see
<http://www.gnu.org/licenses/>.

*/

package jgmpmee;

import java.math.*;
import java.util.*;

/**
 * Provides a Java wrapper for a pointer to a native precomputed table
 * used for fixed based modular exponentiation as implemented in
 * {@link JGMPMEE}.
 *
 * @author Douglas Wikstrom
 */
public class FpowmTab {

    /**
     * Stores native pointer to a precomputed fixed base
     * exponentiation table.
     */
    protected long tablePtr;

    /**
     * Creates a precomputed table for the given basis, modulus, and
     * exponent bit length.
     *
     * @param basis Basis element.
     * @param modulus Modulus used during modular exponentiations.
     * @param exponentBitlen Expected bit length of exponents used when
     * invoking the table.
     */
    public FpowmTab(BigInteger basis,
		    BigInteger modulus,
		    int exponentBitlen) {
	this(basis, modulus, 16, exponentBitlen);
    }

    /**
     * Creates a precomputed table for the given basis, modulus, and
     * exponent bit length.
     *
     * @param basis Basis element.
     * @param modulus Modulus used during modular exponentiations.
     * @param blockWidth Number of basis elements used during
     * splitting.
     * @param exponentBitlen Expected bit length of exponents used when
     * invoking the table.
     */
    public FpowmTab(BigInteger basis,
		    BigInteger modulus,
		    int blockWidth,
		    int exponentBitlen) {
	tablePtr = GMPMEE.fpowm_precomp(basis.toByteArray(),
					modulus.toByteArray(),
					blockWidth,
					exponentBitlen);
    }

    /**
     * Computes a modular exponentiation using the given exponent and
     * the basis and modulus previously used to construct this table.
     *
     * @param exponent Exponent used in modular exponentiation.
     */
    public BigInteger fpowm(BigInteger exponent) {
	return new BigInteger(GMPMEE.fpowm(tablePtr,
					   exponent.toByteArray()));
    }

    /**
     * Release resources allocated by native code.
     */
    public void free() {
	if (tablePtr != 0) {
	    GMPMEE.fpowm_clear(tablePtr);
	    tablePtr = 0;
	}
    }

    public void finalize() {
	free();
    }
}
