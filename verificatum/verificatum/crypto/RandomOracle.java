
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

import verificatum.eio.*;
import verificatum.ui.*;

/**
 * A "random oracle" which can be instantiated with a given length
 * output and based on any underlying hashfunction. The "random
 * oracle" first evaluates the length concatenated with its input
 * using the underlying hashfunction. This gives a digest. The output
 * bytes are then derived by concatenating the result of repeatedly
 * evaluating the digest concatenated with an integer counter that is
 * initially set to zero and incremented by one inbetween calls. The
 * resulting output is then truncated to the correct byte length and
 * as many bits as needed in the first output byte are set to zero.
 *
 * @author Douglas Wikstrom
 */
public class RandomOracle implements Hashfunction {

    /**
     * Underlying hashfunction.
     */
    protected Hashfunction roHashfunction;

    /**
     * Output bit length.
     */
    protected int outputLength;

    /**
     * Constructs an instance following the instructions in the input
     * <code>ByteTree</code>.
     *
     * @param btr Instructions for construction of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Random oracle represented by the input.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public static RandomOracle newInstance(ByteTreeReader btr,
                                           RandomSource rs,
                                           int certainty)
        throws CryptoFormatException {
        try {

            Hashfunction roHashfunction =
                Marshalizer.unmarshalAux_Hashfunction(btr.getNextChild(),
                                                      rs,
                                                      certainty);
            int outputLength = btr.getNextChild().readInt();

            return new RandomOracle(roHashfunction, outputLength);

        } catch (EIOException eioe) {
            throw new CryptoFormatException("Unable to interpret!", eioe);
        }
    }

    /**
     * Creates an instance using the given hashfunction and with the
     * given output bit length.
     *
     * @param roHashfunction Underlying hashfunction.
     * @param outputLength Output bit length.
     */
    public RandomOracle(Hashfunction roHashfunction, int outputLength) {
        this.roHashfunction = roHashfunction;
        this.outputLength = outputLength;
    }

    // Documented in Hashfunction.java.

    public int getOutputLength() {
        return outputLength;
    }

    public byte[] hash(byte[] ... datas) {
        Hashdigest d = getDigest();
        for (int i = 0; i < datas.length; i++) {
            d.update(datas[i]);
        }
        return d.digest();
    }

    public Hashdigest getDigest() {
        return new HashdigestRandomOracle(roHashfunction, outputLength);
    }

    // Documented in Marshalizable.java

    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(Marshalizer.marshal(roHashfunction),
                                     ByteTree.intToByteTree(outputLength));
    }

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(" + roHashfunction.humanDescription(verbose) + ")";
    }
}
