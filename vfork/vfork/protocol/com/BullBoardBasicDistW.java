
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

package vfork.protocol.com;

import java.io.*;
import java.net.*;
import java.util.*;

import vfork.eio.*;
import vfork.crypto.*;
import vfork.ui.*;
import vfork.util.*;
import vfork.protocol.*;

/**
 * Abstract base class for distributed bulletin board. It contains
 * classes for downloading messages of bounded size, and downloading
 * and verifying signatures from other parties within bounded time.
 *
 * <p>
 *
 * Subclass this class to implement the logics of a particular
 * distributed bulletin board.
 *
 * @author Douglas Wikstrom
 */
public class BullBoardBasicDistW extends BullBoardBasicDist {

    public int exceptionCount = 100;

    /**
     * Socket addresses of the hint servers of all parties.
     */
    protected InetSocketAddress[] hints;

    /**
     * Socket address at which the hint server of this party listens,
     * which may be different from the socket address used by other
     * parties, e.g., if this party is behind a NAT.
     */
    protected InetSocketAddress hintServerAddress;

    /**
     * Hint server of this instance.
     */
    protected HintServer hintServer;

    /**
     * Keeps track of the parties marked as corrupted.
     */
    protected boolean[] corrupted;

    /**
     * Joint hashfunction used to compress messages before signing.
     */
    protected Hashfunction jointHashfunction;

    /**
     * Upper bound on the byte length of a signature.
     */
    protected int maximalSignatureByteLength;

    /**
     * Time to wait for a signature to be computed.
     */
    protected int signatureWaitTime;

    /**
     * Time to wait for a signature to be computed when there was an
     * error.
     */
    protected int errorSignatureWaitTime;


    /**
     * Creates an instance.
     *
     * @param protocol Protocol which invokes this one.
     * @param http URLs to the HTTP servers of all parties.
     * @param httpl URL at which this party listens, which may be
     * different from the URL used by other parties, e.g., if this
     * party is behind a NAT.
     * @param http_dir Directory where our HTTP server is rooted.
     * @param hints Socket addresses of the hint servers of all parties.
     * @param hintl Socket address at which the hint server of this
     * party listens, which may be different from the socket address
     * used by other parties, e.g., if this party is behind a NAT.
     * @param pkeys Public signature keys of all parties.
     * @param skey Private signature key.
     * @param backLog Maximal number of concurrent connections to the
     * HTTP server.
     * @param jointHashfunction Joint hashfunction used to compress
     * messages before signing.
     * @param pauseTime Number of milliseconds we pause inbetween
     * download attempts.
     * @param maximalSignatureByteLength Upper bound on the byte
     * length of a signature.
     * @param signatureWaitTime Time to wait for a signature to be
     * computed.
     * @param errorSignatureWaitTime Time to wait for a signature to
     * be computed when there was an error.
     */
    public BullBoardBasicDistW(Protocol protocol,
                               URL[] http,
                               URL httpl,
                               File http_dir,
                               String[] hints,
                               String hintl,
                               SignaturePKey[] pkeys,
                               SignatureSKey skey,
                               int backLog,
                               Hashfunction jointHashfunction,
                               int pauseTime,
                               int maximalSignatureByteLength,
                               int signatureWaitTime,
                               int errorSignatureWaitTime) {
        super(protocol, http, httpl, http_dir, pkeys, skey, backLog, pauseTime);

        // Addresses of hint servers of others.
        this.hints = new InetSocketAddress[k + 1];
        for (int i = 1; i <= k; i++) {
            String[] s = hints[i].split(":");
            if (s.length != 2) {
                throw new ProtocolError("Invalid hint server address! (" +
                                        hints[i] + ")");
            }
            try {
                int port = Integer.parseInt(s[1]);
                this.hints[i] = new InetSocketAddress(s[0], port);
            } catch (NumberFormatException nfe) {
                throw new ProtocolError("Invalid hint server address! (" +
                                        hints[i] + ")");
            }
        }

        // Address of hint server of this instance.
        String[] s = hintl.split(":");
        if (s.length != 2) {
            throw new ProtocolError("Invalid hint server address! (" +
                                    hintl + ")");
        }
        try {
            int port = Integer.parseInt(s[1]);
            this.hintServerAddress = new InetSocketAddress(s[0], port);
        } catch (NumberFormatException nfe) {
            throw new ProtocolError("Invalid hint server address! (" +
                                    hintl + ")");
        }

        corrupted = new boolean[k + 1];
        Arrays.fill(corrupted, false);

        this.jointHashfunction = jointHashfunction;

        this.maximalSignatureByteLength = maximalSignatureByteLength;
        this.signatureWaitTime = signatureWaitTime;
        this.errorSignatureWaitTime = errorSignatureWaitTime;
    }


    // Documented in BullBoardBasic.java

    public void start(Log log) {
        if (!running) {

            log.info("Starting hint server.");
            hintServer = new HintServer(hintServerAddress, j, k);
            hintServer.start();

            super.start(log);
        }
    }

    public void stop(Log log) {
        if (running) {

            log.info("Stopping hint server.");
            hintServer.stop();

            super.stop(log);
        }
    }

    protected URL http(int loc) {
        return http[loc];
    }

    protected void signalWrite() {

        // We signal everybody except ourselves.
        //
        // Note that it may happen that some servers that are not
        // downloading anything gets a hint anyway, which then
        // incorrectly triggers an immediate download attempt. To
        // avoid this one would need this method to be application
        // dependent which makes no sense.

        for (int l = 1; l <= k; l++) {
            if (l != j) {
                HintServer.hint(j, hints[l]);
            }
        }
    }

