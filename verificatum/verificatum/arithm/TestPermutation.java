
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

package verificatum.arithm;

import java.io.*;
import java.security.*;
import java.util.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;

import verificatum.test.*;

/**
 * Tests some of the functionality of {@link Permutation}.
 *
 * @author Douglas Wikstrom
 */
public class TestPermutation {

    public static boolean maps(TestParameters tp)
        throws Exception {

        RandomSource rs = new PRGHeuristic(tp.prgseed.getBytes());

        Integer[] a = new Integer[tp.testSize];
        Integer[] b = new Integer[a.length];
        Integer[] c = new Integer[a.length];

        for (int i = 0; i < a.length; i++) {
            a[i] = new Integer(i);
        }

        Permutation p = new Permutation(a.length, rs, 10);

        p.applyPermutation(a, b);

        p.inv().applyPermutation(b, c);

        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(c[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean convertToByteTree(TestParameters tp)
        throws Exception {

        RandomSource rs =
            new PRGHeuristic(tp.prgseed.getBytes());

        for (int i = 0; i < tp.testSize; i++) {

            Permutation p = new Permutation(tp.testSize, rs, 10);
            ByteTree bt = p.toByteTree();
            ByteTreeReader btr = new ByteTreeReaderBT(bt);
            Permutation q = new Permutation(p.size(), btr);
            if (!p.equals(q)) {
                return false;
            }
        }
        return true;
    }
}
