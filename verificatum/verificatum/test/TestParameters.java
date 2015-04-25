
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

package verificatum.test;

import java.io.*;

/**
 * Container class for global test parameters.
 *
 * @author Douglas Wikstrom
 */
public class TestParameters {

    /**
     * Directory for temporary files created during testing.
     */
    public File tmpDir;

    /**
     * Seed used for main pseudo-random generator. This is used for
     * debugging.
     */
    public String prgseed;

    /**
     * Decides the size of a test. Exactly what "size" means depends
     * on what is tested.
     */
    public int testSize;

    /**
     * Create test parameters.
     *
     * @param prgseed Seed used in tests.
     * @param testSize Universal test size (intepretation depends on
     * the test).
     * @param tmpDir Temporary working directory.
     */
    public TestParameters(String prgseed, int testSize, File tmpDir) {
        this.prgseed = prgseed;
        this.testSize = testSize;
        this.tmpDir = tmpDir;
    }
}
