
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

package vfork.eio;

/**
 * Interface capturing the ability of the instance of a class to be
 * marshalled into a byte tree with type information that can later be
 * restored to the instance. This is useful to, e.g., be able to
 * obliviously recover an instance of a subclass of {@link PGroup} of
 * the precise subclass.
 *
 * <p>
 *
 * <b>Convention.</b> For this to be possible we require by convention
 * that every class that implements this interface also implements a
 * static method with one of the following signatures:
 *
 * <p>
 *
 * <code>public static {@link Object} newInstance({@link ByteTreeReader} btr, {@link RandomSource} rs, int certainty)</code><br>
 * <code>public static {@link Object} newInstance({@link ByteTreeReader} btr)</code>
 *
 * <p>
 *
 * The first method allows probabilistically checking the input using
 * the given source of randomness. The error probability should be
 * bounded by <i>2<sup>-<code>certainty</code></sup></i>.
 *
 * @author Douglas Wikstrom
 */
public interface Marshalizable extends ByteTreeConvertible {

    /**
     * Returns a brief human friendly description of this
     * instance. This is merely a comment which can not be used to
     * recover the instance.
     *
     * @param verbose Decides if the description should be verbose or
     * not.
     * @return Human friendly description of this instance.
     */
    public abstract String humanDescription(boolean verbose);
}