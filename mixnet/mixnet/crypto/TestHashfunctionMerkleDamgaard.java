
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

public class TestHashfunctionMerkleDamgaard {

    public static boolean marshal(TestParameters tp)
	throws Exception {

	RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

	PGroup pGroup = new ModPGroup(tp.testSize);

	HashfunctionFixedLength flhf =
	    new HashfunctionPedersen(pGroup, 5, rs, 50);

	Hashfunction hf1 = new HashfunctionMerkleDamgaard(flhf);

	ByteTreeReader btr = Marshalizer.marshal(hf1).getByteTreeReader();

	Hashfunction hf2 = Marshalizer.unmarshalAux_Hashfunction(btr,
                                                                 rs,
                                                                 50);


	for (int i = 100; i < 500; i++) {
	    byte[] input = rs.getBytes(i);

	    byte[] output1 = hf1.hash(input);
	    byte[] output2 = hf2.hash(input);

	    if (!Arrays.equals(output1, output2)) {
		return false;
	    }
	}
	return true;
    }

    public static boolean hashing(TestParameters tp)
	throws Exception {

	RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

	PGroup pGroup = new ModPGroup(tp.testSize);

	HashfunctionFixedLength flhf =
	    new HashfunctionPedersen(pGroup, 5, rs, 50);

	Hashfunction hf = new HashfunctionMerkleDamgaard(flhf);

	for (int i = 100; i < 500; i++) {
	    byte[] input = rs.getBytes(i);

	    byte[] output = hf.hash(input);
	}
	return true;
    }
}
