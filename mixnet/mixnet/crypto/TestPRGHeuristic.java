
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

public class TestPRGHeuristic {

    public static boolean generate(TestParameters tp) throws Exception {

        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

        PRGHeuristic prg = new PRGHeuristic();

        byte[] seed = rs.getBytes(prg.minNoSeedBytes());
        prg.setSeed(seed);

        for (int i = 0; i < 100; i++) {
            byte[] rand = prg.getBytes(tp.testSize);
        }
        return true;
    }

    public static boolean marshal(TestParameters tp)
        throws Exception {
        PRG prg1;
        PRG prg2;

        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());
        int certainty = 100;

        prg1 = new PRGHeuristic();
        byte[] seedBytes = rs.getBytes(prg1.minNoSeedBytes());
        prg1.setSeed(seedBytes);

        ByteTreeBasic byteTree = Marshalizer.marshal(prg1);
        prg2 = Marshalizer.unmarshalAux_PRG(byteTree.getByteTreeReader(),
                                            rs, certainty);

        prg2.setSeed(seedBytes);
        byte[] r1 = prg1.getBytes(100);
        byte[] r2 = prg2.getBytes(100);

        return true;
    }

}
