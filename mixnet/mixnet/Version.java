
// FORK copyright
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

package mixnet;

/**
 * Programmatic access to the version of this package.
 *
 * @author Douglas Wikstrom
 */
public class Version {

    /**
     * Package version string of this package.
     */
    public static final String packageVersion = "1.0.7";

    /**
     * Returns the major version number as an integer.
     *
     * @return Major version number
     */
    public static int major(String packageVersion) {
        return Integer.valueOf(packageVersion.split(",")[0]);
    }

    /**
     * Returns the minor version number as an integer.
     *
     * @return Minor version number
     */
    public static int minor(String packageVersion) {
        return Integer.valueOf(packageVersion.split(",")[1]);
    }

    /**
     * Returns the revision version number as an integer.
     *
     * @return Revision version number
     */
    public static int revision(String packageVersion) {
        return Integer.valueOf(packageVersion.split(",")[2]);
    }
}