
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

package verificatum.arithm;

import java.io.*;

import verificatum.eio.*;

/**
 * Interface for an iterator over a {@link LargeIntegerArray}.
 *
 * @author Douglas Wikstrom
 */
public class LargeIntegerIteratorF implements LargeIntegerIterator {

    /**
     * Underlying source of elements.
     */
    protected ByteTreeReader btr;

    /**
     * Creates an iterator reading from the given array.
     *
     * @param file Underlying file
     */
    public LargeIntegerIteratorF(File file) {
	this.btr = (new ByteTreeF(file)).getByteTreeReader();
    }

    // Documented in LargeIntegerIterator.java

    public LargeInteger next() {
        if (btr.getRemaining() > 0) {
            try {
                return new LargeInteger(btr.getNextChild());
            } catch (EIOException eioe) {
                throw new ArithmError("Unable to read integer!", eioe);
            } catch (ArithmFormatException afe) {
                throw new ArithmError("Unable to read integer!", afe);
            }
        } else {
            return null;
        }
    }

    public boolean hasNext() {
        return btr.getRemaining() > 0;
    }

    public void close() {
        btr.close();
    }
}

