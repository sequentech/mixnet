
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
 * Tests some of the functionality of {@link Polynomial}.
 *
 * @author Douglas Wikstrom
 */
public class TestPolynomial {

    public static boolean convertToByteTree(TestParameters tp)
        throws Exception {

        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

        int certainty = 100;

        LargeInteger order = new LargeInteger(tp.testSize, rs);
        order = order.nextPrime(rs, certainty);
        PField pField = new PField(order, rs, certainty);

        for (int i = 1; i < 10; i++) {

            PFieldElement[] array = new PFieldElement[i];

            for (int j = 0; j < i; j++) {
                array[j] = pField.randomElement(rs, 50);
            }

            Polynomial p = new Polynomial(array);
            ByteTreeReader btr = p.toByteTree().getByteTreeReader();

            Polynomial q = new Polynomial(pField, p.getDegree(), btr);
            if (!p.equals(q)) {
                return false;
            }
        }
        return true;
    }
}
