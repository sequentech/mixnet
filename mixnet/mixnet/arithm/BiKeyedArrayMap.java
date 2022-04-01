
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
 * Bilinear map capturing a keyed map which is applied element-wise to
 * an array of group elements (and possibly to a matching array of
 * ring elements).
 *
 * @author Douglas Wikstrom
 */
public abstract class BiKeyedArrayMap extends BiPRingPGroup {

    /**
     * Bilinear map capturing the underlying key generation algorithm.
     */
    protected BiPRingPGroup keyBi;

    /**
     * Ring containing the elements of the array of ring elements.
     */
    protected PRing arrayPRingDomain;

    /**
     * Group containing the elements of the array of group elements.
     */
    protected PGroup arrayPGroupDomain;

    /**
     * Group containing the elements of the array given as output.
     */
    protected PGroup arrayRange;

    /**
     * Number of elements that can be mapped.
     */
    protected int size;

    /**
     * Creates an instance with the given underlying group.
     *
     * @param keyBi Underlying key generation algorithm.
     * @param arrayPRingDomain Ring containing the elements of the
     * array of ring elements. A value of null indicates that the map
     * does not take an array of ring elements as input.
     * @param arrayPGroupDomain Group containing the elements of the
     * array of group elements.
     * @param arrayRange Group containing the elements of the array
     * given as output.
     * @param size Number of elements that can be mapped.
     */
    protected BiKeyedArrayMap(BiPRingPGroup keyBi,
                              PRing arrayPRingDomain,
                              PGroup arrayPGroupDomain,
                              PGroup arrayRange,
                              int size) {
        this.keyBi = keyBi;
        this.arrayPRingDomain = arrayPRingDomain;
        this.arrayPGroupDomain = arrayPGroupDomain;
        this.arrayRange = arrayRange;
        this.size = size;
    }

    /**
     * Returns the bilinear map corresponding to key generation of
     * this keyed bilinear map.
     *
     * @return Bilinear map corresponding to key generation.
     */
    public BiPRingPGroup getBiKey() {
        return keyBi;
    }

    /**
     * Actual definition of the map.
     *
     * @param secretKey Secret key of the keyed map.
     * @param basicPublicKey Basic public key of the scheme.
     * @param arrayPRingElements Array of ring elements to be
     * mapped. If the map does not accept such an input, this can be
     * set to null.
     * @param arrayPGroupElements Array of group elements to be
     * mapped.
     * @return Resulting array.
     */
    protected abstract PGroupElementArray
        arrayMap(PRingElement secretKey,
                 PGroupElement basicPublicKey,
                 PRingElementArray arrayPRingElements,
                 PGroupElementArray arrayPGroupElements);


    // Documented in BiPRingPGroup.java

    public PGroup getArrayRange() {
        return arrayRange;
    }

    public PRing getPRingDomain() {

        if (arrayPRingDomain == null) {

            return keyBi.getPRingDomain();

        } else {

            return new PPRing(keyBi.getPRingDomain(),
                              new APRing(arrayPRingDomain, size));
        }
    }

    public PGroup getPGroupDomain() {

        if (arrayPGroupDomain == null) {

            return keyBi.getPGroupDomain();

        } else {

            return new PPGroup(keyBi.getPGroupDomain(),
                               new APGroup(arrayPGroupDomain, size));
        }
    }

    public PPGroup getRange() {

        return new PPGroup(keyBi.getRange(), new APGroup(arrayRange, size));
    }

    public PGroupElement map(PRingElement pRingElement,
                             PGroupElement pGroupElement) {

        PRingElement secretKey = null;
        PGroupElement basicPublicKey = null;
        PRingElementArray arrayPRingElements = null;
        PGroupElementArray arrayPGroupElements = null;

        if (arrayPRingDomain != null) {

            PPRingElement pPRingElement = (PPRingElement)pRingElement;
            secretKey = pPRingElement.project(0);
            arrayPRingElements =
                ((APRingElement)pPRingElement.project(1)).getContent();
        } else {

            secretKey = pRingElement;
            arrayPRingElements = null;

        }

        if (arrayPGroupDomain != null) {

            PPGroupElement pPGroupElement = (PPGroupElement)pGroupElement;
            basicPublicKey = pPGroupElement.project(0);
            arrayPGroupElements =
                ((APGroupElement)pPGroupElement.project(1)).getContent();

        } else {

            basicPublicKey = pGroupElement;

        }

        PGroupElement publicKey = keyBi.map(secretKey, basicPublicKey);

        PGroupElementArray arrayRes = arrayMap(secretKey,
                                               basicPublicKey,
                                               arrayPRingElements,
                                               arrayPGroupElements);

        PPGroup range = getRange();

        return range.product(publicKey,
                             ((APGroup)range.project(1)).toElement(arrayRes));
    }
}
