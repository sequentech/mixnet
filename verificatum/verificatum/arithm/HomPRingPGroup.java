
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

import verificatum.eio.*;

/**
 * Interface for a homomorphism from a {@link PRing} to a {@link
 * PGroup}.
 *
 * @author Douglas Wikstrom
 */
public interface HomPRingPGroup extends ByteTreeConvertible {

    /**
     * Returns the domain of this homomorphism.
     *
     * @return Domain of this homomorphism.
     */
    public abstract PRing getDomain();

    /**
     * Returns the range of this homomorphism.
     *
     * @return Range of this homomorphism.
     */
    public abstract PGroup getRange();

    /**
     * Evaluates the homomorphism at the given point.
     *
     * @param element Point of evaluation.
     * @return Value of the homomorphism at the given point.
     */
    public abstract PGroupElement map(PRingElement element);

    /**
     * Frees resources allocated by this instance. Continued use of a
     * freed instance is undefined.
     */
    public void free();
}
