
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
import java.net.*;
import java.util.*;

import javax.xml.transform.stream.*;
import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.validation.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import verificatum.protocol.*;

/**
 * Parser of XML files. This is a simple wrapper of the SAX parser of
 * the Java class library. It takes an <code>Info</code> instance and
 * a file as input, reads the schema the instance represents, and then
 * parses the XML code from the file according to the schema. Any
 * values encountered are stored in the <code>Info</code> instance.
 *
 * @author Douglas Wikstrom
 */
public class InfoParser extends DefaultHandler {

    /**
     * To allow nested <code>Info</code> instances, we store instances
     * in a stack in the obvious way.
     */
    protected Stack<Info> stack;

    /**
     * Locator used to keep track of where we are in the XML
     * code. This is only used to give proper error messages.
     */
    protected Locator locator;

    /**
     * Keeps the content so far between the starting and ending tags
     * during parsing.
     */
    protected StringBuffer currentContent = new StringBuffer();

    /**
     * Creates an empty instance.
     */
    public InfoParser() {
        stack = new Stack<Info>();
    }

    /**
     * Parses the XML code in the given file according to the schema
     * of the <code>RootInfo</code> instance and fills this instance
     * with the content parsed from file.
     *
     * @param pi <code>RootInfo</code> containing schema and
     * destination of values parsed from file.
     * @param file XML code.
     * @throws InfoException If the file is incorrectly formatted.
     */
    public void parse(RootInfo pi, File file)
    throws InfoException {

        try {

            // Create schema factory.
            SchemaFactory schemaFactory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Create schema
            String schemaString = pi.generateSchema();
            StreamSource ss = new StreamSource(new StringReader(schemaString));
            Schema schema = schemaFactory.newSchema(ss);

            // Use a validating parser following the schema read above.
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setSchema(schema);

            // Create parser.
            SAXParser saxParser = factory.newSAXParser();

            // Do the parsing
            try {
                stack.push(pi);
                saxParser.parse(file, this);
            } catch (SAXParseException spe) {
                    throw new InfoException("Error on line: "
                                        + spe.getLineNumber()
                                        + " column: "
                                        + spe.getColumnNumber() + ": "
                                        + spe.getMessage());
            }
        } catch (SAXException saxe) {
            throw new InfoException("Unable to create or use parser!", saxe);
        } catch (ParserConfigurationException pce) {
            throw new InfoException("Unable to configure parser!", pce);
        } catch (IOException ioe) {
            throw new InfoException("Unable to read files!", ioe);
        }
    }

    // These methods are documented in the super class.

    public void startElement(String namespaceURI,
                             String sName, // simple name (localName)
                             String qName, // qualified name
                             Attributes attrs) throws SAXException {

        Info info = stack.peek().startElement(qName);

        // Push the info onto the stack if it is not null and not the
        // top-most info on the stack.
        if (info != null && info != stack.peek()) {
            stack.push(info);
        }
        currentContent = new StringBuffer();
    }

    public void endElement(String namespaceURI,
                           String sName,
                           String qName) {
        Info info =
            stack.peek().endElement(currentContent.toString(), qName);
        if (info == null) {
            stack.pop();
        }
    }

    public void characters(char buf[], int offset, int len)
        throws SAXException {
        currentContent.append(buf, offset, len);
    }

    public void warning(SAXParseException e)
    throws SAXParseException {
        throw e;
    }

    public void error(SAXParseException e)
    throws SAXParseException {
        throw e;
    }

    public void fatalError(SAXParseException e)
    throws SAXParseException {
        throw e;
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }
}
