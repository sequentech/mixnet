
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

import java.io.*;
import java.security.*;
import java.util.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.test.*;

public class TestCramerShoup {

    protected static PGroup pGroup;
    protected static RandomSource rs;
    protected static Hashfunction crhf;

    protected static void setup(TestParameters tp)
        throws Exception {
        if (pGroup == null) {
            rs = new PRGHeuristic(tp.prgseed.getBytes());
            pGroup = new ModPGroup(tp.testSize);
            crhf = new HashfunctionHeuristic("SHA-256");
        }
    }

    public static boolean marshalKeyGen(TestParameters tp) throws Exception {

        setup(tp);

        int statDist = 100;

        CryptoKeyGenCramerShoup keyGen1 =
            new CryptoKeyGenCramerShoup(pGroup, crhf);

        ByteTreeBasic keyGenBT = Marshalizer.marshal(keyGen1);
        ByteTreeReader reader = keyGenBT.getByteTreeReader();

        CryptoKeyGenCramerShoup keyGen2 =
            (CryptoKeyGenCramerShoup)
            Marshalizer.unmarshalAux_CryptoKeyGen(reader, rs, statDist);

        if (!keyGen1.pGroup.equals(keyGen2.pGroup)) {
            return false;
        }

        // Compare hashfunctions by hashing something
        byte[] input = new byte[1];
        input[0] = 123;
        byte[] out1 = keyGen1.crhf.hash(input);
        byte[] out2 = keyGen2.crhf.hash(input);

        if (!Arrays.equals(out1, out2)) {
            return false;
        }

        return true;
    }

    public static boolean encryption(TestParameters tp) throws Exception {
        setup(tp);

        int statDist = 100;

        CryptoKeyGen keyGen = new CryptoKeyGenCramerShoup(pGroup, crhf);

        CryptoKeyPair keyPair = keyGen.gen(rs, statDist);

        byte[] message = null;
        byte[] label = null;

        for (int i = 1; i < tp.testSize; i++) {

            message = new byte[i];
            label = new byte[i];

            for (int j = 0; j < message.length; j++) {
                message[j] = (byte)j;
                label[j] = (byte)(j * j);
            }

            byte[] ciphertext =
                keyPair.getPKey().encrypt(label, message, rs, statDist);

            byte[] plaintext =
                keyPair.getSKey().decrypt(label, ciphertext);

            if (!Arrays.equals(message, plaintext)) {
                return false;
            }
        }
        return true;
    }

    public static boolean marshalKeys(TestParameters tp) throws Exception {

        setup(tp);

        int statDist = 100;

        CryptoKeyGen keyGen = new CryptoKeyGenCramerShoup(pGroup, crhf);

        CryptoKeyPair keyPair = keyGen.gen(rs, statDist);
        CryptoSKeyCramerShoup skey1 = (CryptoSKeyCramerShoup)keyPair.getSKey();
        CryptoPKeyCramerShoup pkey1 = (CryptoPKeyCramerShoup)keyPair.getPKey();


        ByteTreeBasic skeyBT = Marshalizer.marshal(keyPair.getSKey());
        ByteTreeReader sreader = skeyBT.getByteTreeReader();
        CryptoSKeyCramerShoup skey2 =
            (CryptoSKeyCramerShoup)
            Marshalizer.unmarshalAux_CryptoSKey(sreader, rs, statDist);

        if (!skey1.z.equals(skey2.z)
            || !skey1.x1.equals(skey2.x1)
            || !skey1.x2.equals(skey2.x2)
            || !skey1.y1.equals(skey2.y1)
            || !skey1.y2.equals(skey2.y2)) {
            return false;
        }


        ByteTreeBasic pkeyBT = Marshalizer.marshal(keyPair.getPKey());
        ByteTreeReader preader = pkeyBT.getByteTreeReader();
        CryptoPKeyCramerShoup pkey2 =
            (CryptoPKeyCramerShoup)
            Marshalizer.unmarshalAux_CryptoPKey(preader, rs, statDist);

        if (!pkey1.g1.equals(pkey2.g1)
            || !pkey1.g2.equals(pkey2.g2)
            || !pkey1.h.equals(pkey2.h)
            || !pkey1.c.equals(pkey2.c)
            || !pkey1.d.equals(pkey2.d)) {
            return false;
        }


        return true;

    }
}
