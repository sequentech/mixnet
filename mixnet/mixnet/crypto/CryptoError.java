
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

package mixnet.crypto;

/**
 * Thrown when a fatal error occurs. This should never be caught.
 *
 * @author Douglas Wikstrom
 */
public class CryptoError extends Error {

    /**
     * Constructs a new error with the specified detail message.
     *
     * @param message Detailed message of the problem.
     */
    public CryptoError(String message) {
        super(message);
    }

    /**
     * Constructs a new error with the specified detail message
     * and cause.
     *
     * @param message Detailed message of the problem.
     * @param cause What caused this error to be thrown.
     */
    public CryptoError(String message, Throwable cause) {
        super(message, cause);
    }
}
