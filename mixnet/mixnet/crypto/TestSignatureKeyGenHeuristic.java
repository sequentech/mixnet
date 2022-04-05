
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

import java.io.*;
import java.security.*;
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.test.*;

public class TestSignatureKeyGenHeuristic {


    public static boolean signAndVerify(TestParameters tp) throws Exception {
        SignatureKeyGen keygen = new SignatureKeyGenHeuristic(512);

        RandomSource rs = new RandomDevice();

        SignatureKeyPair keyPair = keygen.gen(rs);

        byte[] message = new byte[123];
        Arrays.fill(message, (byte)0);

        byte[] signature = keyPair.getSKey().sign(rs, message);

        if (keyPair.getPKey().verify(signature, message)) {
            return true;
        } else {
            return false;
        }

    }

    public static boolean marshal(TestParameters tp)
        throws Exception {

        SignatureKeyGen keygen = new SignatureKeyGenHeuristic(512);

        RandomSource rs = new RandomDevice();

        byte[] message = new byte[123];
        Arrays.fill(message, (byte)0);

        SignatureKeyPair keyPair = keygen.gen(rs);

        String skeyString =
            Marshalizer.marshalToHexHuman(keyPair.getSKey(), true);
        SignatureSKey skey = Marshalizer.unmarshalHex_SignatureSKey(skeyString);

        String pkeyString =
            Marshalizer.marshalToHexHuman(keyPair.getPKey(), true);
        SignaturePKey pkey = Marshalizer.unmarshalHex_SignaturePKey(pkeyString);

        byte[] signature = skey.sign(rs, message);

        if (pkey.verify(signature, message)) {
            return true;
        } else {
            return false;
        }
    }
}
