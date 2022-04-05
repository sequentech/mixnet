
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
 * Represents an execution of a given protocol with a given set of
 * parties. It stores global fields such as session id, name, and
 * description. It also stores several <code>PartyInfo</code>
 * instances corresponding to the parties executing the protocol.
 *
 * @author Douglas Wikstrom
 */
public class ProtocolInfo extends RootInfo {

    /**
     * Name of protocol tag.
     */
    public final static String PROTOCOL = "protocol";

    /**
     * Name of session id tag.
     */
    public final static String SESSIONID = "sid";

    /**
     * Name of name tag.
     */
    public final static String NAME = "name";

    /**
     * Name of description tag.
     */
    public final static String DESCRIPTION = "descr";

    /**
     * Name of number of parties tag.
     */
    public final static String NOPARTIES = "nopart";

    /**
     * Name of threshold tag.
     */
    public final static String THRESHOLD = "thres";

    /**
     * List of party infos.
     */
    protected ArrayList<PartyInfo> partyInfos;

    /**
     * Factory for creating new info fields.
     */
    protected PartyInfoFactory factory;

    /**
     * Create instance with the default factory for creating parties
     * and with the given additional info fields. This is useful for
     * subclasses.
     *
     * @param infoFields Additional info fields.
     */
    public ProtocolInfo(InfoField ... infoFields) {
        this(new PartyInfoFactory(), infoFields);
    }

    /**
     * Create instance with a given factory for creating parties and
     * with the given additional info fields.
     *
     * @param factory Factory for creating parties.
     * @param infoFields Additional info fields.
     */
    public ProtocolInfo(PartyInfoFactory factory, InfoField ... infoFields) {

        String doc = "Protocol version for which this protocol info is " +
            "intended.";

        addInfoFields(new StringField(VERSION, doc, 1, 1),
                      new StringField(SESSIONID,
                                      "Session identifier of this protocol " +
                                      "execution. This must be globally " +
                                      "unique for all executions of all " +
                                      "protocols.",
                                      1, 1),
                      new StringField(NAME,
                                      "Name of this protocol execution. This " +
                                      "is a short descriptive name.",
                                      1, 1),
                      new StringField(DESCRIPTION,
                                      "Description of this protocol " +
                                      "execution. This is a longer " +
                                      "description than the name.",
                                      1, 1),
                      new IntField(NOPARTIES,
                                   "Number of parties.",
                                   1, 1),
                      new IntField(THRESHOLD,
                                   "Number of parties needed to violate " +
                                   "privacy. This must at least be majority.",
                                   1, 1));
        addInfoFields(infoFields);

        this.partyInfos = new ArrayList<PartyInfo>();
        this.factory = factory;
    }

    /**
     * Parses values from the file with the given filename and returns
     * itself, i.e., it fills itself with data from the file.
     *
     * @param infoFilename Name of info file.
     * @return This instance.
     * @throws InfoException If parsing fails.
     */
    public ProtocolInfo parse(String infoFilename) throws InfoException {
        return (ProtocolInfo)super.parse(infoFilename);
    }

    /**
     * Copies the party infos to this instance from the given protocol
     * info. Note that this may change the indices of existing party
     * infos.
     *
     * @param pi Source of party infos.
     * @throws InfoException If several party infos to be added have
     * the same name.
     */
    public void addPartyInfos(ProtocolInfo pi) throws InfoException {
        for (PartyInfo partyInfo : pi.partyInfos) {
            String name = partyInfo.getStringValue(NAME);
            PartyInfo pai = getPartyInfo(name);
            if (pai == null) {
                partyInfos.add(partyInfo);
            } else if (!pai.equalInfoFields(partyInfo)) {
                String s = "Different PartyInfo instances with the same" +
                    " name (" + name + ")";
                throw new InfoException(s);
            }
        }
        Collections.sort(partyInfos);
    }

    /**
     * Adds a <code>PartyInfo</code> instance to this instance.
     *
     * @param partyInfo Instance to add.
     */
    public void addPartyInfo(PartyInfo partyInfo) {
        this.partyInfos.add(partyInfo);
    }

