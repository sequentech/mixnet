
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
 * Interface for an iterator over a {@link LargeIntegerArray}.
 *
 * @author Douglas Wikstrom
 */
public class LargeIntegerIteratorIM implements LargeIntegerIterator {

    /**
     * Current index.
     */
    protected int current;

    /**
     * Underlying array.
     */
    protected LargeIntegerArrayIM array;

    /**
     * Creates an iterator reading from the given array.
     *
     * @param array Underlying array.
     */
    public LargeIntegerIteratorIM(LargeIntegerArrayIM array) {
	this.array = array;
	this.current = 0;
    }

    // Documented in LargeIntegerIterator.java

    public LargeInteger next() {
	if (current < array.li.length) {
	    return array.li[current++];
	} else {
	    return null;
	}
    }

    public boolean hasNext() {
        return current < array.li.length;
    }

    public void close() {}
}

