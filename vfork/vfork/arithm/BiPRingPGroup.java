
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

import vfork.eio.*;

/**
 * Abstract class for a bilinear map from the product of a {@link
 * PRing} and {@link PGroup} to a {@link PGroup}.
 *
 * @author Douglas Wikstrom
 */
public abstract class BiPRingPGroup {

    /**
     * Returns the ring part of the domain of this map.
     *
     * @return Ring part of the domain of this map.
     */
    public abstract PRing getPRingDomain();

    /**
     * Returns the group part of the domain of this map.
     *
     * @return Group part of the domain of this map.
     */
    public abstract PGroup getPGroupDomain();

    /**
     * Returns the range of this map.
     *
     * @return Range of this map.
     */
    public abstract PGroup getRange();

    /**
     * Evaluates the map at the given point.
     *
     * @param ringElement Ring element input.
     * @param groupElement Group element input.
     * @return Value of the map at the given point.
     */
    public abstract PGroupElement map(PRingElement ringElement,
                                      PGroupElement groupElement);

    /**
     * Returns the homomorphism resulting from restricting this
     * bilinear map.
     *
     * @param groupElement Restriction of this bilinear map.
     * @return Resulting homomorphism.
     */
    public HomPRingPGroup restrict(PGroupElement groupElement) {
        return new HomPRingPGroupRest(this, groupElement);
    }

    /**
     * Returns the homomorphism resulting from restricting this
     * bilinear map.
     *
     * @param ringElement Restriction of this bilinear map.
     * @return Resulting homomorphism.
     */
    public HomPGroupPGroup restrict(PRingElement ringElement) {
        return new HomPGroupPGroupRest(this, ringElement);
    }
}
