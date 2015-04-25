
/*
 * Copyright 2011 Freyr Sævarsson
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

import java.math.*;
import java.security.*;
import java.security.spec.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.ui.*;

/**
 * Implementation of the Cramer-Shoup signature scheme. This is
 * provably secure under the strong RSA assumption and the
 * collision-resistance of the underlying hashfunction.
 *
 * @author Freyr Sævarsson
 */
public class SignatureKeyGenCS implements SignatureKeyGen, Marshalizable{

    /**
     * Bit length of the modulus of the public key.
     */
    protected int bitLength;

    /**
     * Hashfunction used to compress messages.
     */
    protected Hashfunction hashfunction;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Certainty with which generated integers deemed prime are indeed
     * prime.
     */
    protected int certainty;

    /**
     * Returns a new instance as defined by the input.
     *
     * @param btr Representation of key generator.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Instance of key generator.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public static SignatureKeyGenCS newInstance(ByteTreeReader btr,
                                                RandomSource rs,
                                                int certainty)
    throws CryptoFormatException {
        try {
            int bitLength = btr.getNextChild().readInt();
            Hashfunction hashfunction =
                Marshalizer.unmarshalAux_Hashfunction(btr.getNextChild(),
                                                      rs,
                                                      certainty);
            int statDist = btr.getNextChild().readInt();
            int cert = btr.getNextChild().readInt();

            return new SignatureKeyGenCS(bitLength,
                                         hashfunction,
                                         statDist,
                                         cert);
        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        }
    }

    /**
     * Create a key generator for the given bit length.
     *
     * @param bitLength Bit length of components of modulus of generated public
     * keys.
     */
    public SignatureKeyGenCS(int bitLength,
                             Hashfunction hashfunction,
                             int statDist,
                             int certainty) {
        this.bitLength = bitLength;
        this.hashfunction = hashfunction;
        this.statDist = statDist;
        this.certainty = certainty;
    }

    /**
     * Generates a signature key pair.
     *
     * @param randomSource Source of randomness used by generator.
     * @return Signature key pair.
     */
    public SignatureKeyPair gen(RandomSource randomSource) {

        // Generating safe primes p and q of length bitLength
        LargeInteger p = LargeInteger.randomSafePrime(bitLength / 2,
                                                      randomSource,
                                                      certainty);
        LargeInteger q = LargeInteger.randomSafePrime(bitLength / 2,
                                                      randomSource,
                                                      certainty);

        // Modulus n = p * q
        LargeInteger n = p.mul(q);

        // Finding random quadratic residues h and x modulo n.
        LargeInteger h = new LargeInteger(n, statDist, randomSource);
        h = h.mul(h).mod(n);

        LargeInteger x = new LargeInteger(n, statDist, randomSource);
        x = x.mul(x).mod(n);

        // Random (l+1)-bit prime e'
        LargeInteger eprime =
            LargeInteger.randomSafePrime(hashfunction.getOutputLength() + 1,
                                         randomSource,
                                         certainty);

        try {
            SignaturePKey pkey = new SignaturePKeyCS(n,
                                                     h,
                                                     x,
                                                     eprime,
                                                     hashfunction);
            SignatureSKey skey = new SignatureSKeyCS(p,
                                                     q,
                                                     h,
                                                     x,
                                                     eprime,
                                                     statDist,
                                                     certainty,
                                                     hashfunction);
            return new SignatureKeyPair(pkey, skey);

        } catch (CryptoFormatException cfe) {
            throw new CryptoError("Unable to create keys!", cfe);
        }
    }

    // Documented in Marshalizable.java

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(Cramer-Shoup, bitLength=" + bitLength + ", " +
            hashfunction.humanDescription(verbose) + ")";
    }

    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(ByteTree.intToByteTree(bitLength),
                                     Marshalizer.marshal(hashfunction),
                                     ByteTree.intToByteTree(statDist),
                                     ByteTree.intToByteTree(certainty));
    }
}