
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
 * Secret key of the Cramer-Shoup signature scheme.
 *
 * @author Freyr Saevarsson
 */
public class SignatureSKeyCS implements SignatureSKey {

    /**
     * First factor of modulus.
     */
    protected LargeInteger p;

    /**
     * First factor of modulus.
     */
    protected LargeInteger q;

    /**
     * Modulus.
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
     * Decides the statistical distance from the
     */
    protected int statDist;

    /**
     * Certainty with which generated integers deemed prime are indeed
     * prime.
     */
    protected int certainty;

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
    public static SignatureSKeyCS newInstance(ByteTreeReader btr,
                                              RandomSource rs,
                                              int certainty)
        throws CryptoFormatException {
        try {

            LargeInteger p = new LargeInteger(btr.getNextChild());
            LargeInteger q = new LargeInteger(btr.getNextChild());
            LargeInteger h = new LargeInteger(btr.getNextChild());
            LargeInteger x = new LargeInteger(btr.getNextChild());
            LargeInteger eprime = new LargeInteger(btr.getNextChild());
            int statDist = btr.getNextChild().readInt();
            int readCertainty = btr.getNextChild().readInt();
            Hashfunction hashfunction =
                Marshalizer.unmarshalAux_Hashfunction(btr.getNextChild(),
                                                      rs,
                                                      certainty);

            return new SignatureSKeyCS(p, q, h, x, eprime, statDist,
                                       readCertainty, hashfunction);

        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        } catch (ArithmFormatException afe) {
            throw new CryptoFormatException("Malformed integer!", afe);
        }
    }

    /**
     * Create instance from the given parameters.
     *
     * @param p Safe prime of length bit-length.
     * @param q Safe prime of length bit-length.
     * @param h Random quadratic residue.
     * @param x Random quadratic residue.
     * @param eprime Small fixed prime.
     * @param statDist Decides the statistical distance from the
     * @param certainty Certainty with which generated integers deemed
     * prime are indeed prime.
     * @param hashfunction Underlying hashfunction.
     */
    public SignatureSKeyCS(LargeInteger p,
                           LargeInteger q,
                           LargeInteger h,
                           LargeInteger x,
                           LargeInteger eprime,
                           int statDist,
                           int certainty,
                           Hashfunction hashfunction)
    throws CryptoFormatException {
        this.p = p;
        this.q = q;
        this.n = p.mul(q);
        this.h = h;
        try {
            this.hinv = h.modInv(n);
        } catch (ArithmException ae) {
            throw new ArithmError("Unable to invert element!", ae);
        }
        this.x = x;
        this.eprime = eprime;
        this.statDist = statDist;
        this.certainty = certainty;
        this.hashfunction = hashfunction;
    }

    public String toString() {
        return String.format("%s:%s:%s:%s:%s:%s:%s:%s",
                             p,
                             q,
                             h,
                             x,
                             eprime,
                             statDist,
                             certainty,
                             hashfunction.toString());
    }

    // Documented SignatureSKey.java

    public byte[] sign(RandomSource randomSource, byte[] ... message){
        Hashdigest hd = getDigest();

        for (int i = 0; i < message.length; i++) {
            hd.update(message[i], 0, message[i].length);
        }
        return signDigest(randomSource, hd.digest());
    }

    public Hashdigest getDigest(){
        return hashfunction.getDigest();
    }

    public byte[] signDigest(RandomSource randomSource, byte[] dBA) {

        int ell = hashfunction.getOutputLength();

        // Random l+1 bit prime e != e'
        LargeInteger e;
        do {

            e = LargeInteger.randomPrimeExact(ell + 1, randomSource, certainty);

        } while (e.equals(eprime));

        // Random y' in QRn.
        LargeInteger yprime = new LargeInteger(n, statDist, randomSource);
        yprime = yprime.mul(yprime).mod(n);

        // Solving (y')^e' = x'h^d for x' where d is the hashed message.
        LargeInteger d = LargeInteger.toPositive(dBA);
        LargeInteger xprime =
            yprime.modPow(eprime, n).mul(hinv.modPow(d, n)).mod(n);

        // Solving y^e = xh^H(x') for y using the secret key.
        LargeInteger hashxprime =
            LargeInteger.toPositive(hashfunction.hash(xprime.toByteArray()));

        LargeInteger c = LargeInteger.ZERO;
        try {
            LargeInteger FOUR = LargeInteger.TWO.add(LargeInteger.TWO);
            LargeInteger phi =
                p.sub(LargeInteger.ONE).mul(q.sub(LargeInteger.ONE)).
                divide(FOUR);
            c = e.modInv(phi);
        } catch(ArithmException ae){
            throw new CryptoError("Unable to find inverse!", ae);
        }

        LargeInteger y = x.mul(h.modPow(hashxprime, n)).mod(n).modPow(c, n);

        return new ByteTreeContainer(e.toByteTree(),
                                     y.toByteTree(),
                                     yprime.toByteTree()).toByteArray();
    }

    // Documented in Marshalizer.java

    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(p.toByteTree(),
                                     q.toByteTree(),
                                     h.toByteTree(),
                                     x.toByteTree(),
                                     eprime.toByteTree(),
                                     ByteTree.intToByteTree(statDist),
                                     ByteTree.intToByteTree(certainty),
                                     Marshalizer.marshal(hashfunction));
    }

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(Cramer-Shoup, bit-length=" + n.bitLength() + ", " +
            hashfunction.humanDescription(verbose) + ")";
    }
}