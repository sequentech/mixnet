
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
 * A class that in a natural way is a associated with a {@link PGroup}
 * should implement this interface. It simplifies checking
 * compatibility of associated groups and rings by calling one of the
 * methods {@link PGroup#compatible(PGroupAssociated[])}, {@link
 * PGroup#compatible(PRingAssociated, PGroupAssociated[])}, or {@link
 * PRing#compatible(PGroupAssociated, PRingAssociated[])}.
 *
 * @author Douglas Wikstrom
 */
public interface PGroupAssociated {

    /**
     * Returns the associated group.
     *
     * @return Associated group.
     */
    public PGroup getPGroup();
}
