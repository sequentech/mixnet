
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

package mixnet.ui.tui;

import java.io.*;

import mixnet.ui.*;

/**
 * Simple textual console storing the standard input, output, and
 * error streams.
 *
 * @author Douglas Wikstrom
 */
public class TConsole extends mixnet.ui.Console {

    /**
     * Creates a standard console.
     */
    public TConsole() {
        in = System.in;
        out = System.out;
        err = System.err;
    }
}
