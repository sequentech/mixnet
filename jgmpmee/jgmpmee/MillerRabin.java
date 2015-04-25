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
 * Implements primality tests and safe-primality tests such that the
 * caller provides the randomness.
 *
 * @author Douglas Wikstrom
 */
public class MillerRabin {

    /**
     * Stores native pointer to state.
     */
    protected long statePtr;

    /**
     * Decides if we are checking primality or safe primality.
     */
    protected boolean primality;

    /**
     * Initializes the Miller-Rabin test for the given
     * integers. Please use the method {@link #trial()} and read the
     * comment.
     *
     * @param n Integer to test.
     * @param primality Decides if we are checking primality or safe
     * primality.
     * @param search Decides if we are searching for an integer or testing.
     */
    public MillerRabin(BigInteger n, boolean primality, boolean search) {
	this.primality = primality;
	if (primality) {
	    statePtr = GMPMEE.millerrabin_init(n.toByteArray(), search);
	} else {
	    statePtr = GMPMEE.millerrabin_safe_init(n.toByteArray(), search);
	}
    }

    /**
     * Returns the result of the trial divisions. {@link
     * #once(BigInteger)} or {@link #done()} must not be called if this
     * function returns false. Note that if this instance is created
     * for searching, this will always return <code>true</code>, since
     * the constructor in that case moves to the first candidate
     * integer that passes trial divisions.
     *
     * @return Returns <code>true</code> or <code>false</code>
     * depending on if the integer is found not to be a candidate
     * after trial divisions.
     */
    public boolean trial() {
	return statePtr != 0;
    }

    /**
     * Increases the integer to the next candidate prime, or safe
     * prime, depending on how this instance was created a candidate
     * prime passes all trial divisions.
     */
    public void nextCandidate() {
	if (primality) {
	    GMPMEE.millerrabin_next_cand(statePtr);
	} else {
	    GMPMEE.millerrabin_safe_next_cand(statePtr);
	}
    }

    /**
     * Returns the current candidate.
     *
     * @return Current candidate.
     */
    public BigInteger getCurrentCandidate() {
	if (primality) {
	    return new BigInteger(GMPMEE.millerrabin_current(statePtr));
	} else {
	    return new BigInteger(GMPMEE.millerrabin_current_safe(statePtr));
	}
    }

    /**
     * Perform one Miller-Rabin test using the given base.
     *
     * @param base Base used in testing.
     * @return <code>false</code> if the integer is not prime and
     * <code>true</code> otherwise.
     */
    public boolean once(BigInteger base) {
	return GMPMEE.millerrabin_once(statePtr, base.toByteArray()) == 1;
    }

    /**
     * Perform one Miller-Rabin test using the given base.
     *
     * @param base Base used in testing.
     * @param index Determines if Miller-Rabin is executed on the
     * tested integer <i>n</i> or <i>(n-1)/2</i>.
     * @return <code>false</code> if the integer is not prime and
     * <code>true</code> otherwise.
     */
    public boolean once(BigInteger base, int index) {
	return GMPMEE.millerrabin_safe_once(statePtr,
					    base.toByteArray(),
					    index) == 1;
    }

    /**
     * Releases resources allocated for testing. This must be called
     * after testing is completed, but it must not be called if {@link
     * #trial()} returns 0.
     */
    public void done() {
	if (statePtr != 0) {
	    if (primality) {
		GMPMEE.millerrabin_clear(statePtr);
	    } else {
		GMPMEE.millerrabin_safe_clear(statePtr);
	    }
	    statePtr = 0;
	}
    }
}
