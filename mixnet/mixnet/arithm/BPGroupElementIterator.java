
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

/**
 * Iterator over a {@link BPGroupElementArray}.
 *
 * @author Douglas Wikstrom
 */
public class BPGroupElementIterator implements PGroupElementIterator {

    /**
     * Underlying array.
     */
    protected BPGroupElementArray array;

    /**
     * Index of current element.
     */
    protected int current;

    /**
     * Creates an instance over a {@link BPGroupElementArray}.
     *
     * @param array Underlying array.
     */
    public BPGroupElementIterator(BPGroupElementArray array) {
	this.array = array;
	this.current = 0;
    }

    // Documented in PGroupElementIterator.java

    public PGroupElement next() {
	if (current < array.size()) {
	    return this.array.values[current++];
	} else {
	    return null;
	}
    }
}

