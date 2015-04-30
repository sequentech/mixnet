
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
 * Bilinear map capturing exponentiated products. The multiple group
 * elements and exponents are represented by {@link PPGroupElement}
 * and {@link PPRingElement} instances that must contain the same
 * number of underlying elements and with matching groups and rings.
 *
 * @author Douglas Wikstrom
 */
public class BiExpProd extends BiPRingPGroup {

    /**
     * Underlying group.
     */
    protected PGroup pGroup;

    /**
     * Group domain of map.
     */
    protected PGroup pGroupDomain;

    /**
     * Creates an instance with the given underlying group.
     *
     * @param pGroup Underlying group.
     * @param width Number of bases in exponentiated product.
     */
    public BiExpProd(PGroup pGroup, int width) {
        this.pGroup = pGroup;
        this.pGroupDomain = new PPGroup(pGroup, width);
    }


    // Documented in BiPRingPGroup.java

    public PRing getPRingDomain() {
        return pGroupDomain.getPRing();
    }

    public PGroup getPGroupDomain() {
        return pGroupDomain;
    }

    public PGroup getRange() {
        return pGroup;
    }

    public PGroupElement map(PRingElement ringElement,
                             PGroupElement groupElement) {
        if (!groupElement.getPGroup().equals(pGroupDomain)
            || !ringElement.getPRing().equals(pGroupDomain.getPRing())) {
            throw new ArithmError("Inputs not in domains!");
        }
        PRingElement[] ringFactors =
            ((PPRingElement)ringElement).getFactors();
        PGroupElement[] groupFactors =
            ((PPGroupElement)groupElement).getFactors();

        PGroupElement res = pGroup.getONE();
        for (int i = 0; i < groupFactors.length; i++) {
            res = res.mul(groupFactors[i].exp(ringFactors[i]));
        }
        return res;
    }
}
