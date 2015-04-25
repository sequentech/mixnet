
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
 * Simplistic timer that allows setting a point in time in the future
 * and then later querying if this future time has become now.
 *
 * @author Douglas Wikstrom
 */
public class Timer {

    /**
     * Time from which this timer reports that the time is up.
     */
    protected int endTime;

    /**
     * Creates an instance.
     *
     * @param timeToEnd Total time to wait.
     */
    public Timer(int timeToEnd) {
        endTime = (int)System.currentTimeMillis() + timeToEnd;
    }

    /**
     * Returns true when the time is up.
     *
     * @return <code>true</code> if the time is up and
     * <code>false</code> otherwise.
     */
    public boolean timeIsUp() {
        return remainingTime() == 0;
    }

    /**
     * Returns the remaining number of milliseconds until the time is
     * up, or zero if the time is already up.
     *
     * @return Remaining time until the time is up.
     */
    public int remainingTime() {
        return Math.max(0, endTime - (int)System.currentTimeMillis());
    }
}