
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

/**
 * Iterator over a {@link PPRingElementArray}.
 *
 * @author Douglas Wikstrom
 */
public class PPRingElementIterator implements PRingElementIterator {

    /**
     * Underlying ring.
     */
    protected PPRing pPRing;

    /**
     * Underlying iterators.
     */
    protected PRingElementIterator[] iterators;

    /**
     * Creates an instance over a {@link PPRingElementArray}.
     *
     * @param pPRing Underlying ring.
     * @param iterators Underlying iterators.
     */
    public PPRingElementIterator(PPRing pPRing,
				 PRingElementIterator[] iterators) {
	this.pPRing = pPRing;
	this.iterators = iterators;
    }

    // Documented in PRingElementIterator.java

    public PRingElement next() {
	PRingElement[] res = new PRingElement[iterators.length];
	for (int i = 0; i < res.length; i++) {
	    res[i] = iterators[i].next();
	}
	if (res[0] != null) {
	    return pPRing.product(res);
	} else {
	    return null;
	}
    }
}

