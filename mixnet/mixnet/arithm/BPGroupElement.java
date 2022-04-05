
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

import mixnet.crypto.*;
import mixnet.eio.*;

/**
 * Abstract class representing an immutable group element in a prime
 * order group. The group is represented by the class {@link
 * BPGroup}.
 *
 * @author Douglas Wikstrom
 */
public abstract class BPGroupElement extends PGroupElement {

    /**
     * Initializes this instance.
     *
     * @param pGroup Group to which this instance will belong.
     */
    protected BPGroupElement(PGroup pGroup) {
        super(pGroup);
    }

    // Documented in PGroupElement.java

    public PGroupElementArray exp(PRingElementArray exponents) {
        return pGroup.toElementArray(exp(exponents.elements()));
    }
}
