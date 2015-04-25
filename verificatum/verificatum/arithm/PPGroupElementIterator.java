
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
 * Iterator over a {@link PPGroupElementArray}.
 *
 * @author Douglas Wikstrom
 */
public class PPGroupElementIterator implements PGroupElementIterator {

    /**
     * Underlying group.
     */
    protected PPGroup pPGroup;

    /**
     * Underlying iterators.
     */
    protected PGroupElementIterator[] iterators;

    /**
     * Creates an instance over a {@link PPGroupElementArray}.
     *
     * @param pPGroup Underlying group.
     * @param iterators Underlying iterators.
     */
    public PPGroupElementIterator(PPGroup pPGroup,
				  PGroupElementIterator[] iterators) {
	this.pPGroup = pPGroup;
	this.iterators = iterators;
    }

    // Documented in PGroupElementIterator.java

    public PGroupElement next() {
	PGroupElement[] res = new PGroupElement[iterators.length];
	for (int i = 0; i < res.length; i++) {
	    res[i] = iterators[i].next();
	}
	if (res[0] != null) {
	    return pPGroup.product(res);
	} else {
	    return null;
	}
    }
}

