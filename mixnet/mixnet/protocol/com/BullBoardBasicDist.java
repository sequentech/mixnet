
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
import java.util.*;

import mixnet.eio.*;
import mixnet.crypto.*;
import mixnet.ui.*;
import mixnet.util.*;
import mixnet.protocol.*;

/**
 * Abstract base class for distributed bulletin board. It contains
 * classes for downloading messages of bounded size, and downloading
 * and verifying signatures from other parties within bounded time.
 *
 * <p>
 *
 * If you intend to implement a distributed bulletin board, then you
 * should probably subclass this class.
 *
 * @author Douglas Wikstrom
 */
public abstract class BullBoardBasicDist extends BullBoardBasic {

    /**
     * Number of milliseconds we pause inbetween download
     * attempts. Subclasses are expected to minimize this time by
     * using interrupts.
     */
    public int pauseTime;

    /**
     * URLs to the HTTP servers of all parties.
     */
    protected URL[] http;

    /**
     * Directory where our HTTP server is rooted.
     */
    protected File http_dir;

    /**
     * URL at which this party listens, which may be different from
     * the URL used by other parties, e.g., if this party is behind a
     * NAT.
     */
    protected URL httpl;

    /**
     * Public signature keys of all parties.
     */
    protected SignaturePKey[] pkeys;

    /**
     * Private signature key.
     */
    protected SignatureSKey skey;

    /**
     * HTTP server of this instance.
     */
    protected SimpleHTTPServer httpServer;

    /**
     * Maximal number of concurrent connections to the HTTP server.
     */
    protected int backLog;

    /**
     * Indicates if this server is running or not.
     */
    protected boolean running;

    /**
     * Creates an instance.
     *
     * @param protocol Protocol which invokes this one.
     * @param http URLs to the HTTP servers of all parties.
     * @param httpl URL at which this party listens, which may be
     * different from the URL used by other parties, e.g., if this
     * party is behind a NAT.
     * @param http_dir Directory where our HTTP server is rooted.
     * @param pkeys Public signature keys of all parties.
     * @param skey Private signature key.
     * @param backLog Maximal number of concurrent connections to the
     * HTTP server.
     * @param pauseTime Number of milliseconds paused inbetween
     * download attempts.
     */
    public BullBoardBasicDist(Protocol protocol,
                              URL[] http,
                              URL httpl,
                              File http_dir,
                              SignaturePKey[] pkeys,
                              SignatureSKey skey,
                              int backLog,
                              int pauseTime) {

        // This sid has special meaning for protocol.Protocol. Do not
        // change!
        super("BullBoard", protocol);
        this.http = http;

        if (backLog <= 0) {
            throw new ProtocolError("Non-positive back log!");
        }
        this.backLog = backLog;

        this.httpl = httpl;
        this.http_dir = http_dir;
        this.pkeys = pkeys;
        this.skey = skey;

        this.pauseTime = pauseTime;
    }


    /**
     * Returns a URL to what is logically the data of the party with
     * the given index. Depending on how this is implemented this
     * party may actually download from the party with the given index
     * or from some other source.
     *
     * @param loc Index of party.
     * @return A URL.
     */
    protected abstract URL http(int loc);

    /**
     * Signal that something was written to its HTTP server by this
     * party. The bulletin boards of other parties may receive the
     * signal and interrupt waiting in {@link
     * #waitForAtMost(int,int)}.
     */
    protected abstract void signalWrite();

    /**
     * Wait for at most the given number of milliseconds or until
     * interrupted. The interrupt may be the result of another party
     * calling its {@link #signalWrite()} method.
     *
     * @param l Index of party from which we are waiting for an
     * interruption.
     * @param waitTime Maximal number of milliseconds to wait.
     */
    protected abstract void waitForAtMost(int l, int waitTime);




    // Implemented in terms of the above.

    /**
     * Starts this server.
     *
     * @param log Logging context.
     */
    public void start(Log log) {
        if (!running) {

            log.info("Starting http server.");
            httpServer = new SimpleHTTPServer(http_dir, httpl, backLog);
            httpServer.start();

            running = true;
        }
    }

    /**
     * Stops this server.
     *
     * @param log Logging context.
     */
    public void stop(Log log) {
        if (running) {

            log.info("Stopping http server.");
            httpServer.stop();

            running = false;
        }
    }

    /**
     * Creates a new signature-label with a postfix from the indicated
     * party that represents the signature of the message published
     * under the given label.
     *
     * @param label Original label.
     * @param i Index of signing party.
     * @return Label representing the signature.
     */
    protected String sigPostfix(String label, int i) {
        return label + ".sig." + i;
    }

