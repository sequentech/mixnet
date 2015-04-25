
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

package verificatum.util;

import java.util.*;

import verificatum.test.*;

/**
 * Tests the minimal JSON implementation.
 *
 * @author Douglas Wikstrom
 */
public class TestSimpleJSON {

    public static boolean toAndFrom(TestParameters tp)
        throws Exception {

        TreeMap<String, String> map1 = new TreeMap<String, String>();

        map1.put("hej1", "hopp1");
        map1.put("hej2", "hopp2");
        map1.put("hej3", "hopp3");

        String mapString1 = SimpleJSON.toJSON(map1);

        TreeMap<String, String> map2 = SimpleJSON.fromJSON(mapString1);

        String mapString2 = SimpleJSON.toJSON(map2);

        return mapString1.equals(mapString2);
    }
}

