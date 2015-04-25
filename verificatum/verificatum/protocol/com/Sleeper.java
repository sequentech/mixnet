
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

package verificatum.protocol.com;

import verificatum.eio.*;
import verificatum.ui.*;
import verificatum.protocol.*;

/**
 * A simple sleeping thread that sleeps at for a given amount of time
 * unless interrupted prematurely.
 *
 * @author Douglas Wikstrom
 */
public class Sleeper extends Thread {

    /**
     * Maximum time this instance sleeps when executed as a thread.
     */
    int waitTime;

    /**
     * Creates an instance that waits for at most the given amount of
     * time when started as a thread.
     *
     * @param waitTime Maximum time this instance sleeps when executed
     * as a thread.
     */
    Sleeper(int waitTime) {
        this.waitTime = waitTime;
    }

    // Documented in Thread.java

    public void run() {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ie) {}
    }
}
