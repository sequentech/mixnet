
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

package vfork.ui.info;

import vfork.protocol.*;

/**
 * A integer data field that can be used in an XML configuration file.
 *
 * @author Douglas Wikstrom
 */
public class IntField extends InfoField {

    /**
     * Smallest integer that can be stored in this instance (inklusive
     * lower bound).
     */
    protected String minInclusive;

    /**
     * Upper bound on the integer that can be stored in this instance
     * (exklusive upper bound).
     */
    protected String maxExclusive;

    /**
     * Indicates if there are any bounds on this field.
     */
    protected boolean bounded;

    /**
     * Creates an instance with unbounded value.
     *
     * @param name Name of instance.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used.
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used.
     */
    public IntField(String name, int minOccurs, int maxOccurs) {
        super(name, minOccurs, maxOccurs);
        bounded = false;
    }

    /**
     * Creates an instance with unbounded value.
     *
     * @param name Name of instance.
     * @param description Description of field.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used.
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used.
     */
    public IntField(String name, String description,
                    int minOccurs, int maxOccurs) {
        super(name, description, minOccurs, maxOccurs);
        bounded = false;
    }

    /**
     * Creates an instance with double sided bounds on value.
     *
     * @param name Name of instance.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used (exklusive).
     * @param minInclusive Minimal value that this field represents
     * (inklusive).
     * @param maxExclusive Upper bound on the value that this field
     * represents (exklusive).
     */
    public IntField(String name, int minOccurs, int maxOccurs,
                    int minInclusive, int maxExclusive) {
        this(name, "", minOccurs, maxOccurs, minInclusive, maxExclusive);
    }

    /**
     * Creates an instance with double sided bounds on value.
     *
     * @param name Name of instance.
     * @param description Description of field.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used (exklusive).
     * @param minInclusive Minimal value that this field represents
     * (inklusive).
     * @param maxExclusive Upper bound on the value that this field
     * represents (exklusive).
     */
    public IntField(String name, String description,
                    int minOccurs, int maxOccurs,
                    int minInclusive, int maxExclusive) {
        super(name, description, minOccurs, maxOccurs);
        this.minInclusive = Integer.toString(minInclusive);
        this.maxExclusive = Integer.toString(maxExclusive);
        bounded = true;
    }

    /**
     * Creates an instance with upper bound only.
     *
     * @param name Name of instance.
     * @param description Description of field.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used (exklusive).
     * @param minInclusive Minimal value that this field represents
     * (inklusive).
     * @param maxExclusive Should be <code>"unbounded"</code>, but
     * this parameter is in fact ignored (exklusive).
     */
    public IntField(String name, String description,
                    int minOccurs, int maxOccurs,
                    int minInclusive, String maxExclusive) {
        super(name, description, minOccurs, maxOccurs);
        this.minInclusive = Integer.toString(minInclusive);
        this.maxExclusive = Integer.toString(Integer.MAX_VALUE);
        bounded = true;
    }

    /**
     * Creates an instance with upper bound only.
     *
     * @param name Name of instance.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used (exklusive).
     * @param minInclusive Minimal value that this field represents
     * (inklusive).
     * @param maxExclusive Should be <code>"unbounded"</code>, but
     * this parameter is in fact ignored (exklusive).
     */
    public IntField(String name, int minOccurs, int maxOccurs,
                    int minInclusive, String maxExclusive) {
        this(name, "", minOccurs, maxOccurs, minInclusive, maxExclusive);
    }

    /**
     * Creates an instance with lower bound only.
     *
     * @param name Name of instance.
     * @param minOccurs Minimum number of times the field must occur
     * in the context it is used (inklusive).
     * @param maxOccurs Strict upper bound on the number of times the
     * field may occur in the context it is used (exklusive).
     * @param minInclusive Should be <code>"unbounded"</code>, but
     * this parameter is in fact ignored (inklusive).
     * @param maxExclusive Upper bound on the value that this field
     * represents (exklusive).
     */
    public IntField(String name, int minOccurs, int maxOccurs,
                    String minInclusive, int maxExclusive) {
        super(name, minOccurs, maxOccurs);
        this.minInclusive = Integer.toString(Integer.MIN_VALUE);
        this.maxExclusive = Integer.toString(maxExclusive);
        bounded = true;
    }

    // These methods are documented in the super class.

    public String schemaElementString() {
        StringBuffer sb = new StringBuffer();

        String typeName = name + "Type";
        sb.append("<xs:element name=\"" + name + "\"\n");
        if (!bounded) {
            sb.append("            type=\"xs:integer\"\n");
        }
        sb.append("            minOccurs=\"" + minOccurs + "\"\n");
        sb.append("            maxOccurs=\"" + maxOccurs + "\"");

        if (!bounded) {
            sb.append("/>");
        }

        if (bounded) {
            sb.append(">\n");
            sb.append("<xs:simpleType>\n");
            sb.append("   <xs:restriction base=\"xs:integer\">\n");
            sb.append("      <xs:minInclusive value=\""
                      + minInclusive + "\"/>\n");
            sb.append("      <xs:maxExclusive value=\""
                      + maxExclusive + "\"/>\n");
            sb.append("   </xs:restriction>\n");
            sb.append("</xs:simpleType>\n");
            sb.append("</xs:element>\n");
        }

        return sb.toString();
    }

    public Object parse(String value) {
        return new Integer(value);
    }
}
