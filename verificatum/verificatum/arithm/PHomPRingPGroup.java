
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
import java.util.*;

import verificatum.crypto.*;
import verificatum.eio.*;

/**
 * Product homomorphism.
 *
 * @author Douglas Wikstrom
 */
public class PHomPRingPGroup implements HomPRingPGroup {

    /**
     * Product of the underlying domains.
     */
    protected PPRing domain;

    /**
     * Product of the underlying ranges.
     */
    protected PPGroup range;

    /**
     * Underlying homomorphisms.
     */
    HomPRingPGroup[] homs;

    /**
     * Creates the product homomorphism of the input homomorphisms.
     *
     * @param homs Underlying homomorphisms.
     */
    public PHomPRingPGroup(HomPRingPGroup ... homs) {
        this.homs = homs;

        PRing[] pRings = new PRing[homs.length];
        for (int i = 0; i < pRings.length; i++) {
            pRings[i] = homs[i].getDomain();
        }
        this.domain = new PPRing(pRings);

        PGroup[] pGroups = new PGroup[homs.length];
        for (int i = 0; i < pGroups.length; i++) {
            pGroups[i] = homs[i].getRange();
        }
        this.range = new PPGroup(pGroups);
    }

    /**
     * Returns an array of the underlying arrays.
     *
     * @return Array of underlying homomorphisms.
     */
    public HomPRingPGroup[] getFactors() {
        return Arrays.copyOfRange(homs, 0, homs.length);
    }

    // Documented in HomPRingPGroup.java

    public PRing getDomain() {
        return domain;
    }

    public PGroup getRange() {
        return range;
    }

    public PGroupElement map(PRingElement element) {
        if (domain.contains(element)) {
            PRingElement[] elements = ((PPRingElement)element).getFactors();

            PGroupElement[] res = new PGroupElement[elements.length];
            for (int i = 0; i < res.length; i++) {
                res[i] = homs[i].map(elements[i]);
            }
            return range.product(res);
        }
        throw new ArithmError("Element not in domain!");
    }

    public ByteTreeBasic toByteTree() {
        ByteTreeBasic[] bts = new ByteTreeBasic[homs.length];

        for (int i = 0; i < bts.length; i++) {
            bts[i] = homs[i].toByteTree();
        }

        return new ByteTreeContainer(homs);
    }

    public void free() {
        for (int i = 0; i < homs.length; i++) {
            homs[i].free();
        }
    }
}
