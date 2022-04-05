
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

package mixnet.protocol.com;

import java.io.*;
import java.net.*;

import mixnet.eio.*;
import mixnet.crypto.*;
import mixnet.ui.*;
import mixnet.protocol.*;

/**
 * This class allows several protocols to share a single instance of
 * {@link BullBoardBasic} in the natural hierarchical way using unique
 * instance identifiers. Essentially each instance of this class is
 * given its own "scope" of the bulletin board where messages can be
 * published or read.
 *
 * @author Douglas Wikstrom
 */
public class BullBoard extends Protocol {

    /**
     * Default additional milliseconds to wait due to computing
     * performed by the publisher.
     */
    protected int defaultAddedTime;

    /**
     * Default maximal number of bytes of downloaded message.
     */
    protected int defaultMaximalByteLength;

    /**
     * Default maximal recursive depth of downloaded message.
     */
    protected int defaultMaximalRecursiveDepth;

    /**
     * Provides the underlying functionality.
     */
    protected BullBoardBasic bullBoardBasic;

    /**
     * Creates an instance.
     *
     * @param protocol Protocol which invokes this one.
     * @param bullBoardBasic Underlying bulletin board shared by all
     * instances of this class.
     * @param defaultAddedTime Default additional milliseconds to wait
     * due to computing performed by the publisher.
     * @param defaultMaximalByteLength Default maximal number of bytes
     * of downloaded message.
     * @param defaultMaximalRecursiveDepth Default maximal recursive
     * depth of downloaded message.
     */
    public BullBoard(Protocol protocol,
                     BullBoardBasic bullBoardBasic,
                     int defaultAddedTime,
                     int defaultMaximalByteLength,
                     int defaultMaximalRecursiveDepth) {

        // This sid has a special meaning in Protocol.java. Do not
        // change!
        super("BullBoard", protocol);

        this.bullBoardBasic = bullBoardBasic;
        this.defaultAddedTime = defaultAddedTime;
        this.defaultMaximalByteLength = defaultMaximalByteLength;
        this.defaultMaximalRecursiveDepth = defaultMaximalRecursiveDepth;
    }

    /**
     * Creates an instance associated with the protocol
     * <code>protocol</code>.
     *
     * @param protocol Protocol invoking this one.
     * @param bullBoard Bulletin board of which this instance is a
     * child.
     */
    public BullBoard(Protocol protocol, BullBoard bullBoard) {
        // This sid has special meaning for Protocol. Do not change!
        super("BullBoard", protocol);

        this.bullBoardBasic = bullBoard.bullBoardBasic;
        this.defaultAddedTime = bullBoard.defaultAddedTime;
        this.defaultMaximalByteLength = bullBoard.defaultMaximalByteLength;
        this.defaultMaximalRecursiveDepth =
            bullBoard.defaultMaximalRecursiveDepth;
    }

    /**
     * Start the underlying server unless it is already running.
     *
     * @param log Logging context.
     */
    public void start(Log log) {
        bullBoardBasic.start(log);
    }

    /**
     * Stop the underlying basic server if it is running.
     *
     * @param log Logging context.
     */
    public void stop(Log log) {
        bullBoardBasic.stop(log);
    }

    /**
     * Makes the labels of this instance globally unique.
     *
     * @param label Basic label.
     * @return Input label prepended with the full name of this
     * instance.
     */
    protected String marshal(String label) {
        return (getFullName() + "/" + label).replace("$", ".");
    }

    /**
     * Writes the string message on the bulletin board under the given
     * label.
     *
     * @param label Label under which the entry should be stored.
     * @param message Entry to be stored.
     * @param log Log context.
     */
    public void publish(String label, ByteTreeBasic message, Log log) {
        String fullLabel = marshal(label);
        bullBoardBasic.publish(fullLabel, message, log);
    }

    /**
     * Waits for a posting on the bulletin board as specified by the
     * parameters. This method does not block.
     *
     * @param l Index of the party that wrote the message to be
     * read.
     * @param label Label of the message to be read.
     * @param log Log context.
     * @return Information stored on the bulletin board as a
     * <code>ByteTree</code>.
     */
    public ByteTreeReader waitFor(int l, String label, Log log) {
        return waitFor(l,
                       label,
                       defaultAddedTime,
                       defaultMaximalByteLength,
                       log);
    }

    /**
     * Waits for a posting on the bulletin board as specified by the
     * parameters. This method does not block.
     *
     * @param l Index of the party that wrote the message to be
     * read.
     * @param label Label of the message to be read.
     * @param maximalByteLength Maximal number of bytes in the
     * downloaded message.
     * @param log Log context.
     * @return Information stored on the bulletin board under the
     * given label as a <code>ByteTree</code>.
     */
    public ByteTreeReader waitFor(int l,
                                  String label,
                                  int maximalByteLength,
                                  Log log) {
        String fullLabel = marshal(label);
        ByteTreeBasic bt = bullBoardBasic.waitFor(l,
                                                  fullLabel,
                                                  defaultAddedTime,
                                                  maximalByteLength,
                                                  defaultMaximalRecursiveDepth,
                                                  log);
        return bt.getByteTreeReader();
    }

    /**
     * Waits for a posting on the bulletin board as specified by the
     * parameters. This method does not block.
     *
     * @param l Index of the party that wrote the message to be
     * read.
     * @param label Label of the message to be read.
     * @param addedTime Additional milliseconds to wait due to
     * computations performed by the publisher.
     * @param maximalByteLength Maximal number of bytes in the
     * downloaded message.
     * @param log Log context.
     * @return Information stored on the bulletin board under the
     * given label as a <code>ByteTree</code>.
     */
    public ByteTreeReader waitFor(int l,
                                  String label,
                                  int addedTime,
                                  int maximalByteLength,
                                  Log log) {
        String fullLabel = marshal(label);
        ByteTreeBasic bt = bullBoardBasic.waitFor(l,
                                                  fullLabel,
                                                  addedTime,
                                                  maximalByteLength,
                                                  defaultMaximalRecursiveDepth,
                                                  log);
        return bt.getByteTreeReader();
    }

    /**
     * Waits for a posting on the bulletin board as specified by the
     * parameters. This method does not block.
     *
     * @param l Index of the party that wrote the message to be
     * read.
     * @param label Label of the message to be read.
     * @param addedTime Additional milliseconds to wait due to
     * computations performed by the publisher.
     * @param maximalByteLength Maximal number of bytes in the
     * downloaded message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param log Log context.
     * @return Information stored on the bulletin board under the
     * given label as a <code>ByteTree</code>.
     */
    public ByteTreeReader waitFor(int l,
                                  String label,
                                  int addedTime,
                                  int maximalByteLength,
                                  int maximalRecursiveDepth,
                                  Log log) {
        String fullLabel = marshal(label);
        ByteTreeBasic bt = bullBoardBasic.waitFor(l,
                                                  fullLabel,
                                                  addedTime,
                                                  maximalByteLength,
                                                  maximalRecursiveDepth,
                                                  log);
        return bt.getByteTreeReader();
    }
}
