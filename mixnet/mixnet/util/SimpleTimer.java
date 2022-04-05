
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

package mixnet.util;

/**
 * Implements a simple timer based on {@link System#currentTimeMillis()}.
 *
 * @author Douglas Wikstrom
 */
public class SimpleTimer {

    /**
     * Time at which this instance was created.
     */
    long startTime;

    /**
     * Create timer that automatically starts.
     */
    public SimpleTimer() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Outputs a string that describes the time elapsed since this
     * timer was created.
     *
     * @return String describing the time elapsed since this instance
     * was created.
     */
    public String toString() {
        long elapsed = System.currentTimeMillis() - startTime;
        long secs = elapsed / 1000;
        long minutes = secs / 60;
        long remainingSecs = secs % 60;
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        return "" + hours + "h " + remainingMinutes + "m " +
            remainingSecs + "s";
    }
}
