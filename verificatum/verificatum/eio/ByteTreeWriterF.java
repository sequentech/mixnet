
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

package verificatum.eio;

import java.io.*;

/**
 * A writer of byte tree instances to file. The functionality of this
 * class does not match that of {@link ByteTreeReader}. This class
 * should be used inside in classes operating on files internally.
 *
 * @author Douglas Wikstrom
 */
public class ByteTreeWriterF {

    /**
     * Destination of instances.
     */
    protected DataOutputStream dos;

    /**
     * Creates an instance with the given number of children/bytes to
     * be written.
     *
     * @param remaining Number of children/bytes (supposedly)
     * remaining to be written.
     * @param file Destination of instances.
     */
    public ByteTreeWriterF(int remaining, File file)
        throws FileNotFoundException, IOException {
        this.dos = new DataOutputStream(new FileOutputStream(file));
        dos.writeByte(ByteTreeBasic.NODE);
        dos.writeInt(remaining);
    }

    /**
     * Writes a byte tree to the underlying file.
     *
     * @param bt Byte tree to be written.
     */
    public void unsafeWrite(ByteTreeBasic bt) {
        bt.unsafeWriteTo(dos);
    }

    /**
     * Writes a byte tree to the underlying file.
     *
     * @param bt Byte tree to be written.
     * @throws EIOException If writing fails.
     */
    public void write(ByteTreeBasic bt) throws EIOException {
        bt.writeTo(dos);
    }

    /**
     * Writes byte-tree convertible object to the underlying file.
     *
     * @param btc Byte tree convertible objects to be written.
     * @throws EIOException If writing fails.
     */
    public void write(ByteTreeConvertible ... btc) throws EIOException {
        for (int i = 0; i < btc.length; i++) {
            write(btc[i].toByteTree());
        }
    }

    /**
     * Writes byte-tree convertible object to the underlying file.
     *
     * @param btc Byte tree convertible objects to be written.
     */
    public void unsafeWrite(ByteTreeConvertible ... btc) {
        try {
            write(btc);
        } catch (EIOException eioe) {
            throw new EIOError("Unable to write!");
        }
    }

    /**
     * Closes the underlying file.
     */
    public void close() {
        ExtIO.strictClose(dos);
    }
}
