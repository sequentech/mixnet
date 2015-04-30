
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

package vfork.arithm;

import java.io.*;
import java.security.*;
import java.util.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;

import vfork.test.*;

/**
 * Tests some of the functionality of {@link PPRing}.
 *
 * @author Douglas Wikstrom
 */
public class TestPPRing {

    protected static PRing pRing = null;
    protected static RandomSource rs;

    protected static void setupPRing(TestParameters tp) throws Exception {
        if (pRing == null) {
            rs = new PRGHeuristic(tp.prgseed.getBytes());

            int certainty = 100;

            LargeInteger order = new LargeInteger(tp.testSize, rs);
            order = order.nextPrime(rs, certainty);

            PField pField = new PField(order, rs, certainty);
            pRing = new PPRing(new PPRing(pField, pField), pField);
        }
    }

    public static boolean convertToByteTree(TestParameters tp)
            throws Exception {

            setupPRing(tp);

            for (int i = 0; i < tp.testSize; i++) {
                PRingElement a = pRing.randomElement(rs, 50);
                ByteTreeReader btr = a.toByteTree().getByteTreeReader();

                PRingElement b = pRing.toElement(btr);

                if (!a.equals(b)) {
                    return false;
                }
            }
            return true;
    }
}
