
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

package verificatum.ui.info;

import verificatum.protocol.*;

/**
 * A <code>String</code> data field that can be used in an XML
 * configuration file.
 *
 * @author Douglas Wikstrom
 */
public class StringField extends InfoField {

    /**
     * Creates an instance.
     *
     * @param name Name of instance.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive lower bound).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used (exlusive upper bound).
     */
    public StringField(String name, int minOccurs, int maxOccurs) {
        super(name, minOccurs, maxOccurs);
    }

    /**
     * Creates an instance.
     *
     * @param name Name of instance.
     * @param description Description of this field.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive lower bound).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used (exlusive upper bound).
     */
    public StringField(String name, String description,
                       int minOccurs, int maxOccurs) {
        super(name, description, minOccurs, maxOccurs);
    }

    // These methods are documented in the super class.

    public String schemaElementString() {
        String s = ""
            + "<xs:element name=\"" + name + "\"\n"
            + "            type=\"xs:string\"\n"
            + "            minOccurs=\"" + minOccurs + "\"\n"
            + "            maxOccurs=\"" + maxOccurs + "\"/>";
        return s;
    }

    public Object parse(String value) {
        return value;
    }
}
