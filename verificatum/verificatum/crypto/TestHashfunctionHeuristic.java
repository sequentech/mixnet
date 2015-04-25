
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

public class TestHashfunctionHeuristic {

    public static boolean marshal(TestParameters tp)
        throws Exception {

        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

        Hashfunction hf256 = new HashfunctionHeuristic("SHA-256");
        Hashfunction hf384 = new HashfunctionHeuristic("SHA-384");
        Hashfunction hf512 = new HashfunctionHeuristic("SHA-512");

        ByteTreeBasic bt256 = Marshalizer.marshal(hf256);
        ByteTreeBasic bt384 = Marshalizer.marshal(hf384);
        ByteTreeBasic bt512 = Marshalizer.marshal(hf512);

        ByteTreeReader btr256 = bt256.getByteTreeReader();
        ByteTreeReader btr384 = bt384.getByteTreeReader();
        ByteTreeReader btr512 = bt512.getByteTreeReader();

        Hashfunction hf256_t =
            Marshalizer.unmarshalAux_Hashfunction(btr256, rs, 50);
        Hashfunction hf384_t =
            Marshalizer.unmarshalAux_Hashfunction(btr384, rs, 50);
        Hashfunction hf512_t =
            Marshalizer.unmarshalAux_Hashfunction(btr512, rs, 50);

        for (int i = 100; i < 500; i++) {
            byte[] input = rs.getBytes(i);

            byte[] output;
            output = hf256_t.hash(input);
            //  output = hf384_t.hash(input);
            //output = hf512_t.hash(input);
        }
        return true;
    }
}
