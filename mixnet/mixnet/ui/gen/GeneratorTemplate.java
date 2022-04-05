
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

package mixnet.ui.gen;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.naming.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.ui.*;
import mixnet.ui.opt.*;

// NATIVE
/**
 * Template class used to store instructions for how to generate an
 * object. WARNING! This is mainly for debugging and demonstrations,
 * and may not be platform independent.
 *
 * @author Douglas Wikstrom
 */
public class GeneratorTemplate implements Marshalizable {

    /**
     * Size of buffer used to read outputs of commands.
     */
    final static int OUTPUT_BUFFER_SIZE = 1024;

    /**
     * Signifies that this instance holds a shell command.
     */
    public final static int CMD = 0;

    /**
     * Signifies that this instance contains data that should simply
     * be copied when this instance is executed.
     */
    public final static int CPY = 1;

    /**
     * Type of instance, i.e., a shell command or a copy.
     */
    protected int type;

    /**
     * Encapsulated shell command or data to be copied.
     */
    protected String data;

    /**
     * Creates an instance.
     *
     * @param type Type of template.
     * @param data Data to be encapsulated.
     */
    public GeneratorTemplate(int type, String data) {
        this.data = data;
        this.type = type;
    }

    /**
     * Execute the given generator template.
     *
     * @param humanHex Description of generator template to be executed.
     * @return Resulting instance.
     *
     * @throws GenException If the template can not be executed.
     */
    public static String execute(String humanHex) throws GenException {
        try {
            GeneratorTemplate gt =
                Marshalizer.unmarshalHex_GeneratorTemplate(humanHex);
            return gt.execute();
        } catch (EIOException eioe) {
            throw new GenException("Unable to execute template!", eioe);
        }
    }

    /**
     * Execute this generator template.
     *
     * @return Resulting instance.
     *
     * @throws GenException If this template can not be executed.
     */
    public String execute() throws GenException {

        if (type == CMD) {
            Runtime runtime = Runtime.getRuntime();
            Process proc = null;
            InputStream procin = null;
            OutputStream procout = null;
            InputStream procerr = null;
            try {

                proc = runtime.exec(data);
                int ch;
                StringBuffer sb = new StringBuffer(OUTPUT_BUFFER_SIZE);

                procin = proc.getInputStream();
                procout = proc.getOutputStream();
                procerr = proc.getErrorStream();
                while ((ch = procin.read()) != -1) {
                    sb.append((char) ch);
                }
                return sb.toString();

            } catch (IOException ioe) {
                throw new GenException("Unable to execute template!", ioe);
            } finally {
                ExtIO.strictClose(procin);
                ExtIO.strictClose(procout);
                ExtIO.strictClose(procerr);
            }
        } else {
            return data;
        }
    }

    /**
     * Returns the generator template corresponding to the input.
     *
     * @param btr Representation of a generator template.
     * @return Generator template.
     *
     * @throws EIOException If the input does not represent an
     * instance.
     */
    public static GeneratorTemplate newInstance(ByteTreeReader btr)
    throws EIOException {
        int type = btr.getNextChild().readInt();
        String data = btr.getNextChild().readString();
        return new GeneratorTemplate(type, data);
    }

    // Documented in Marshalizable.java

    public ByteTree toByteTree() {
        return new ByteTree(ByteTree.intToByteTree(type),
                            ByteTree.stringToByteTree(data));
    }

    public String humanDescription(boolean verbose) {
        return Util.className(this, verbose) + "(" + data + ")";
    }
}