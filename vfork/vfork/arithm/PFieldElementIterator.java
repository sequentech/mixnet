
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

/**
 * Iterator over a {@link PFieldElementArray}.
 *
 * @author Douglas Wikstrom
 */
public class PFieldElementIterator implements PRingElementIterator {

    /**
     * Underlying group.
     */
    protected PField pField;

    /**
     * Underlying iterator over integers.
     */
    protected LargeIntegerIterator integerIterator;

    /**
     * Creates an instance over a {@link PFieldElementArray}.
     *
     * @param pField Underlying group.
     * @param integerIterator Underlying integer iterator.
     */
    public PFieldElementIterator(PField pField,
				 LargeIntegerIterator integerIterator) {
	this.pField = pField;
	this.integerIterator = integerIterator;
    }

    // Documented in PFieldElementIterator.java

    public PFieldElement next() {
	LargeInteger integer = integerIterator.next();
	if (integer != null) {
	    return pField.toElement(integer);
	} else {
	    return null;
	}
    }
}

