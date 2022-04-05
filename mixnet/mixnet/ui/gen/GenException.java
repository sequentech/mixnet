
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

/**
 * Thrown when an input is malformed. By convention this exception
 * should contain an error string suitable for the user as a an error
 * message.
 *
 * @author Douglas Wikstrom
 */
public class GenException extends Exception {

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message Detailed message of the problem.
     */
    public GenException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message
     * and cause.
     *
     * @param message Detailed message of the problem.
     * @param cause What caused this exception to be thrown.
     */
    public GenException(String message, Throwable cause) {
        super(message, cause);
    }
}