    /**
     * Returns the party info with the given name.
     *
     * @param name Name of party info.
     * @return The party info with the given name.
     */
    public PartyInfo getPartyInfo(String name) {
        for (PartyInfo pi : partyInfos) {
            if (pi.getStringValue(NAME).equals(name)) {
                return pi;
            }
        }
        return null;
    }

    /**
     * Returns the index of the <code>PartyInfo</code> with the given
     * name. WARNING! Do not use this method until all
     * <code>PartyInfo</code> instances have been added. Doing so may
     * give incorrect results.
     *
     * @param name Name of a party.
     * @return Index of a party.
     * @throws InfoException If the name is unknown.
     */
    public int getIndex(String name) throws InfoException {
        int index = 1;

        for (ListIterator<PartyInfo> li = partyInfos.listIterator();
             li.hasNext();) {
            if (li.next().getValue(NAME).equals(name)) {
                return index;
            } else {
                index++;
            }
        }
        throw new InfoException("Can not find entry in protocol info "
                                + "file for the name \"" + name + "\"!");
    }

    /**
     * Returns the number of <code>PartyInfo</code> instances in this
     * instance.
     *
     * @return Number of parties stored in this instance.
     */
    public int getNumberOfParties() throws InfoException {
        int noparties = getIntValue(NOPARTIES);
        if (partyInfos.size() != noparties) {
            throw new InfoException("Mismatching number of parties! (" +
                                    partyInfos.size() +
                                    " parties, but expected " + noparties +
                                    ")");
        } else {
            return noparties;
        }
    }

    /**
     * Returns the <code>PartyInfo</code> with the given index.
     *
     * @param i Index of requested party.
     * @return The <code>i</code>th <code>PartyInfo</code> contained
     * in this instance.
     */
    public PartyInfo get(int i) {
        return partyInfos.get(i - 1);
    }

    /**
     * Returns the factory of this instance.
     *
     * @return Factory used in this instance.
     */
    public PartyInfoFactory getFactory() {
        return factory;
    }

    // These methods are documented in the super class.

    public Info startElement(String tagName) {
        if (tagName.equals(PartyInfo.PARTY)) {
            PartyInfo pi = factory.newInstance();
            partyInfos.add(pi);
            return pi;
        } else {
            return super.startElement(tagName);
        }
    }

    public Info endElement(String content, String tagName) {
        if (tagName.equals(PROTOCOL)) {
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
        s = "<xs:element name=\"" + PROTOCOL + "\">\n" +
            "<xs:complexType>\n" +
            "<xs:sequence>";
        sb.append(s).append("\n\n");

        // Generate all our fields
        schemaOfInfoFields(sb);
        sb.append("\n");

        // Generate the party element
        (factory.newInstance()).generateSchema(sb);

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
"ATTENTION! WE STRONGLY ADVICE AGAINST EDITING THIS FILE!" +
"\n\n" +
"This is a protocol information file. It contains all the " +
"parameters of a protocol session as agreed by all parties." +
"\n\n" +
"Each party must hold an identical copy of this file. DO NOT UNDER " +
"ANY CIRCUMSTANCES EDIT THIS FILE after you and the administrators " +
"of the other parties have agreed on its content." +
"If you do, then there are no security guarantees." +
"\n\n" +
"If the Fiat-Shamir heuristic is used, then DO NOT EDIT THIS FILE UNDER " +
"ANY CIRCUMSTANCES! " +
"Any Fiat-Shamir proof of correctness computed before this file was changed " +
"will be rejected after the change. This can not be corrected without access " +
"to the original version of this file!";

        sb.append("\n");
        formatComment(sb, 0, genDescription, 2);

        formatTag(sb, 0, PROTOCOL, BEGIN, 1);
        xmlOfInfoFields(sb, 1);
        sb.append("\n");
        for (ListIterator<PartyInfo> li = partyInfos.listIterator();
             li.hasNext();) {
            li.next().toXML(sb, 1);
            sb.append("\n");
        }
        formatTag(sb, 0, PROTOCOL, END, 2);
        return sb.toString();
    }
}
