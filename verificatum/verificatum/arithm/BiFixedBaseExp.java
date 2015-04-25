
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

import verificatum.crypto.*;
import verificatum.eio.*;

/**
 * Bilinear map capturing exponentiation of a fixed basis with
 * multiple exponents. The multiple exponents are represented by an
 * {@link APRingElement} instance which must encapsulate elements
 * compatible with the fixed basis.
 *
 * @author Douglas Wikstrom
 */
public class BiFixedBaseExp extends BiPRingPGroup {

    /**
     * Underlying domain group.
     */
    protected PGroup pGroup;

    /**
     * Underlying range group.
     */
    protected APGroup aPGroup;

    /**
     * Domain of map.
     */
    protected APRing aPRing;

    /**
     * Creates an instance with the given underlying group.
     *
     * @param pGroup Underlying group.
     * @param size Size of arrays mapped by this instance.
     */
    public BiFixedBaseExp(PGroup pGroup, int size) {
        this.pGroup = pGroup;
        this.aPRing = new APRing(pGroup.getPRing(), size);
        this.aPGroup = new APGroup(pGroup, size);
    }


    // Documented in BiPRingPGroup.java

    public PRing getPRingDomain() {
        return aPRing;
    }

    public PGroup getPGroupDomain() {
        return pGroup;
    }

    public PGroup getRange() {
        return aPGroup;
    }

    public PGroupElement map(PRingElement ringElement,
                             PGroupElement groupElement) {
        if (ringElement.pRing.equals(aPRing)
            && groupElement.pGroup.equals(pGroup)) {

            PGroupElementArray res =
                groupElement.exp(((APRingElement)ringElement).value);

            return aPGroup.toElement(res);
        }
        throw new ArithmError("Input not contained in domain!");
    }

    public HomPRingPGroup restrict(PGroupElement groupElement) {
        return new HomFixedBaseExp(this, groupElement);
    }
}
