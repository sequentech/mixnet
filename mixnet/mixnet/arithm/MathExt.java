
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
 * Mathematical utility functions for primitive types.
 *
 * @author Douglas Wikstrom
 */
public class MathExt {

    /**
     * Returns the least upper bound of the binary logarithm of the
     * input, which is assumed to be positive. The output is undefined
     * otherwise.
     *
     * @param x Integer of which the logarithmic upper bound is requested.
     * @return Least upper bound on the binary logarithm of
     * the input.
     */
    public static int log2c(int x) {
        int mask = 0x80000000;
        int res = 32;
        while ((x & mask) == 0 && res >= 1) {
            mask >>>= 1;
            res--;
        }
        return res;
    }
}
