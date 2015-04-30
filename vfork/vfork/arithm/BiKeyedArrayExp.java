
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

import vfork.crypto.*;
import vfork.eio.*;

/**
 * Bilinear map capturing exponentiation with a single exponent.
 *
 * @author Douglas Wikstrom
 */
public abstract class BiKeyedArrayExp extends BiKeyedArrayMap {

    /**
     * Creates an instance with the given underlying group.
     *
     * @param pGroup Underlying group.
     * @param size Number of elements in input.
     */
    public BiKeyedArrayExp(PGroup pGroup, int size) {
        super(new BiExp(pGroup), null, pGroup, pGroup, size);
    }


    // Documented in BiKeyedArrayMap.java

    protected PGroupElementArray
        arrayMap(PRingElement secretKey,
                 PGroupElement basicPublicKey,
                 PRingElementArray arrayPRingElements,
                 PGroupElementArray arrayPGroupElements) {
        return arrayPGroupElements.exp(secretKey);
    }
}