    /**
     * Adds a party-specific prefix to a filename.
     *
     * @param l Index of party.
     * @param label Label to be prefixed.
     * @return Prefixed filename.
     */
    protected String partyPrefix(int l, String label) {
        return "" + l + "/" + label;
    }


    // ############### Reads and writes raw data ##################

    /**
     * Reads the given data from the party with index
     * <code>loc</code>. The output is guaranteed to represent a
     * proper byte tree with no spurious bytes at the end.
     *
     * @param loc Index of party that should have put the data on its
     * HTTP server.
     * @param relativeFileName Relative filename of requested data.
     * @param maximalByteLength Maximal number of bytes in the
     * published message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param readTimeout Maximal time spent waiting for the data.
     * @param log Log context.
     * @return Byte tree representation of the data downloaded from
     * the HTTP server of the party with index <code>loc</code>, or
     * <code>null</code> if the download failed.
     */
    protected ByteTreeBasic readData(int loc,
                                     String relativeFileName,
                                     long maximalByteLength,
                                     int maximalRecursiveDepth,
                                     int readTimeout,
                                     Log log) {

        // Attempt to fetch the data.
        boolean result = SimpleHTTPClient.fetchFile(http(loc),
                                                    directory,
                                                    relativeFileName,
                                                    readTimeout,
                                                    maximalByteLength,
                                                    log);

        // Did we download anything?
        if (result) {

            // Verify that the downloaded file represents a byte tree
            // that is not too deep.
            File file = new File(directory, relativeFileName);

            if (!ByteTreeF.verifyFormat(file, maximalRecursiveDepth)) {
                return null;
            }

            // If everything is ok we return data that is now
            // guaranteed to represent a proper byte tree of limited
            // total size and with limited depth.
            return new ByteTreeF(file);

        }
        return null;
    }

    /**
     * Spends at most the time given by the timer reading the given
     * data from the party with index <code>loc</code>.
     *
     * @param loc Index of party that should have put the file on its
     * HTTP server.
     * @param relativeFileName Filename of requested information.
     * @param maximalByteLength Maximal number of bytes in the
     * published message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param timer Timer that keeps track of how long we may try to
     * download.
     * @param log Log context.
     * @return Byte tree representation of the file downloaded from
     * the HTTP server of the party with index <code>loc</code>, or
     * <code>null</code> if the download failed.
     */
    protected ByteTreeBasic readData(int loc,
                                     String relativeFileName,
                                     int maximalByteLength,
                                     int maximalRecursiveDepth,
                                     Timer timer,
                                     Log log) {

        while (!timer.timeIsUp()) {

            ByteTreeBasic data =
                readData(loc, relativeFileName, maximalByteLength,
                         maximalRecursiveDepth, timer.remainingTime(),
                         log);

            if (data != null) {

                return data;

            } else {

                int waitTime = Math.min(pauseTime, timer.remainingTime());
                waitForAtMost(loc, waitTime);

            }
        }
        return null;
    }

    /**
     * Puts the data on the HTTP server.
     *
     * @param relativeFileName Filename where data is stored.
     * @param data Data to be stored.
     * @param log Log context.
     * @return Number of bytes in data.
     */
    protected long writeData(String relativeFileName,
                             ByteTreeBasic data,
                             Log log) {

        // Write to root of HTTP server.
        File file = new File(http_dir, relativeFileName);

        // Make sure directory exists.
        File parent = file.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new ProtocolError("Unable to make directory! (" +
                                    parent + ")");
        }

        // Write data atomically.
        File tmpFile = getFile("tmp");
        data.unsafeWriteTo(tmpFile);
        if (!tmpFile.renameTo(file)) {
            throw new ProtocolError("Unable to write file atomically!");
        }

        signalWrite();