    protected void waitForAtMost(int l, int waitTime) {

        Sleeper sleeper = new Sleeper(waitTime);

        hintServer.setListener(l, sleeper);

        synchronized (sleeper) {

            sleeper.start();

            // We must manually check that no hint has already been
            // received. Note that this can not be done before
            // starting the thread, since the hint could come
            // inbetween these lines.
            hintServer.checkHint(l);
            try {
                sleeper.wait();
            } catch (InterruptedException ie) {}
        }
    }

    /**
     * Publishes the message under the label.
     *
     * @param messageLabel Label under which the message is published.
     * @param message Published message.
     * @param log Logging context.
     */
    public void publish(String messageLabel,
                        ByteTreeBasic message,
                        Log log) {

        writeMessage(messageLabel, message, log);

        // If a joint hashfunction is used, then we use it to compress
        // the message before signing.
        byte[] jointDigest = null;
        if (jointHashfunction != null) {

            jointDigest = jointDigestOfMessage(j, messageLabel, message,
                                               jointHashfunction);
            message = null;
        }
        writeSignature(j, messageLabel, message, jointDigest, log);

        waitFor(j, messageLabel, 0, 0, 0, log, message, jointDigest);
    }


    public ByteTreeBasic waitFor(int l,
                                 String messageLabel,
                                 int addedTime,
                                 int maximalByteLength,
                                 int maximalRecursiveDepth,
                                 Log log) {
        return waitFor(l, messageLabel, addedTime, maximalByteLength,
                       maximalRecursiveDepth, log, null, null);
    }

    /**
     * Waits for a posting on the bulletin board as specified by the
     * parameters. This method does not block.
     *
     * @param l Index of the party that wrote the message to be
     * read.
     * @param messageLabel Name of the file to be read.
     * @param addedTime Additional milliseconds to wait due to
     * computations performed by the publisher, both computations in
     * at the application layer.
     * @param maximalByteLength Maximal number of bytes in the
     * published message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param log Log context.
     * @param message The publisher should input the message to be
     * published here.
     * @param jointDigest Joint digest of the message.
     * @return Information stored on the bulletin board under the
     * given label.
     */
    protected ByteTreeBasic waitFor(int l,
                                    String messageLabel,
                                    int addedTime,
                                    int maximalByteLength,
                                    int maximalRecursiveDepth,
                                    Log log,
                                    ByteTreeBasic message,
                                    byte[] jointDigest) {

        // Zero indicates that we download a signature from the
        // publisher.
        int signatureIndex = 0;
        int waitTime = signatureWaitTime;

        int exceptionType = 0;
        int count = 0;

        for (;;) {

            if (signatureIndex == 0) {

                if (l == j) {

                    // If we are the publisher, then we do not do
                    // anything. We obviously produced a valid
                    // signature of our own message.

                    signatureIndex++;

                } else {

                    // If we are not the publisher, we try to download
                    // the message and a corresponding valid signature
                    // of the publisher on the message directly from
                    // the publisher.

                    Timer timer = new Timer(addedTime + signatureWaitTime);

                    Pair<ByteTreeBasic,byte[]> pair =
                        readMessAndSig(l,
                                       l,
                                       messageLabel,
                                       maximalByteLength,
                                       maximalRecursiveDepth,
                                       maximalSignatureByteLength,
                                       timer,
                                       log,
                                       jointHashfunction);
                    message = pair.first;
                    jointDigest = pair.second;

                    // If a message and signature was successfully
                    // downloaded, then we try to download signatures.
                    if (message != null) {
                        signatureIndex++;
                    }
                }
            }

            // This is false iff a valid signature could not be
            // downloaded from some party.
            boolean verdict = true;

            // Provided that we have successfully downloaded a message
            // and valid signature from the publisher, we try to
            // download the signatures of other parties and sign the
            // message downloaded from the publisher.
            while (0 < signatureIndex && signatureIndex <= k && verdict) {

                if (corrupted[signatureIndex]) {

                    // Ignore parties that are marked as corrupted.

                } else if (signatureIndex == l) {

                    // Valid signature of the publisher is already
                    // downloaded, so we do nothing.

                } else if (signatureIndex == j) {

                    // If it is our turn, we make our signature of the
                    // message available on our HTTP server.

                    writeSignature(l, messageLabel, message, jointDigest, log);

                } else {

                    // Otherwise, we try to download a valid signature
                    // from Party <signatureIndex>.

                    Timer timer = new Timer(waitTime);

                    verdict = readSignature(signatureIndex,
                                            l,
                                            messageLabel,
                                            signatureIndex,
                                            message,
                                            jointDigest,
                                            maximalSignatureByteLength,
                                            timer,
                                            log);
                }

                //catch (Throwable ce) {
                    //     if (exceptionType != 2 || count > exceptionCount) {
                    //         exceptionType = 2;
                    //         count = 0;
                    //     }
                    //     if (count == 0) {
                    //         log.info("Unable to connect to " + http(l) + "!");
                    //     }
                    //     verdict = false;
                    // }
                if (verdict) {
                    signatureIndex++;
                }
            }

            if (signatureIndex > k) {

                // If we have downloaded the message and valid
                // signatures from all parties, then we simply return
                // the message.

                return message;
            }

            String q =
                "The bulletin board failed. " +
                "You may agree with the administrators of the other " +
                "mix-servers to make another attempt to complete the " +
                "request to the bulletin board. Otherwise the " +
                "protocol will halt securely without an output. Would you " +
                "like to try again?";

            if (!ui.dialogQuery(q)) {
                throw new Error("No error resolution is implemented yet!");
            }
            waitTime = errorSignatureWaitTime;
        }
    }
}
