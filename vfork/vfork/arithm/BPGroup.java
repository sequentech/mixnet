
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
import java.lang.reflect.*;
import java.util.*;

import vfork.crypto.*;
import vfork.eio.*;

/**
 * Abstract class representing a basic group for cryptographic
 * use. Elements in the group are represented by the abstract class
 * {@link BPGroupElement}. The {@link BPGroupElementArray}-class is
 * used for arrays of elements.
 *
 * @author Douglas Wikstrom
 */
public abstract class BPGroup extends PGroup {

    /**
     * Creates a group. It is the responsibility of the programmer to
     * initialize this instance by calling {@link PGroup#init(PRing)}.
     */
    protected BPGroup() {}

    /**
     * Creates a group with the given associated ring.
     *
     * @param pRing Ring associated with this instance.
     */
    protected BPGroup(PRing pRing) {
        super(pRing);
    }


    // Documented in PGroup.java

    public PGroupElementArray toElementArray(PGroupElement[] elements) {
        return new BPGroupElementArray(this, elements);
    }

    public PGroupElementArray toElementArray(PGroupElementArray ... arrays) {

        int total = 0;
        for (int i = 0; i < arrays.length; i++) {
            total += arrays[i].size();
        }

        PGroupElement[] res = new PGroupElement[total];
        int offset = 0;
        for (int i = 0; i < arrays.length; i++) {
            int len = arrays[i].size();
            System.arraycopy(arrays[i].elements(), 0,
                             res, offset,
                             len);
            offset += len;
        }
        return new BPGroupElementArray(this, res);
    }

    public PGroupElementArray toElementArray(int size, ByteTreeReader btr)
        throws ArithmFormatException {
        return new BPGroupElementArray(this, size, btr);
    }

    public PGroupElementArray toElementArray(int size, PGroupElement element) {
        PGroupElement[] res = new PGroupElement[size];
        Arrays.fill(res, element);
        return new BPGroupElementArray(this, res);
    }
}
