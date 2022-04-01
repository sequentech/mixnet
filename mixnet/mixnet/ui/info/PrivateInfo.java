
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

package mixnet.ui.info;

import java.io.*;
import java.net.*;
import java.util.*;

import org.xml.sax.*;

import mixnet.protocol.*;

/**
 * Represents the private info of a party in a protocol. It stores the
 * name of the party, its own directory, its temporary directory, and
 * some source of randomness. Additional fields, e.g., secret keys,
 * can be added.
 *
 * @author Douglas Wikstrom
 */
public class PrivateInfo extends RootInfo {

    /**
     * Name of private tag.
     */
    public final static String PRIVATE = "private";

    /**
     * Name of name tag.
     */
    public final static String NAME = "name";

    /**
     * Description of name field.
     */
    public final static String NAME_DESCRIPTION = "Name of party.";

    /**
     * Name of directory tag.
     */
    public final static String DIR = "dir";

    /**
     * Name of temporary directory tag tag.
     */
    public final static String TEMP_DIR = "tempdir";

    /**
     * Name of randomness tag.
     */
    public final static String RANDOMNESS = "rand";

    /**
     * Name of certainty tag.
     */
    public final static String CERTAINTY = "cert";

    /**
     * Name of private key tag.
     */
    public final static String PRIV_KEY = "skey";

    /**
     * Creates an instance with default info fields. A meaningless
     * parameter is used to identify this constructor.
     *
     * @param s Parameter that should always be <code>null</code>.
     */
    public PrivateInfo(String s) {

        String doc = "Protocol version for which this protocol info is " +
            "intended.";

        addInfoFields(new StringField(VERSION, doc, 1, 1),
                      new StringField(NAME, NAME_DESCRIPTION, 1, 1),
                      new StringField(DIR,
                                   "Working directory of protocol execution.",
                                   1, 1));
    }

    /**
     * Creates an instance with the given additional info fields.
     *
     * @param infoFields Additional info fields.
     */
    public PrivateInfo(InfoField ... infoFields) {
        this("");
        addInfoFields(new StringField(PRIV_KEY,
                                      "Pair of public and private signature " +
                                      "keys (instance of " +
                                      "mixnet.crypto.SignatureKeyPair).",
                                      1, 1),
                      new StringField(RANDOMNESS,
                                      "Source of randomness (instance of " +
                                      "mixnet.crypto.RandomSource).",
                                      1, 1),
                      new IntField(CERTAINTY,
                                   "Certainty with which probabilistically " +
                                   "checked parameters are verified, i.e., " +
                                   "the probability of an error is bounded " +
                                   "by 2^(-certainty).",
                                   1, 1)
                      );
        addInfoFields(infoFields);
    }

    // These methods are documented in the super class.

    public PrivateInfo parse(String infoFilename) throws InfoException {
        PrivateInfo pi = (PrivateInfo)super.parse(infoFilename);
        return pi;
    }

    public Info endElement(String content, String tagName) {
        if (tagName.equals(PRIVATE)) {
            return null;
        } else {
            return super.endElement(content, tagName);
        }
    }

    public String generateSchema() {
        StringBuffer sb = new StringBuffer();

        // Generate the beginning of the schema
        String s =
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
            "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">";
        sb.append(s).append("\n");

        // Generate protocol element
        s = "<xs:element name=\"" + PRIVATE + "\">\n" +
            "<xs:complexType>\n" +
            "<xs:sequence>";
        sb.append(s).append("\n\n");

        // Generate all our fields
        schemaOfInfoFields(sb);

        // Generate the end of the schema
        s = "</xs:sequence>\n" +
            "</xs:complexType>\n" +
            "</xs:element>\n\n" +
            "</xs:schema>\n";
        sb.append(s);

        return sb.toString();
    }

    public String toXML() {
        StringBuffer sb = new StringBuffer();

        String genDescription =
"ATTENTION! This is a private information file. It contains all your " +
"internal parameters of a protocol execution, including your secret signing " +
"key." +
"\n\n" +
"WARNING! The contents of this file MUST REMAIN SECRET. Failure to do so may " +
"result in a catastrophic security breach." +
"\n\n" +
"DO NOT edit this file during the execution of the protocol. If you " +
"must edit it, then please be VERY CAREFUL and MAKE SURE THAT YOU " +
"UNDERSTAND THE IMPLICATIONS OF YOUR EDITS.";

        sb.append("\n");
        formatComment(sb, 0, genDescription, 2);

        formatTag(sb, 0, PRIVATE, BEGIN, 1);
        xmlOfInfoFields(sb, 1);
        sb.append("\n");
        formatTag(sb, 0, PRIVATE, END, 2);
        return sb.toString();
    }
}
