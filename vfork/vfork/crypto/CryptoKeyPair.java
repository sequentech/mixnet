
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

package vfork.crypto;

import vfork.eio.*;
import vfork.ui.*;

/**
 * Container class of a public key and secret key of a cryptosystem
 * with labels.
 *
 * @author Douglas Wikstrom
 */
public class CryptoKeyPair implements Marshalizable {

    /**
     * Secret key stored in this instance.
     */
    protected CryptoSKey skey;

    /**
     * Public key stored in this instance.
     */
    protected CryptoPKey pkey;

    /**
     * Create instance containing the given keys.
     *
     * @param pkey Public key.
     * @param skey Secret key.
     */
    public CryptoKeyPair(CryptoPKey pkey, CryptoSKey skey) {
        this.pkey = pkey;
        this.skey = skey;
    }

    /**
     * Create instance from the given representation.
     *
     * @param btr Representation of an instance.
     * @param rs Random source used to probabilistically check the
     * validity of an input.
     * @param certainty Certainty with which an input is deemed
     * correct, i.e., an incorrect input is accepted with probability
     * at most 2<sup>-<code>certainty</code></sup>.
     * @return Instance corresponding to the input.
     *
     * @throws CryptoFormatException If the input does not represent
     * an input.
     */
    public static CryptoKeyPair newInstance(ByteTreeReader btr, RandomSource rs,
                                            int certainty)
        throws CryptoFormatException {
        try {
            CryptoPKey pkey =
                Marshalizer.unmarshalAux_CryptoPKey(btr.getNextChild(),
                                                    rs, certainty);
            CryptoSKey skey =
                Marshalizer.unmarshalAux_CryptoSKey(btr.getNextChild(),
                                                    rs, certainty);

            return new CryptoKeyPair(pkey, skey);

        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        }
    }

    /**
     * Return the public key of this instance.
     *
     * @return Public key.
     */
    public CryptoPKey getPKey() {
        return pkey;
    }

    /**
     * Return the secret key of this instance.
     *
     * @return Secret key.
     */
    public CryptoSKey getSKey() {
        return skey;
    }

    // Documented in Marshalizable.java

    public ByteTreeBasic toByteTree() {
        return new ByteTreeContainer(Marshalizer.marshal(pkey),
                                     Marshalizer.marshal(skey));
    }

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(" + pkey.humanDescription(verbose) + "," +
            skey.humanDescription(verbose) + ")";
    }
}