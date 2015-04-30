
/*
 * Copyright 2011 Freyr Sævarsson
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

package vfork.crypto;

import java.security.*;
import java.security.spec.*;

import vfork.arithm.*;
import vfork.eio.*;
import vfork.ui.*;

/**
 * Public key of the Cramer-Shoup signature scheme.
 *
 * @author Freyr Saevarsson
 */
public class SignaturePKeyCS implements SignaturePKey {

    /**
     * Composite modulus.
     */
    protected LargeInteger n;

    /**
     * First random quadratic residue.
     */
    protected LargeInteger h;

    /**
     * Inverse of first random quadratic residue.
     */
    protected LargeInteger hinv;

    /**
     * Second random quadratic residue.
     */
    protected LargeInteger x;

    /**
     * Small fixed prime.
     */
    protected LargeInteger eprime;

    /**
     * Underlying hashfunction.
     */
    protected Hashfunction hashfunction;

    /**
     * Constructs an instance corresponding to the input.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Instance corresponding to the input.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public static SignaturePKeyCS newInstance(ByteTreeReader btr,
                                              RandomSource rs,
                                              int certainty)
        throws CryptoFormatException {
        try {

            LargeInteger n = new LargeInteger(btr.getNextChild());
            LargeInteger h = new LargeInteger(btr.getNextChild());
            LargeInteger x = new LargeInteger(btr.getNextChild());
            LargeInteger eprime = new LargeInteger(btr.getNextChild());
            Hashfunction hashfunction =
                Marshalizer.unmarshalAux_Hashfunction(btr.getNextChild(),
                                                      rs,
                                                      certainty);

            return new SignaturePKeyCS(n, h, x, eprime, hashfunction);

        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        } catch (ArithmFormatException afe) {
            throw new CryptoFormatException("Malformed integer!", afe);
        }
    }

    /**
     * Creates an instance with the given parameters.
     *
     * @param n Composite modulus.
     * @param h Random quadratic residue.
     * @param x Random quadratic residue.
     * @param eprime Small fixed prime.
     * @param hashfunction Underlying hashfunction.
     */
    public SignaturePKeyCS(LargeInteger n,
                           LargeInteger h,
                           LargeInteger x,
                           LargeInteger eprime,
                           Hashfunction hashfunction)
    throws CryptoFormatException {
        this.n = n;
        this.h = h;
        try {
            this.hinv = h.modInv(n);
        } catch (ArithmException ae) {
            throw new ArithmError("Unable to invert element!", ae);
        }
        this.x = x;
        this.eprime = eprime;
        this.hashfunction = hashfunction;
    }

    public String toString() {
        return String.format("%s:%s:%s:%s:%s",
                             n,
                             h,
                             x,
                             eprime,
                             hashfunction.toString());
    }

    // Documented in SignaturePKey.java

    public boolean verify(byte[] signature, byte[] ... message){

        Hashdigest hd = getDigest();

        for (int i = 0; i < message.length; i++) {
            hd.update(message[i], 0, message[i].length);
        }
        return verifyDigest(signature, hd.digest());
    }

    public boolean verifyDigest(byte[] signature, byte[] dBA){

        int ell = hashfunction.getOutputLength();

        LargeInteger e = null;
        LargeInteger y = null;
        LargeInteger yprime = null;

        try {
            ByteTreeReader btr =
                (new ByteTree(signature, null)).getByteTreeReader();

            e = new LargeInteger(btr.getNextChild());
            y = new LargeInteger(btr.getNextChild());
            yprime = new LargeInteger(btr.getNextChild());

        } catch (EIOException eioe) {
            return false;
        } catch (ArithmFormatException afe) {
            return false;
        }

        // Check that e is an odd ell + 1 bit number different from e'
        if (e.mod(LargeInteger.TWO).compareTo(LargeInteger.ZERO) == 0
           || e.equals(eprime)
           || e.bitLength() != ell + 1){
            return false;
        }

        // Compute x' = (y')^e'*h^-d (d here replaces H(m) in the paper
        // by Cramer-Shoup)
        LargeInteger d = LargeInteger.toPositive(dBA);
        LargeInteger xprime =
            yprime.modPow(eprime, n).mul(hinv.modPow(d, n)).mod(n);

        // Check that x = y^e*h^-H(x')
        LargeInteger hashxprime =
            LargeInteger.toPositive(hashfunction.hash(xprime.toByteArray()));

        LargeInteger xx =
            y.modPow(e, n).mul(hinv.modPow(hashxprime, n)).mod(n);

        return x.equals(xx);
    }

    // Documented in SignaturePKey.java

    public Hashdigest getDigest(){
        return hashfunction.getDigest();
    }

    // Documented in Marshalizable.java

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(Cramer-Shoup, bitlength=" + n.bitLength() + ", " +
            hashfunction.humanDescription(verbose) + ")";
    }

    public ByteTreeContainer toByteTree() {

        return new ByteTreeContainer(n.toByteTree(),
                                     h.toByteTree(),
                                     x.toByteTree(),
                                     eprime.toByteTree(),
                                     Marshalizer.marshal(hashfunction));
    }
}