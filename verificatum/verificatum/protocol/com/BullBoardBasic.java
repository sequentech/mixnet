
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

import java.io.*;
import java.net.*;
import java.util.*;

import verificatum.eio.*;
import verificatum.ui.*;
import verificatum.protocol.*;

/**
 * This abstract base class represents a bulletin board. Protocols
 * should not use any subclass of this protocol directly. Use instead
 * the class {@link BullBoard} and initialize it with a subclass of
 * this class.
 *
 * @author Douglas Wikstrom
 */
public abstract class BullBoardBasic extends Protocol {

    /**
     * Create an instance of the bulletin board.
     *
     * @param sid Session identifier for this instance.
     * @param prot Protocol that invokes this protocol as a
     * subprotocol.
     */
    protected BullBoardBasic(String sid, Protocol prot) {
        super(sid, prot);
    }

    /**
     * Starts this bulletin board.
     *
     * @param log Logging context.
     */
    public abstract void start(Log log);

    /**
     * Stops this bulletin board.
     *
     * @param log Logging context.
     */
    public abstract void stop(Log log);

    /**
     * Publishes a message.
     *
     * @param messageLabel Label under which the message should be
     * stored.
     * @param message Message to be published.
     * @param log Logging context.
     */
    public abstract void publish(String messageLabel,
                                 ByteTreeBasic message,
                                 Log log);

    /**
     * Waits for a posting on the bulletin board as specified by the
     * parameters. This method must not block, i.e., it <b>must</b>
     * give an output after the maximal time.
     *
     * @param l Index of the party that wrote the message to be
     * read.
     * @param messageLabel Name of the file to be read.
     * @param addedTime Additional milliseconds to wait due to
     * computations performed by the publisher, both computations in
     * at the application layer.
     * @param maximalByteCount Maximal number of bytes in the
     * published message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param log Log context.
     * @return Information stored on the bulletin board under the
     * given label.
     */
    public abstract ByteTreeBasic waitFor(int l,
                                          String messageLabel,
                                          int addedTime,
                                          int maximalByteCount,
                                          int maximalRecursiveDepth,
                                          Log log);
}
