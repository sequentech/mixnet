
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

/**
 * Thrown when there is a format error. This should normally be caught
 * by the calling party and dealt with appropriately.
 *
 * @author Douglas Wikstrom
 */
public class SimpleJSONException extends Exception {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message Detailed message of the problem.
     */
    public SimpleJSONException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message
     * and cause.
     *
     * @param message Detailed message of the problem.
     * @param cause What caused this exception to be thrown.
     */
    public SimpleJSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
