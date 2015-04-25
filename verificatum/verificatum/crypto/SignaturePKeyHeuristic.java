
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

import java.security.*;
import java.security.spec.*;

import verificatum.eio.*;
import verificatum.ui.*;

/**
 * Wrapper of a standard signature schemes. Currently, this means RSA
 * full domain hash based on SHA-256.
 *
 * @author Douglas Wikstrom
 */
public class SignaturePKeyHeuristic implements SignaturePKey {

    /**
     * Maximum number of bytes in public key.
     */
    protected static final int MAX_PKEY_BYTELENGTH = 100 * 1024;

    /**
     * Underlying "signature algorithm".
     */
    protected static String algorithm = "RSA";

    /**
     * Bit length of modulus.
     */
    protected int bitlength;

    /**
     * Encapsulated public key.
     */
    protected PublicKey pub;

    /**
     * Constructs an instance corresponding to the input.
     *
     * @param btr Representation of an instance.
     * @return Public signature key.
     * @throws CryptoFormatException If the input does not represent
     * an instance.
     */
    public static SignaturePKeyHeuristic newInstance(ByteTreeReader btr)
        throws CryptoFormatException {
        try {

            int bitlength = btr.getNextChild().readInt();

            ByteTreeReader kbtr = btr.getNextChild();
            if (kbtr.getRemaining() > MAX_PKEY_BYTELENGTH) {
                throw new CryptoFormatException("Too long key!");
            }

            byte[] keyBytes = kbtr.read();

            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

            KeyFactory factory = KeyFactory.getInstance(algorithm);

            return new SignaturePKeyHeuristic(factory.generatePublic(spec),
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
     * Creates an instance with the given parameters.
     *
     * @param pub Encapsulated public key.
     * @param bitlength Bit length of modulus of public key.
     */
    public SignaturePKeyHeuristic(PublicKey pub, int bitlength) {
        this.pub = pub;
        this.bitlength = bitlength;
    }

    // Documented in SignaturePKey.java

    public boolean verify(byte[] signature, byte[] ... message) {
        Hashdigest hd = getDigest();

        for (int i = 0; i < message.length; i++) {
            hd.update(message[i], 0, message[i].length);
        }
        return verifyDigest(signature, hd.digest());
    }

    public boolean verifyDigest(byte[] signature, byte[] d) {
        try {

            Signature sig = Signature.getInstance("SHA256with" + algorithm);
            sig.initVerify(pub);
            sig.update(d);

            return sig.verify(signature);

        } catch (NoSuchAlgorithmException nsae) {
            throw new CryptoError("Failed to instantiate public key!", nsae);
        } catch (InvalidKeyException ike) {
            throw new CryptoError("Invalid public key!", ike);
        } catch (SignatureException se) {
            return false;
        }
    }

    public Hashdigest getDigest() {
        return (new HashfunctionHeuristic("SHA-256")).getDigest();
    }


    // Documented in Marshalizable.java

    public ByteTree toByteTree() {
        return new ByteTree(ByteTree.intToByteTree(bitlength),
                            new ByteTree(pub.getEncoded()));
    }

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) +
            "(" + algorithm + ", bitlength=" + bitlength + ")";
    }
}
