
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

package mixnet.crypto;

import java.security.*;

import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.ui.*;

/**
 * Wrapper of standardized signatures. Currently, this means RSA full
 * domain hash based on SHA-256.
 *
 * @author Douglas Wikstrom
 */
public class SignatureKeyGenHeuristic
    implements SignatureKeyGen, Marshalizable {

    /**
     * Underlying "signature algorithm".
     */
    protected static String algorithm = "RSA";

    /**
     * Underlying key generation algorithm.
     */
    protected KeyPairGenerator keyGen;

    /**
     * Bit length of modulus of public key.
     */
    protected int bitlength;

    /**
     * Returns a new instance as defined by the input.
     *
     * @param btr Representation of key generator.
     * @return Instance of key generator.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public static SignatureKeyGenHeuristic newInstance(ByteTreeReader btr)
    throws CryptoFormatException {
        try {
            return new SignatureKeyGenHeuristic(btr.readInt());
        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        }
    }

    /**
     * Create a key generator for the given bit length.
     *
     * @param bitlength Bit length of modulus of generated public
     * keys.
     */
    public SignatureKeyGenHeuristic(int bitlength) {
        try {
            keyGen = KeyPairGenerator.getInstance(algorithm);
            keyGen.initialize(bitlength);
            this.bitlength = bitlength;
        } catch (NoSuchAlgorithmException nsae) {
            throw new CryptoError("Failed to instantiate key generator!", nsae);
        }
    }

    /**
     * Generates a signature key pair. WARNING! This method ignores
     * the random source parameter and uses the builtin standard
     * {@link SecureRandom} instance of the virtual machine.
     *
     * @param randomSource Source of randomness used by generator.
     * @return Signature key pair.
     */
    public SignatureKeyPair gen(RandomSource randomSource) {

        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();

        return new SignatureKeyPair(new SignaturePKeyHeuristic(pub,
                                                               bitlength),
                                    new SignatureSKeyHeuristic(priv,
                                                               bitlength));
    }


    // Documented in Marshalizable.java

    public ByteTree toByteTree() {
        return ByteTree.intToByteTree(bitlength);
    }

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(" + algorithm + ", bitlength=" + bitlength + ")";
    }
}
