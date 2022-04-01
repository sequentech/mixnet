
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
public class TestModPGroup {

    protected static ModPGroup pGroup = null;
    protected static RandomSource rs;

    protected static void setupModPGroup(TestParameters tp)
        throws Exception {
        if (pGroup == null) {
            rs = new PRGHeuristic(tp.prgseed.getBytes());
            pGroup = new ModPGroup(tp.testSize);
        }
    }

    public static boolean convertToByteTree(TestParameters tp)
        throws Exception {

        setupModPGroup(tp);

        for (int i = 0; i < tp.testSize; i++) {
            PGroupElement a = pGroup.randomElement(rs, 50);
            ByteTreeReader btr = a.toByteTree().getByteTreeReader();

            PGroupElement b = pGroup.toElement(btr);
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }
}
