
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

import java.io.*;
import java.util.*;

import org.xml.sax.*;

import verificatum.protocol.*;
import verificatum.ui.*;
import verificatum.util.*;

/**
 * Abstract base class for representing configuration data for
 * protocols and parties in a protocol.
 *
 * @author Douglas Wikstrom
 */
public abstract class Info {

    /**
     * Used to pretty print an instance as XML code.
     */
    protected final static String INDENT_STRING = "   ";

    /**
     * Used to start XML tags.
     */
    protected final static String BEGIN = "";

    /**
     * Used to end XML tags.
     */
    protected final static String END = "/";

    /**
     * Abbreviation for "end of file".
     */
    protected final static String EOF = "end of file";

    /**
     * Stores the <code>InfoField</code> instances associated with
     * this instance in the order they are added.
     */
    protected ArrayList<InfoField> infoFields;

    /**
     * Stores the <code>InfoField</code> instances associated with
     * this instance.
     */
    protected HashMap<String, InfoField> infoFieldsHashMap;

    /**
     * Stores the values stored in the <code>InfoField</code>
     * instances of this instance.
     */
    protected HashMap<String, ArrayList<Object>> values;

    /**
     * Creates an empty instance with no <code>InfoField</code>s and no
     * values.
     */
    protected Info() {
        infoFields = new ArrayList<InfoField>();
        infoFieldsHashMap = new HashMap<String, InfoField>();
        values = new HashMap<String, ArrayList<Object>>();
    }

    /**
     * Adds an <code>InfoField</code> to this instance.
     *
     * @param infoField <code>InfoField</code> instance added to this
     * <code>Info</code>.
     */
    public void addInfoField(InfoField infoField) {
        infoFields.add(infoField);
        infoFieldsHashMap.put(infoField.getName(), infoField);
        values.put(infoField.getName(), new ArrayList<Object>());
    }

    /**
     * Adds several <code>InfoField</code> to this instance.
     *
     * @param infoFields <code>InfoField</code> instances added to
     * this instance.
     */
    public void addInfoFields(InfoField ... infoFields) {
        for (InfoField infoField : infoFields) {
            addInfoField(infoField);
        }
    }

    /**
     * Writes the schema entries of the <code>InfoField</code>
     * instances of this instance.
     *
     * @param sb Where the schema entries are written.
     */
    public void schemaOfInfoFields(StringBuffer sb) {
        for (InfoField infoField : infoFields) {
            sb.append(infoField.schemaElementString()).append("\n\n");
        }
    }

    /**
     * Called by {@link InfoParser} when an element is found. This
     * should normally be overridden by subclasses.
     *
     * @param tagName Name of element.
     * @return Returns this instance.
     */
    protected Info startElement(String tagName) {
        return this;
    }

    /**
     * Adds a value under a given tag.
     *
     * @param tagName Name of element.
     * @param content String representing value.
     */
    public void addValue(String tagName, Object content) {
        InfoField infoField = infoFieldsHashMap.get(tagName);
        values.get(tagName).add(content);
    }

    /**
     * Adds a value under a given tag.
     *
     * @param tagName Name of element.
     * @param content String representing value.
     */
    public void addValue(String tagName, String content) {
        InfoField infoField = infoFieldsHashMap.get(tagName);
        values.get(tagName).add(infoField.parse(content));
    }

    /**
     * Adds a value under a given tag.
     *
     * @param tagName Name of element.
     * @param intValue Integer value.
     */
    public void addValue(String tagName, int intValue) {
        values.get(tagName).add(new Integer(intValue));
    }

    /**
     * Copy values under a given tag name from another info
     * instance. If an object to be copied is an instance of {@link
     * Lazy}, then the output of {@link Lazy#gen()} is used
     * instead. This allows lazy evaluation of default parameters that
     * are costly to generate.
     *
     * @param tagName Name of tag.
     * @param info Source of values.
     */
    public void copyValues(String tagName, Info info) {
        ArrayList<Object> al = values.get(tagName);
        for (Object obj : info.values.get(tagName)) {
            if (obj instanceof Lazy) {
                al.add(((Lazy)obj).gen());
            } else {
                al.add(obj);
            }
        }
    }

    /**
     * Writes the XML code of the fields stored in this instance.
     *
     * @param sb Where the XML code is written.
     * @param indent Indent depth of this invokation.
     */
    protected void xmlOfInfoFields(StringBuffer sb, int indent) {
        for (InfoField infoField : infoFields) {

            String description = infoField.getDescription();
            if (!description.equals("")) {
                sb.append("\n");
                formatComment(sb, indent, description, 0);
            }
            sb.append("\n");

            for (Object obj : values.get(infoField.getName())) {
                formatContent(sb, indent, infoField.getName(),
                              obj.toString(), 1);
            }
        }
    }

