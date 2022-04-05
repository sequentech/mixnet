
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

import mixnet.eio.*;

/**
 * Restriction of a bilinear map to a homomorphism.
 *
 * @author Douglas Wikstrom
 */
public class HomPRingPGroupRest implements HomPRingPGroup {

    /**
     * Underlying bilinear map.
     */
    protected BiPRingPGroup bi;

    /**
     * Restriction inducing a homomorphism.
     */
    protected PGroupElement restriction;

    /**
     * Creates the homomorphism from the given restriction.
     *
     * @param bi Underlying bilinear map.
     * @param restriction Restriction of the bilinear map.
     */
    public HomPRingPGroupRest(BiPRingPGroup bi, PGroupElement restriction) {
        this.bi = bi;
        this.restriction = restriction;
    }


    // Documented in HomPRingPGroup.java

    public PRing getDomain() {
        return bi.getPRingDomain();
    }

    public PGroup getRange() {
        return bi.getRange();
    }

    public PGroupElement map(PRingElement element) {
        return bi.map(element, restriction);
    }

    public ByteTreeBasic toByteTree() {
        return restriction.toByteTree();
    }

    public void free() {}
}

