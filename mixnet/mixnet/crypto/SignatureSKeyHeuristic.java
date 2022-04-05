
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
import java.security.spec.*;

import mixnet.eio.*;
import mixnet.ui.*;

/**
 * Wrapper of a standard signature schemes. Currently, this means RSA
 * full domain hash based on SHA-256.
 *
 * @author Douglas Wikstrom
 */
public class SignatureSKeyHeuristic implements SignatureSKey {

    /**
     * Maximum number of bytes in public key.
     */
    protected static final int MAX_SKEY_BYTELENGTH = 100 * 1024;

    /**
     * Underlying "signature algorithm".
     */
    protected static String algorithm = "RSA";

    /**
     * Bit length of the modulus in the corresponding public key.
     */
    protected int bitlength;

    /**
     * Encapsulated secret signature key.
     */
    protected PrivateKey priv;

    /**
     * Constructs an instance corresponding to the input.
     *
     * @param btr Representation of an instance.
     * @return Instance corresponding to the input.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public static SignatureSKeyHeuristic newInstance(ByteTreeReader btr)
        throws CryptoFormatException {
        try {

            int bitlength = btr.getNextChild().readInt();

            ByteTreeReader kbtr = btr.getNextChild();
            if (kbtr.getRemaining() > MAX_SKEY_BYTELENGTH) {
                throw new CryptoFormatException("Too long key!");
            }

            byte[] keyBytes = kbtr.read();

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

            KeyFactory factory = KeyFactory.getInstance(algorithm);

            return new SignatureSKeyHeuristic(factory.generatePrivate(spec),
                                              bitlength);
        } catch (NoSuchAlgorithmException nsae) {
            throw new CryptoError("Failed to instantiate public key!", nsae);
        } catch (EIOException eioe) {
            throw new CryptoFormatException("Malformed ByteTree!", eioe);
        } catch (InvalidKeySpecException ikse) {
            throw new CryptoFormatException("Malformed public key spec!", ikse);
        }
    }

    /**
     * Create instance from the given parameters.
     *
     * @param priv Encapsulated secret key.
     * @param bitlength Bit length of the modulus in the corresponding
     * public key.
     */
    public SignatureSKeyHeuristic(PrivateKey priv, int bitlength) {
        this.priv = priv;
        this.bitlength = bitlength;
    }

    // Documented SignatureSKey.java

    public byte[] sign(RandomSource randomSource, byte[] ... message) {
        Hashdigest hd = getDigest();

        for (int i = 0; i < message.length; i++) {
            hd.update(message[i], 0, message[i].length);
        }
        return signDigest(randomSource, hd.digest());
    }

    public byte[] signDigest(RandomSource randomSource, byte[] d) {
        try {

            Signature sig = Signature.getInstance("SHA256with" + algorithm);
            sig.initSign(priv);
            sig.update(d);

            return sig.sign();

        } catch (NoSuchAlgorithmException nsae) {
            throw new CryptoError("Failed to instantiate private key!", nsae);
        } catch (InvalidKeyException ike) {
            throw new CryptoError("Invalid private key!", ike);
        } catch (SignatureException se) {
            throw new CryptoError("Failed to compute signature!", se);
        }
    }

    public Hashdigest getDigest() {
        return (new HashfunctionHeuristic("SHA-256")).getDigest();
    }

    // Documented in Marshalizable.java

    public ByteTree toByteTree() {
        return new ByteTree(ByteTree.intToByteTree(bitlength),
                            new ByteTree(priv.getEncoded()));
    }

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(" + algorithm + ", bitlength=" + bitlength + ")";
    }
}