    /**
     * Called by {@link InfoParser} when an element is ended. This
     * should normally be overrridden by subclasses.
     *
     * @param tagName Name of element.
     * @param content String representing value.
     * @return Returns this instance.
     */
    public Info endElement(String content, String tagName) {
        addValue(tagName, content);
        return this;
    }

    /**
     * Returns an iterator of all values stored under a given tag.
     *
     * @param tagName Name of tag.
     * @return Iterator for values stored under the tag.
     */
    public ListIterator<Object> getValues(String tagName) {
        return values.get(tagName).listIterator();
    }

    /**
     * Checks if any value has been stored under the given tag name.
     *
     * @param tagName Name of tag.
     * @return <code>true</code> if this instance stores a value under
     * the given tag name and <code>false</code> otherwise.
     */
    public boolean hasValue(String tagName) {
        ArrayList<Object> someValues = values.get(tagName);
        return someValues != null && someValues.size() > 0;
    }

    /**
     * Returns the first value stored under a given tag name.
     *
     * @param tagName Name of tag.
     * @return First value stored under the given tag name.
     */
    public Object getValue(String tagName) {
        return values.get(tagName).get(0);
    }

    /**
     * Returns the first value stored under a given tag name.
     *
     * @param tagName Name of tag.
     * @return First value stored under the given tag name.
     */
    public int getIntValue(String tagName) {
        return ((Integer)getValue(tagName)).intValue();
    }

    /**
     * Returns the first value stored under a given tag name.
     *
     * @param tagName Name of tag.
     * @return First value stored under the given tag name.
     */
    public String getStringValue(String tagName) {
        return (String)getValue(tagName);
    }

    /**
     * Returns a string with the number of indent steps given as
     * input. This is used for formatting output.
     *
     * @param indent Number of indent steps.
     * @return Indent string.
     */
    protected String indentString(int indent) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indent; i++) {
            sb.append(INDENT_STRING);
        }
        return sb.toString();
    }

    /**
     * Returns a string with the number of new lines given as
     * input. This is used for formatting output.
     *
     * @param nl Number of new lines.
     * @return String containing new lines.
     */
    protected String newLines(int nl) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nl; i++) {
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Formats a schema tag.
     *
     * @param sb Where the formatted tag is written.
     * @param indent How much the formatted tag should be indented.
     * @param tag Name of tag.
     * @param tagType Schema type of tag.
     * @param nl Number of new lines after the tag.
     */
    protected void formatTag(StringBuffer sb, int indent,
                             String tag, String tagType, int nl) {
        sb.append(indentString(indent)
                  + "<" + tagType + tag + ">"
                  + newLines(nl));
    }

    /**
     * Formats an element.
     *
     * @param sb Where the formatted tag is written.
     * @param indent How much the formatted tag should be indented.
     * @param tag Name of tag.
     * @param content Encoded value.
     * @param nl Number of new lines after the element.
     */
    protected void formatContent(StringBuffer sb, int indent,
                                 String tag, String content, int nl) {
        formatTag(sb, indent, tag, BEGIN, 0);
        sb.append(content);
        formatTag(sb, 0, tag, END, nl);
    }

    /**
     * Formats a comment.
     *
     * @param sb Where the formatted tag is written.
     * @param indent How much the formatted tag should be indented.
     * @param description Comment string.
     * @param nl Number of new lines at end of comment.
     */
    protected void formatComment(StringBuffer sb, int indent,
                                 String description, int nl) {
        String is = indentString(indent);
        String last = "";

        sb.append(is + "<!-- ");

        String brokenDescription =
            Util.breakLine(description + " -->", 70 - (indent + 5));

        sb.append(brokenDescription.replaceAll("\n", "\n" + is + "     "));
        sb.append(newLines(nl));
    }


    /**
     * Verifies that the input contains identical info fields and that
     * it has identical info values to the ones in this instance.
     *
     * @param info Instance compared with this instance.
     * @return <code>true</code> or <code>false</code> depending on if
     * this instance is equal to the input or not.
     */
    public boolean equalInfoFields(Info info) {
        if (infoFields.size() != info.infoFields.size()) {
            return false;
        }
        for (InfoField infoField : infoFields) {
            if (!values.get(infoField.name).
                equals(info.values.get(infoField.name))) {
                return false;
            }
        }
        return true;
    }
}

