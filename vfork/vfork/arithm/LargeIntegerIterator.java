
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

/**
 * Interface for an iterator over a {@link LargeIntegerArray}.
 *
 * @author Douglas Wikstrom
 */
public interface LargeIntegerIterator {

    /**
     * Returns the next integer, or null if no more integers are
     * available.
     *
     * @return Next integer.
     */
    public abstract LargeInteger next();

    /**
     * Returns true or false depending on if there is another integer
     * to read.
     *
     * @return True or false depending on if there is another integer
     * to read.
     */
    public abstract boolean hasNext();

    /**
     * Deallocates any resources allocated by this instance. This
     * method is called automatically when {@link #hasNext()} returns
     * false, but it must be called explicitly if not all elements are
     * processed.
     */
    public abstract void close();
}