        return file.length();
    }


    // ############### Reads and writes messages ##################

    /**
     * Puts the data on the HTTP server as published by the given
     * party.
     *
     * @param l Index of original publisher of the data (this may be
     * different from the index of this party).
     * @param messageLabel Label under which the message should be
     * stored.
     * @param message Message to write.
     * @param log Log context.
     * @return Number of bytes in data.
     */
    protected long writeMessage(int l,
                                String messageLabel,
                                ByteTreeBasic message,
                                Log log) {
        return writeData(partyPrefix(l, messageLabel), message, log);
    }

    /**
     * Puts the data on the HTTP server as published by this party.
     *
     * @param messageLabel Label under which the message should be
     * stored.
     * @param message Message to write.
     * @param log Log context.
     * @return Number of bytes in data.
     */
    protected long writeMessage(String messageLabel,
                                ByteTreeBasic message,
                                Log log) {
        return writeMessage(j, messageLabel, message, log);
    }

    /**
     * Spends at most the time given by the timer reading the given
     * message from the party with index <code>loc</code>.
     *
     * @param loc Index of party that should have put the file on its
     * HTTP server.
     * @param l Index of supposed producer of message.
     * @param messageLabel Message label of requested message.
     * @param maximalByteLength Maximal number of bytes in the
     * published message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param timer Timer that keeps track of how long we may try to
     * download.
     * @param log Log context.
     * @return Byte tree representation of the file downloaded from
     * the HTTP server of the party with index <code>loc</code>, or
     * <code>null</code> if the download failed.
     */
    protected ByteTreeBasic readMessage(int loc,
                                        int l,
                                        String messageLabel,
                                        int maximalByteLength,
                                        int maximalRecursiveDepth,
                                        Timer timer,
                                        Log log) {

        return readData(loc, partyPrefix(l, messageLabel),
                        maximalByteLength, maximalRecursiveDepth, timer, log);
    }


    /**
     * Returns a full message that embeds the index of the signer, the
     * message label, and the original message.
     *
     * @param l Index of original sender.
     * @param messageLabel Message label under which the message is
     * published.
     * @param message Original message.
     * @return Constructed byte tree.
     */
    protected ByteTreeBasic fullMessage(int l,
                                        String messageLabel,
                                        ByteTreeBasic message) {

        // Use index of original sender and the message label as
        // prefix. This is safe due to the invertability of how a
        // ByteTree is encoded as a byte[].
        ByteTree labelByteTree =
            new ByteTree(partyPrefix(l, messageLabel).getBytes());
        return new ByteTreeContainer(labelByteTree, message);
    }



    // ############### Computes digests ###########################

    /**
     * Computes a digest of a message.
     *
     * @param l Index of original sender.
     * @param messageLabel Message label under which the message is
     * published.
     * @param message Original message.
     * @param s Index of owner of the hashfunction used to compute
     * digest.
     * @return Digest of message.
     */
    protected byte[] digestOfMessage(int l,
                                     String messageLabel,
                                     ByteTreeBasic message,
                                     int s) {
        Hashdigest hd = pkeys[s].getDigest();
        fullMessage(l, messageLabel, message).update(hd);
        return hd.digest();
    }

    /**
     * Computes a joint digest of a message.
     *
     * @param l Index of original sender.
     * @param messageLabel Message label under which the message is
     * published.
     * @param message Original message.
     * @param jointHashfunction Hashfunction used to compute joint
     * digest.
     * @return Joint digest of message.
     */
    protected byte[] jointDigestOfMessage(int l,
                                          String messageLabel,
                                          ByteTreeBasic message,
                                          Hashfunction jointHashfunction) {
        Hashdigest hd = jointHashfunction.getDigest();
        fullMessage(l, messageLabel, message).update(hd);
        return hd.digest();
    }

    /**
     * Computes a digest of a joint digest.
     *
     * @param jointDigest Digest of which we compute the digest.
     * @param s Index of owner of the hashfunction used to compute
     * digest.
     * @return Digest of joint digest.
     */
    protected byte[] digestOfJointDigest(byte[] jointDigest, int s) {
        Hashdigest hd = pkeys[s].getDigest();
        hd.update(jointDigest);
        return hd.digest();
    }


    // ############### Reads and writes signatures ############

    /**
     * Spends at most the time given by the timer to download, from
     * Party <code>loc</code>, a valid signature computed by Party
     * <code>s</code> of a message originally published by Party
     * <code>l</code> or a joint digest (depending on the parameters).
     *
     * @param loc Index of party that should have put the file on its
     * HTTP server.
     * @param l Index of supposed producer of message.
     * @param messageLabel Message label of requested message.
     * @param s Index of supposed signer.
     * @param message Message of which we seek a signature. This
     * should be null to use the joint digest instead.
     * @param jointDigest Joint digest of message of which we seek a
     * signature. This should be null if the message is used directly.
     * @param maximalSignatureByteLength Maximal number of bytes in a
     * signature.
     * @param timer Timer indicating how much time we can spend on
     * downloading a signature.
     * @param log Log context.
     * @return <code>true</code> or <code>false</code> depending on if
     * a valid signature could be downloaded or not.
     */
    protected boolean readSignature(int loc,
                                    int l,
                                    String messageLabel,
                                    int s,
                                    ByteTreeBasic message,
                                    byte[] jointDigest,
                                    int maximalSignatureByteLength,
                                    Timer timer,
                                    Log log) {

        // Download signature.
        ByteTreeBasic signature =
            readData(loc,
                     sigPostfix(partyPrefix(l, messageLabel), s),
                     maximalSignatureByteLength,
                     0, // This implies that the signature must be a leaf.
                     timer,
                     log);

        // Did we download a candidate signature?
        if (signature != null) {

            try {

                // Compute digest.
                byte[] digest;
                if (message != null) {
                    digest = digestOfMessage(l, messageLabel, message, s);
                } else {
                    digest = digestOfJointDigest(jointDigest, s);
                }

                // Is the candidate signature valid? (this is safe
                // since the signature is a leaf of limited size)
                byte[] signatureBytes = signature.getByteTreeReader().read();
                boolean res = pkeys[s].verifyDigest(signatureBytes, digest);
                if (!res) {
                    log.info("Invalid signature!");
                }
                return res;

            } catch (EIOException eioe) {
                log.info("Unable to extract signature from ByteTree!");
                log.register(eioe);
                return false;
            }

        } else {

            log.info("Unable to download signature!");
            return false;

        }
    }

    /**
     * Publishes a signature of the full message derived from the
     * index of the publisher, the message label, and the message or
     * joint digest (depending on the parameters).
     *
     * @param l Index of publisher of the message.
     * @param messageLabel Label under which the message should be
     * stored.
     * @param message Message to be signed. This should be null if a
     * joint digest is meant to be signed.
     * @param jointDigest Digest to be signed. This should be null if
     * a message is meant to be signed.
     * @param log Log context.
     */
    protected void writeSignature(int l,
                                  String messageLabel,
                                  ByteTreeBasic message,
                                  byte[] jointDigest,
                                  Log log) {

        // Compute digest using our key.
        byte[] digest;
        if (jointDigest == null) {
            digest = digestOfMessage(l, messageLabel, message, j);
        } else {
            digest = digestOfJointDigest(jointDigest, j);
        }

        // Sign digest.
        byte[] signatureBytes = skey.signDigest(randomSource, digest);
        ByteTree signature = new ByteTree(signatureBytes);

        // Write signature.
        writeData(sigPostfix(partyPrefix(l, messageLabel), j), signature, log);
    }

    /**
     * Spends at most the time given by the timer to download, from
     * Party <code>loc</code>, a message originally published by Party
     * <code>l</code> and a valid signature of the publisher. If
     * <code>jointHashfunction</code> is not null, then
     * <code>jointDigest</code> is assumed to be of size
     * <code>jointHashfunction</code>.
     *
     * @param loc Index of party that should have put the file on its
     * HTTP server.
     * @param l Index of supposed producer of message.
     * @param messageLabel Filename of requested message.
     * @param maximalByteLength Maximal number of bytes in the
     * published message.
     * @param maximalRecursiveDepth Maximal recursive depth of
     * downloaded message.
     * @param maximalSignatureByteLength Maximal number of bytes in a
     * signature.
     * @param timer Timer indicating how much time we can spend on
     * downloading a signature.
     * @param log Log context.
     * @param jointHashfunction Joint hashfunction. If this is not
     * null then digest is computed using this hashfunction and the
     * downloaded signature is a signature of the digest.
     * @return Byte tree representation of the file
     * downloaded from the HTTP server of the party with index
     * <code>loc</code>, or <code>null</code> if the download failed.
     */
    protected Pair<ByteTreeBasic,byte[]>
        readMessAndSig(int loc,
                       int l,
                       String messageLabel,
                       int maximalByteLength,
                       int maximalRecursiveDepth,
                       int maximalSignatureByteLength,
                       Timer timer,
                       Log log,
                       Hashfunction jointHashfunction) {

        ByteTreeBasic message = null;
        byte[] jointDigest = null;

        do {

            // Try to download the message.
            message = readMessage(loc, l, messageLabel, maximalByteLength,
                                  maximalRecursiveDepth, timer, log);

            // Download and verify signature.
            if (message != null) {

                // If there is a joint hashfunction we use it first to
                // compress the message.
                if (jointHashfunction != null) {
                    jointDigest =
                        jointDigestOfMessage(l, messageLabel, message,
                                             jointHashfunction);
                }

                // Read signature.
                if (!readSignature(loc, l, messageLabel, l, message,
                                   jointDigest, maximalSignatureByteLength,
                                   timer, log)) {
                    message = null;
                }
            }

        } while (message == null && !timer.timeIsUp());

        return new Pair<ByteTreeBasic,byte[]>(message, jointDigest);
    }
}
