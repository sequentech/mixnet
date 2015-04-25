
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

/**
 * This is a minimal and incomplete implementation of the <a
 * href="http://www.json.org">JSON</a> format. It can only handle maps
 * of strings, but this suffices for our needs.
 *
 * @author Douglas Wikstrom
 */
public class SimpleJSON {

    /**
     * Converts a table of strings into its JSON representation.
     *
     * @param map JSON representation of input.
     * @return String representation of the input map.
     */
    public static String toJSON(TreeMap<String, String> map) {

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        Set<String> keys = map.keySet();
        Iterator<String> iterator = keys.iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = map.get(key);

            sb.append("\"");
            sb.append(key);
            sb.append("\":\"");
            sb.append(value);
            sb.append("\"");
            sb.append(",");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");

        return sb.toString();
    }

    /**
     * Parses a quoted string.
     *
     * @param head Source of tokens.
     * @return String represented by the input.
     * @throws SimpleJSONException If the content at the current
     * position is not a string.
     */
    protected static String readString(Head head)
        throws SimpleJSONException {

        if (!head.equals('"')) {
            throw new SimpleJSONException("Expected a string!");
        }
        head.inc();

        StringBuilder sb = new StringBuilder();
        while (!head.end() && head.isAlphaNum()) {
            sb.append(head.getc());
            head.inc();
        }

        if (head.end() || !head.equals('"')) {
            throw new SimpleJSONException("Missing ending quote!");
        }
        head.inc();

        return sb.toString();
    }

    /**
     * Parses a key-and-value pair and stores the result in the given
     * map.
     *
     * @param head Source of tokens.
     * @param map Destination of key-and-value pair.
     * @throws SimpleJSONException If the content at the current
     * position is not a key-value pair.
     */
    protected static void readPair(Head head, TreeMap<String,String> map)
    throws SimpleJSONException {
        String key = readString(head);
        if (head.end() || !head.equals(':')) {
            throw new SimpleJSONException("Missing ':'!");
        }

        head.inc();

        String value = readString(head);
        map.put(key, value);
    }

    /**
     * Returns the map corresponding to the input representation.
     *
     * @param mapString JSON representation of a map.
     * @return Map corresponding to the input representation.
     * @throws SimpleJSONException If the content at the current
     * position is not a key-value pair map.
     */
    public static TreeMap<String,String> fromJSON(String mapString)
    throws SimpleJSONException {

        Head head = new Head(mapString);

        TreeMap<String,String> map = new TreeMap<String,String>();

        head.skipWhitespace();

        if (head.end() || !head.equals('{')) {
            throw new SimpleJSONException("Missing starting '{'!");
        }
        head.inc();
        head.skipWhitespace();

        readPair(head, map);

        head.skipWhitespace();

        while (head.equals(',')) {

            head.inc();
            head.skipWhitespace();
            readPair(head, map);

        }

        head.skipWhitespace();

        if (head.end() || !head.equals('}')) {
            throw new SimpleJSONException("Missing ending '}'!");
        }

        head.inc();
        head.skipWhitespace();

        if (!head.end()) {
            throw new SimpleJSONException("Junk at end of input!");
        }

        return map;
    }
}

/**
 * Simple tokenizer moving over a string.
 *
 * @author Douglas Wikstrom
 */
class Head {

    /**
     * Current position of head.
     */
    protected int i;

    /**
     * Data source.
     */
    protected String src;

    /**
     * Creates an instance that reads from the given source.
     *
     * @param src Source of characters.
     */
    public Head(String src) {
        this.src = src;
        this.i = 0;
    }

    /**
     * Returns the current character.
     *
     * @return Current character.
     */
    public char getc() throws SimpleJSONException {
        return src.charAt(i);
    }

    /**
     * Returns true if and only if the character is a letter or a
     * digit.
     *
     * @return True if and only if the character is a letter or a
     * digit.
     */
    public boolean isAlphaNum() {
        char c = src.charAt(i);
        return (48 <= c && c < 58)    // 0-9
            || (65 <= c && c < 91)    // A-Z
            || (97 <= c && c < 123);  // a-z
    }

    /**
     * Moves the head passed any whitespace.
     */
    public void skipWhitespace() {
        while (!end() && Character.isWhitespace(src.charAt(i))) {
            i++;
        }
    }

    /**
     * Moves the head one step forward.
     */
    public void inc() throws SimpleJSONException {
        if (end()) {
            throw new SimpleJSONException("Unexpected end of input!");
        }
        i++;
    }

    /**
     * Tests for equality with the current character.
     *
     * @param c Character to test for.
     * @return Result of equality test.
     */
    public boolean equals(char c) {
        return src.charAt(i) == c;
    }

    /**
     * Returns true if and only if the head is passed the end of the
     * input.
     *
     * @return true if and only if the head is passed the end of the
     * input.
     */
    public boolean end() {
        return i == src.length();
    }
}

