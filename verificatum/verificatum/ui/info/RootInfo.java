
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

import verificatum.protocol.*;

/**
 * Abstract base class for a root <code>Info</code> instance.
 *
 * @author Douglas Wikstrom
 */
public abstract class RootInfo extends Info {

    /**
     * Name of version tag. A field with this tag is not added by
     * default.
     */
    public final static String VERSION = "version";

    /**
     * Parses the file with the given filename according to its own
     * schema and stores the values it encounters.
     *
     * @param infoFilename Name of XML file.
     * @return Instance containing values parsed from file.
     * @throws InfoException If parsing fails.
     */
    public RootInfo parse(String infoFilename) throws InfoException {
        (new InfoParser()).parse(this, new File(infoFilename));
        return this;
    }

    /**
     * Generates its own schema.
     *
     * @return Schema of this instance.
     */
    public abstract String generateSchema();

    /**
     * Outputs an XML representation of the values in this instance.
     *
     * @return XML code representing this instance.
     */
    protected abstract String toXML();

    /**
     * Writes an XML representation of the values in this instance to
     * the given file.
     *
     * @param infoFile Where the XML is written.
     * @throws InfoException If generating the info file fails.
     */
    public void toXML(File infoFile) throws InfoException {
         try {
             infoFile.delete();
             FileWriter fw = new FileWriter(infoFile);
             fw.write(toXML());
             fw.flush();
         } catch (IOException ioe) {
             throw new InfoException("Can not generate info file!", ioe);
         }
    }
}
