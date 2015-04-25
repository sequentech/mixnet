
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

package verificatum.crypto;

import java.util.*;

import verificatum.arithm.*;
import verificatum.eio.*;
import verificatum.ui.*;

/**
 * Implementation of the Merkle-Damgaard construction of an
 * arbitrary-length collision-resistant hash function from a fixed length
 * collision-resistant hash function.
 *
 * @author Douglas Wikstrom
 */
public class HashfunctionMerkleDamgaard implements Hashfunction {

    /**
     * Underlying fixed length collision-resistant hash function.
     */
    protected HashfunctionFixedLength hffl;

    /**
     * Constructs an instance following the instructions in the input
     * <code>ByteTree</code>.
     *
     * @param btr Representation of instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Hashfunction represented by the input.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public static HashfunctionMerkleDamgaard newInstance(ByteTreeReader btr,
                                                         RandomSource rs,
                                                         int certainty)
	throws CryptoFormatException {
	try {
	    HashfunctionFixedLength hffl =
		Marshalizer.unmarshalAux_HashfunctionFixedLength(btr,
                                                                 rs,
                                                                 certainty);
	    return new HashfunctionMerkleDamgaard(hffl);
	} catch (EIOException eioe) {
	    throw new CryptoFormatException("Unable to interpret!", eioe);
	}
    }

    /**
     * Creates an instance from the given fixed length hash function.
     *
     * @param hffl Fixed length collision-resistant hash function.
     */
    public HashfunctionMerkleDamgaard(HashfunctionFixedLength hffl) {
	this.hffl = hffl;
    }


    public String toString() {
        return hffl.toString();
    }

    // Documented in Hashfunction.java

    public byte[] hash(byte[] ... datas) {
        Hashdigest hd = getDigest();

        for (int i = 0; i < datas.length; i++) {
            hd.update(datas[i]);
        }
        return hd.digest();
    }

    public Hashdigest getDigest() {
        return new HashdigestMerkleDamgaard(hffl);
    }

    public int getOutputLength() {
	return hffl.getOutputLength();
    }

    public ByteTreeBasic toByteTree() {
	return Marshalizer.marshal(hffl);
    }

    // Documented in HumanDescription.java

    public String humanDescription(boolean verbose) {
	return Util.className(this, verbose) +
	    "(" + hffl.humanDescription(verbose) + ")";
    }
}
