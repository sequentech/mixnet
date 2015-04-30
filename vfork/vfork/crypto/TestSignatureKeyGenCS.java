
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

import java.io.*;
import java.security.*;
import java.util.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.test.*;

public class TestSignatureKeyGenCS {


    public static boolean signAndVerify(TestParameters tp) throws Exception {
        SignatureKeyGen keygen =
            new SignatureKeyGenCS(512,
                                  new HashfunctionHeuristic("SHA-256"),
                                  20,
                                  50);

        RandomSource rs = new RandomDevice();

        SignatureKeyPair keyPair = keygen.gen(rs);

        for (int i = 0; i < 1000; i++) {

            byte[] message = new byte[123];
            Arrays.fill(message, (byte)0);

            byte[] signature = keyPair.getSKey().sign(rs, message);

            if (!keyPair.getPKey().verify(signature, message)) {
                return false;
            }
        }
        return true;
    }

    public static boolean marshal(TestParameters tp)
        throws Exception {

        SignatureKeyGen keygen =
            new SignatureKeyGenCS(512,
                                  new HashfunctionHeuristic("SHA-256"),
                                  20,
                                  50);

        RandomSource rs = new RandomDevice();

        byte[] message = new byte[123];
        Arrays.fill(message, (byte)0);

        SignatureKeyPair keyPair = keygen.gen(rs);

        String skeyString =
            Marshalizer.marshalToHexHuman(keyPair.getSKey(), true);
        SignatureSKey skey =
            Marshalizer.unmarshalHexAux_SignatureSKey(skeyString, rs, 50);

        String pkeyString =
            Marshalizer.marshalToHexHuman(keyPair.getPKey(), true);
        SignaturePKey pkey =
            Marshalizer.unmarshalHexAux_SignaturePKey(pkeyString, rs, 50);

        byte[] signature = skey.sign(rs, message);

        if (pkey.verify(signature, message)) {
            return true;
        } else {
            return false;
        }
    }
}
