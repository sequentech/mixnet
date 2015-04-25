
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

import java.net.*;
import java.util.*;

import org.xml.sax.*;

import verificatum.protocol.*;

/**
 * Represents the public information about a party in a protocol. It
 * stores its name, description, and how to reach it. Additional
 * fields may (and should) of course be added if needed in a
 * particular protocol.
 *
 * @author Douglas Wikstrom
 */
public class PartyInfo extends Info implements Comparable<PartyInfo> {

    /**
     * Name of the party tag.
     */
    public final static String PARTY = "party";

    /**
     * Name of the description tag.
     */
    public final static String DESCRIPTION = "pdescr";

    /**
     * Name of the public key tag.
     */
    public final static String PUB_KEY = "pkey";

    /**
     * Name of the sort-by-role tag.
     */
    public final static String SORT_BY_ROLE = "srtbyrole";

    /**
     * Creates an instance with default fields.
     */
    public PartyInfo() {
        String s = "Sorting attribute used to sort parties with respect to " +
            "their roles in the protocol. This is used to assign roles in " +
            "protocols where different parties play different roles.";
        addInfoFields(new StringField(SORT_BY_ROLE,
                                      s,
                                      1, 1),
                      new StringField(PrivateInfo.NAME,
                                      PrivateInfo.NAME_DESCRIPTION,
                                      1, 1),
                      new StringField(DESCRIPTION,
                                      "Description of party.",
                                      1, 1),
                      new StringField(PUB_KEY,
                                      "Public signature key (instance of " +
                                      "crypto.SignaturePKey).",
                                      1, 1));
    }

    /**
     * Creates an instance with the given additional fields.
     *
     * @param infoFields Additional fields of this instance.
     */
    public PartyInfo(InfoField ... infoFields) {
        this();
        addInfoFields(infoFields);
    }

    /**
     * Creates an instance with the given additional fields.
     *
     * @param infoFields Additional fields of this instance.
     */
    public PartyInfo(ArrayList<InfoField> infoFields) {
        for (InfoField infoField : infoFields) {
            addInfoFields(infoField);
        }
    }

    /**
     * Appends the schema of this instance to the input.
     *
     * @param sb Destination of schema.
     */
    protected void generateSchema(StringBuffer sb) {
        String s =
            "<xs:element name=\"" + PARTY + "\"\n" +
            "            minOccurs=\"0\"\n" +
            "            maxOccurs=\"unbounded\">\n" +
            "<xs:complexType>\n" +
            "<xs:sequence>\n\n";
        sb.append(s);

        schemaOfInfoFields(sb);

        s = "</xs:sequence>\n" +
            "</xs:complexType>\n" +
            "</xs:element>\n\n";
        sb.append(s);
    }

    /**
     * Writes an XML representation of this instance to the input
     * <code>StringBuffer</code> using the given indent level.
     *
     * @param sb Destination of XML representation.
     * @param indent Indentation level.
     */
    protected void toXML(StringBuffer sb, int indent) {
        formatTag(sb, indent, PARTY, BEGIN, indent);
        xmlOfInfoFields(sb, indent + 1);
        sb.append("\n");
        formatTag(sb, indent, PARTY, END, indent);
    }

    // These methods are documented in the super class.

    public Info endElement(String content, String tagName) {
        if (tagName.equals(PARTY)) {
            return null;
        } else {
            return super.endElement(content, tagName);
        }
    }

    /**
     * Performs a comparison between this instance and the input.
     *
     * @param pi Instance with which this instance is compared.
     * @return -1, 0, or 1 depending on if this instance is less,
     * equal, or larger than the input respectively.
     */
    public int compareTo(PartyInfo pi) {
        int c = this.getStringValue(SORT_BY_ROLE).
            compareTo(pi.getStringValue(SORT_BY_ROLE));
        if (c != 0) {
            return c;
        } else {
            return this.getStringValue(PrivateInfo.NAME).
                compareTo(pi.getStringValue(PrivateInfo.NAME));
        }
    }
}
