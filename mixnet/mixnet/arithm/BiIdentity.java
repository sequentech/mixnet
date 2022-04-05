
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
 * Bilinear map that maps the group element to itself for any ring
 * element.
 *
 * @author Douglas Wikstrom
 */
public class BiIdentity extends BiPRingPGroup {

    /**
     * Underlying group.
     */
    protected PGroup pGroup;

    /**
     * Creates an instance with the given underlying group.
     *
     * @param pGroup Underlying group.
     */
    public BiIdentity(PGroup pGroup) {
        this.pGroup = pGroup;
    }


    // Documented in BiPRingPGroup.java

    public PRing getPRingDomain() {
        return pGroup.getPRing();
    }

    public PGroup getPGroupDomain() {
        return pGroup;
    }

    public PGroup getRange() {
        return pGroup;
    }

    public PGroupElement map(PRingElement ringElement,
                             PGroupElement groupElement) {
        return groupElement;
    }
}
