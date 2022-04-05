
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

package mixnet.arithm;

import java.io.*;
import java.security.*;
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;

import mixnet.test.*;

/**
 * Tests some of the functionality of {@link ModPGroup}.
 *
 * @author Douglas Wikstrom
 */
public class TestPGroup {

    protected static PGroup pGroup = null;
    protected static RandomSource rs;

    protected static void setupPGroup(TestParameters tp)
        throws Exception {
        if (pGroup == null) {
            rs = new PRGHeuristic();
            pGroup = JECPGroupParams.getJECPGroup("prime256v1");
        }
    }

    public static boolean expProd(TestParameters tp)
        throws Exception {
        setupPGroup(tp);

        PField pField = pGroup.getPRing().getPField();


        for (int size = 1; size < 100; size += 13) {

            PGroupElement[] bases =
                pGroup.randomElementArray(size, rs, 20).elements();

            PFieldElement[] exponents =
                pField.randomElementArray(size, rs, 20).elements();


            PGroupElement res1 = pGroup.naiveExpProd(bases, exponents);
            PGroupElement res2 = pGroup.expProd(bases, exponents);

            if (!res1.equals(res2)) {
                return false;
            }
        }

        return true;
    }

    public static boolean fixedBaseExp(TestParameters tp)
        throws Exception {
        setupPGroup(tp);

        PField pField = pGroup.getPRing().getPField();

        PGroupElement basis = pGroup.randomElement(rs, 20);

        for (int size = 1; size < 100; size += 13) {

            PFieldElement[] exponents =
                pField.randomElementArray(size, rs, 20).elements();

            PGroupElement[] res1 = null;
            PGroupElement[] res2 = null;

            res1 = basis.naiveExp(exponents);
            res2 = basis.exp(exponents);

            for (int i = 0; i < size; i++) {
                if (!res1[i].equals(res2[i])) {
                    return false;
                }
            }
        }
        return true;
    }
}
