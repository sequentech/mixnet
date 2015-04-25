
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

package verificatum.protocol;

import java.io.*;
import java.net.*;
import java.util.*;

import verificatum.*;
import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.ui.*;
import verificatum.util.*;
import verificatum.protocol.com.*;
import verificatum.ui.info.*;

/**
 * Implements the basic functionality required by virtually every
 * cryptographic protocol. Any concrete protocol should subclass this
 * class or one of its subclasses.
 *
 * <p>
 *
 * More precisely, an instance of this class keeps the following data:
 *
 * <ul>
 *
 * <li> {@link #k} -- Number of parties.
 *
 * <li> {@link #j} -- Index of this party (indices start at one).
 *
 * <li> {@link #randomSource} -- Default source of (pseudo) random
 * bits.
 *
 * <li> A working directory dedicated to this instance. Use {@link
 * #getFile(String)} to get a file in this directory.
 *
 * <li> {@link #ui} -- User interface. This allows all the usual ways
 * of interacting with the user.
 *
 * <li> {@link #protocolInfo} -- Public information given in the
 * configuration file. Use {@link #getProtocolInfo()} to access it.
 *
 * <li> {@link #privateInfo} -- Private information given in the
 * configuration file. Use {@link #getPrivateInfo()} to access it.
 *
 * <li> {@link #bullBoard} -- Bulletin board used for communication.
 *
 * </ul>
 *
 * <p>
 *
 * There are two constructors in this class corresponding to if the
 * constructed instance is the main protocol or not.
 *
 * <p>
 *
 * The constructor of the main protocol should call {@link
 * #Protocol(PrivateInfo, ProtocolInfo, UI)}. This should normally be
 * done only once in an application. It initializes the above
 * variables based on the information in the public and private info
 * fields. It also sets the user interface.
 *
 * <p>
 *
 * For all subsequent subprotocols, the constructor {@link
 * #Protocol(String, Protocol)} should be used instead. The latter
 * copies some of the above fields from the parent protocol given as
 * input, and creates sub-versions of others, e.g., it creates a
 * subdirectory of the directory of the parent protocol. The idea is
 * that these variables should be easily accessible, since almost all
 * protocols need to access them.
 *
 * @author Douglas Wikstrom
 */
public class Protocol {

    /**
     * Time to wait in milliseconds after we have downloaded all other
     * servers completion acknowledgement to allow all other servers
     * to download our own completion acknowledgment.
     */
    public static final int WAIT_FOR_OTHERS_TIME = 1000;

    /**
     * Default number of milliseconds to wait inbetween download
     * attempts.
     */
    public static final int DEFAULT_PAUSE_TIME = 1;

    /**
     * Default upper bound on signature byte length.
     */
    public static final int DEFAULT_MAXIMAL_SIGNATURE_BYTE_LENGTH =
        1000 * 1024; /* One megabyte should be enough for any
                      * signature scheme. */

    /**
     * Default added time waiting for a signature to be computed.
     */
    public static final int DEFAULT_MAXIMAL_SIGNATURE_WAIT_TIME =
        1000 * 100;

    /**
     * Default added time waiting for a signature to be computed in
     * the event of an error.
     */
    public static final int DEFAULT_MAXIMAL_ERROR_SIGNATURE_WAIT_TIME =
        1000 * 1024;

    /**
     * Default added time for performing computations.
     */
    public static final int DEFAULT_ADDED_TIME = 1024 * 1024 * 1024;

    /**
     * Default upper bound on the number of bytes in a message.
     */
    public static final int DEFAULT_MAXIMAL_BYTE_LENGTH = 1024 * 1024 * 1024;

    /**
     * Default upper bound on the recursion depth.
     */
    public static final int DEFAULT_MAXIMAL_RECURSION_DEPTH = 10;

    // ######################################################

    /**
     * Name of file where the seed to the global PRG is stored.
     */
    public static final String SEED_FILENAME = ".prgseed_DO_NOT_TOUCH";

    /**
     * Name of temporary file where the seed to the global PRG is
     * stored before updating.
     */
    public static final String TMP_SEED_FILENAME = ".prgseed_DO_NOT_TOUCH_TMP";

    /**
     * Stores the full names of all instances of sub-protocols to make
     * sure that we keep them unique. Strictly speaking this is not
     * needed if all subprotocols are implemented correctly, but a bug
     * could be very dangerous and almost impossible to detect. Thus,
     * it seems prudent to check explicitly.
     */
    private final HashSet<String> allFullNames = new HashSet<String>();

    /**
     * Number of parties executing the protocol.
     */
    protected final int k;

    /**
     * Threshold number of parties needed to violate privacy.
     */
    protected int threshold;

    /**
     * Index of this instance/party.
     */
    public final int j;

    /**
     * Private information, e.g., seeds to pseudo-random generators
     * and secret keys.
     */
    protected PrivateInfo privateInfo;

    /**
     * Public protocol parameters and information needed to
     * communicate with the other parties.
     */
    protected ProtocolInfo protocolInfo;

    /**
     * User interface.
     */
    protected final UI ui;

    /**
     * Directory where this instance stores its files.
     */
    protected final File directory;

    /**
     * Default source of randomness used in the protocol.
     */
    public final RandomSource randomSource;

    /**
     * Certainty determining the error when checking consistency of
     * data, i.e., bad data is accepted with probability at most
     * 2^(-certainty).
     */
    public final int certainty;

    /**
     * Session identifier that singles out this instance from other
     * instances of this protocol invoked by the same protocol.
     */
    protected final String sid;

    /**
     * A unique name among all subprotocols.
     */
    private String fullName;

    /**
     * Bulletin board used to communicate.
     */
    protected final BullBoard bullBoard;

    /**
     * Name of directory of http server tag.
     */
    public final static String HTTP_DIR = "httpdir";

    /**
     * Name of http server type tag.
     */
    public final static String HTTP_TYPE = "httptype";

    /**
     * Name of the http-server tag.
     */
    public final static String HTTP = "http";

    /**
     * Name of listening http server type tag.
     */
    public final static String HTTPL = "httpl";

    /**
     * Name of the hint-server tag.
     */
    public final static String HINT = "hint";

    /**
     * Name of listening hint server type tag.
     */
    public final static String HINTL = "hintl";

    /**
     * Constructs a basic instance with the fields needed by any
     * instance of this class.
     *
     * @return Instance with all needed fields.
     */
    public static PrivateInfo newPrivateInfo() {
        PrivateInfo pi = new PrivateInfo();
        String s;

        s = "URL where our HTTP-server listens for connections, " +
            "which may be different from the HTTP address used to access " +
            "us, e.g., if we are behind a NAT.";
        pi.addInfoField(new StringField(HTTPL, s, 1, 1));

        s = "Root directory of our HTTP server.";
        pi.addInfoField(new StringField(HTTP_DIR, s, 1, 1));

        s = "Decides if an internal or external HTTP server is used " +
            "(legal values \"internal\" or \"external\").";
        pi.addInfoField(new StringField(HTTP_TYPE, s, 1, 1));

        s = "Socket address given as <hostname>:<port>, where our hint " +
            "server listens for connections.";
        pi.addInfoField(new StringField(HINTL, s, 1, 1));

        return pi;
    }

    /**
     * Constructs a basic instance with the fields needed by any
     * instance of this class.
     *
     * @return Instance with all needed fields.
     */
    public static ProtocolInfo newProtocolInfo() {
        String hintDescr =
            "Socket address given as <hostname>:<port> to our hint server. " +
            "A hint server is a simple UDP server that reduces latency and " +
            "traffic on the HTTP servers.";

        PartyInfoFactory pif =
            new PartyInfoFactory(new StringField(HTTP,
                                                 "URL to our HTTP server.",
                                                 1, 1),
                                 new StringField(HINT,
                                                 hintDescr,
                                                 1, 1));
        return new ProtocolInfo(pif);
    }

    /**
     * Returns a file representing a random seed as defined in the
     * given private info.
     *
     * @param privateInfo Private info of this party.
     * @return File representing random seed.
     */
    public static File seedFile(PrivateInfo privateInfo) {
        File dir = new File(privateInfo.getStringValue(PrivateInfo.DIR));
        return new File(dir, SEED_FILENAME);
    }

    /**
     * Returns the standard source of random bits as defined in the
     * given private info.
     *
     * @param privateInfo Private info of this party.
     * @return Source of random bits.
     */
    public static RandomSource randomSource(PrivateInfo privateInfo) {

        // Set up default source of randomness.
        String randomness =
            privateInfo.getStringValue(PrivateInfo.RANDOMNESS);
        try {
            return Marshalizer.unmarshalHex_RandomSource(randomness);
        } catch (EIOException eioe) {
            throw new ProtocolError("Unable to use random source!", eioe);
        }
    }

    /**
     * Writes copyright information on the log.
     */
    public final void licenseLogEntry() {
        String info =
              "\n-----------------------------------------------------------"
            + "\n This protocol is based on the Verificatum software.       "
            + "\n Copyright 2008 2009 2010 2011 2012 Douglas Wikstrom. See "
            + "\n <http://www.verificatum.com>"
            + "\n"
            + "\n Verificatum is free software: you can redistribute it "
            + "\n and/or modify it under the terms of the GNU Lesser General"
            + "\n Public License as published by the Free Software "
            + "\n Foundation, either version 3 of the License, or (at your "
            + "\n option) any later version."
            + "\n"
            + "\n Verificatum is distributed in the hope that it will be "
            + "\n useful, but WITHOUT ANY WARRANTY; without even the implied"
            + "\n warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR "
            + "\n PURPOSE.  See the GNU Lesser General Public License for "
            + "\n more details."
            + "\n"
            + "\n You should have received a copy of the GNU Lesser General "
            + "\n Public License along with Verificatum.  If not, see "
            + "\n <http://www.gnu.org/licenses/>."
            + "\n-----------------------------------------------------------"
            + "\n The arithmetic of Verificatum is optionally mapped to "
            + "\n native calls to the GNU Multiple Precision Arithmetic "
            + "\n Library (GMP) generously made available under the LGPL "
            + "\n license. See <http://www.gmplib.org>."
            + "\n-----------------------------------------------------------";
        ui.getLog().plainInfo(info);
    }

    /**
     * Creates a root protocol. This constructor should normally only
     * be called once in each application. All other protocols should
     * be constructed by calling a constructor of a subclass of this
     * class that makes a super call to {@link
     * #Protocol(String,Protocol)}.
     *
     * @param privateInfo Information about this party.
     * @param protocolInfo Information about the protocol executed,
     * including information about other parties.
     * @param ui User interface.
     */
    public Protocol(PrivateInfo privateInfo,
                    ProtocolInfo protocolInfo,
                    UI ui) {

        this.privateInfo = privateInfo;
        this.protocolInfo = protocolInfo;
        this.ui = ui;

        // Set up the directory of this instance.
        this.directory = new File(privateInfo.getStringValue(PrivateInfo.DIR));
        directory.mkdirs();
        if (!directory.exists()) {
            throw new ProtocolError("Unable to create directory!");
        }

        // Create log file and instruct the logging context of the
        // user interface to write to it.
        try {
            File logFile = new File(directory, "log");
            PrintStream ps =
                new PrintStream(new FileOutputStream(logFile, true));
            ui.getLog().addLogStream(ps);
        } catch (FileNotFoundException fnfe) {
            throw new ProtocolError("Can not create log file!", fnfe);
        }

        // Session ID of root protocol. This is read from file and
        // separates this execution from all previous, provided that
        // the user provides a fresh session ID.
        String t = protocolInfo.getStringValue(ProtocolInfo.SESSIONID);

        // This is one way of removing any characters that behave
        // badly as directory names.
        this.sid = Hex.toHexString(t.getBytes());

        // Number of parties involved in the protocol.
        try {
            this.k = protocolInfo.getNumberOfParties();
        } catch (InfoException ie) {
            throw new ProtocolError("Can not read number of parties!", ie);
        }
        this.threshold = protocolInfo.getIntValue(ProtocolInfo.THRESHOLD);

        String name = privateInfo.getStringValue(PrivateInfo.NAME);
        try {
            this.j = protocolInfo.getIndex(name);
        } catch (InfoException ie) {
            throw new ProtocolError("Mismatching protocol info and " +
                                    "private info files!", ie);
        }

        // Default source of randomness used by all subprotocols.
        this.randomSource = randomSource(privateInfo);

        // If we get randomness from a PRG, then we need to seed it.
        if (this.randomSource instanceof PRG) {
            File seedFile = getFile(SEED_FILENAME);
            File tmpSeedFile = getFile(TMP_SEED_FILENAME);

            // We replace the seed before continuing to avoid reuse.
            try {
                ((PRG)this.randomSource).setSeedReplaceStored(seedFile,
                                                              tmpSeedFile);
            } catch (IOException ioe) {
                throw new ProtocolError("Unable to read or to write PRG seed!",
                                        ioe);
            }
        }

        // Certainty parameter.
        this.certainty = privateInfo.getIntValue(PrivateInfo.CERTAINTY);

        // Set a globally unique "fullname".
        setFullName(ui.getLog(), getNameAndSid());

        // Parse the public information of other servers.
        URL[] http = new URL[k + 1];
        String[] hints = new String[k + 1];
        SignaturePKey[] pkeys = new SignaturePKey[k + 1];

        try {
            for (int i = 1; i <= k; i++) {

                PartyInfo pi = protocolInfo.get(i);
                http[i] = new URL(pi.getStringValue(HTTP));
                hints[i] = pi.getStringValue(HINT);

                String pkeyString = pi.getStringValue(PartyInfo.PUB_KEY);

                pkeys[i] =
                    Marshalizer.unmarshalHexAux_SignaturePKey(pkeyString,
                                                              randomSource,
                                                              certainty);
            }
        } catch (EIOException eioe) {
            throw new ProtocolError("Unable to use public keys!", eioe);
        } catch (MalformedURLException murle) {
            throw new ProtocolError("Malformed party info!", murle);
        }

        // Make the directory of our HTTP server.
        File http_dir = new File(privateInfo.getStringValue(HTTP_DIR));
        http_dir.mkdirs();
        SignatureSKey skey = null;
        try {
            String keyPairString =
                privateInfo.getStringValue(PrivateInfo.PRIV_KEY);
            SignatureKeyPair keyPair =
                Marshalizer.unmarshalHexAux_SignatureKeyPair(keyPairString,
                                                             randomSource,
                                                             certainty);
            skey = keyPair.getSKey();
        } catch (EIOException eioe) {
            throw new ProtocolError("Unable to use secret key!", eioe);
        }

        // Determine if we are using the internal (simplified)
        // HTTP-server or an external one.
        int backLog = 10;
        if (privateInfo.getStringValue(HTTP_TYPE).equals("external")) {
            backLog = 0;
        }

        // If the server is run behind a firewall with portforwarding,
        // the local hostname may differ from the one used by other
        // parties.
        URL httpl = null;
        String hintl = null;
        try {
            httpl = new URL(privateInfo.getStringValue(HTTPL));
            hintl = privateInfo.getStringValue(HINTL);
        } catch (MalformedURLException murle) {
            throw new ProtocolError("Malformed party info!", murle);
        }

        // TODO: The reason for using a joint hashfunction is to speed
        // up the verification of the other parties' signatures, In
        // particular when provably secure collision resistant
        // hashfunctions are used, this should give a noticeable
        // speedup, but in this case it should be possible to first
        // generate a joint hashfunction using the bulletin board in
        // non-optimized mode, and then set the joint hashfunction for
        // all future invocations.

        Hashfunction jointHashfunction = null;
        //new HashfunctionHeuristic("SHA-256");


        BullBoardBasic bbb =
            new BullBoardBasicDistW(this,
                                    http,
                                    httpl,
                                    http_dir,
                                    hints,
                                    hintl,
                                    pkeys,
                                    skey,
                                    backLog,
                                    jointHashfunction,
                                    DEFAULT_PAUSE_TIME,
                                    DEFAULT_MAXIMAL_SIGNATURE_BYTE_LENGTH,
                                    DEFAULT_MAXIMAL_SIGNATURE_WAIT_TIME,
                                    DEFAULT_MAXIMAL_ERROR_SIGNATURE_WAIT_TIME);

        bullBoard = new BullBoard(this,
                                  bbb,
                                  DEFAULT_ADDED_TIME,
                                  DEFAULT_MAXIMAL_BYTE_LENGTH,
                                  DEFAULT_MAXIMAL_RECURSION_DEPTH);
    }

    /**
     * Returns the name of the snapshot file with the given index.
     *
     * @param index Index of snap shot file.
     */
    protected String snapShotName(int index) {
        return String.format(".snapshot%02d", index);
    }

    // TODO: this is ugly. it is not clear what is nice functionality
    // to provide
    /**
     * Make a snapshot of the working directory and the http directory.
     *
     * @param index Index of snapshot used to distinguish snapshots.
     */
    public void snapShot(int index) {

        try {

            // Make a snapshot of both the working directory
            // and the http directory.
            File dir = new File(privateInfo.getStringValue(PrivateInfo.DIR));
            File dirSnap = new File(dir, snapShotName(index));
            File wdSnap = new File(dirSnap, "wd");

            ExtIO.recursiveCopyDir(dir, wdSnap);

        } catch (InterruptedException ie) {
            throw new ProtocolError("Unable to make snapshop!", ie);
        } catch (IOException ioe) {
            throw new ProtocolError("Unable to make snapshop!", ioe);
        }
    }

    public void resetToSnapShot(int index) {

        File dir = new File(privateInfo.getStringValue(PrivateInfo.DIR));
        File http_dir =
            new File(privateInfo.getStringValue(Protocol.HTTP_DIR));

        File dirSnap = new File(dir, snapShotName(index));
        File wdSnap = new File(dirSnap, "wd");

        // Delete all files except the snapshots in the working
        // directory.
        for (File f: dir.listFiles()) {
            if (!f.getName().startsWith(".snapshot")) {
                f.delete();
            }
        }

        // Delete all obsolete snapshots.
        int i = index + 1;
        for (;;) {

            File obsoleteDirSnap = new File(dir, snapShotName(i));

            if (obsoleteDirSnap.exists()) {
                obsoleteDirSnap.delete();
            } else {
                break;
            }
            i++;
        }

        // Delete the entire http directory.
        String[] list = http_dir.list();
        for (int j = 0; j < list.length; j++) {
            if (!ExtIO.delete(new File(http_dir, list[j]))) {
                throw new ProtocolError("Unable to delete files in http " +
                                        "directory!");
            }
        }

        try {

            // Replace contents of working directory with the chosen
            // snapshot.
            ExtIO.recursiveCopyDir(wdSnap, dir);

        } catch (InterruptedException ie) {
            throw new ProtocolError("Unable to recover snapshop!", ie);
        } catch (IOException ioe) {
            throw new ProtocolError("Unable to recover snapshop!", ioe);
        }
    }

    /**
     * Starts the underlying servers.
     */
    public void startServers() {
        bullBoard.start(ui.getLog());

        ui.getLog().info("Synchronizing with the other servers. Please wait.");
        Log tempLog = ui.getLog().newChildLog();

        if (j == 1) {
            bullBoard.publish("Synchronize", new ByteTree(), tempLog);
        } else {
            bullBoard.waitFor(1, "Synchronize", tempLog);
        }
    }

    /**
     * Perform a graceful shutdown of the protocol. Every execution of
     * the protocol should end with a call to this method, since
     * before it returns it <b>tries</b> to guarantee that all other
     * parties have successfully completed the execution as well. It
     * also shuts down all servers and releases all allocated
     * resources.
     *
     * <p>
     *
     * Note that it is <b>impossible</b> to be sure that all parties
     * received that last message in a protocol. Thus, the goal of
     * this method is to exchange some dummy messages until we are
     * certain that no party is stuck in a real protocol step. We wait
     * for others to complete, but if this fails the others can safely
     * kill their servers, since all that remains is to download some
     * dummy messages.
     *
     * @param label Label used for shutdown messages.
     * @param log Logging context.
     */
    public void shutdown(String label, Log log) {
        waitForAllServers(label + 0, log);
        waitForAllServers(label + 1, log);

        log.info("Allow download of our acknowledgment for another " +
                 (WAIT_FOR_OTHERS_TIME / 1000) + " seconds.");

        try {
            Thread.sleep(WAIT_FOR_OTHERS_TIME);
        } catch (InterruptedException ie) {
            // User requested immediate shutdown. Do nothing.
        }
        bullBoard.stop(log);
    }

    /**
     * Creates a child instance of <code>protocol</code> with session
     * identifier <code>sid</code>. It copies most of the fields of
     * the input protocol, which is convenient when implementing
     * subprotocols. It also initializes an fresh instance of a
     * bulletin board that can be used by the constructed instance.
     *
     * @param sid Session identifier for this instance.
     * @param prot Protocol that invokes this protocol as a
     * subprotocol.
     */
    protected Protocol(String sid, Protocol prot) {
        this.sid = sid;
        this.privateInfo = prot.privateInfo;
        this.protocolInfo = prot.protocolInfo;
        this.k = prot.k;
        this.threshold = prot.threshold;
        this.j = prot.j;
        this.randomSource = prot.randomSource;
        this.certainty = prot.certainty;
        this.ui = prot.ui;

        // Create our own sub directory if it does not exist.
        this.directory = new File(prot.directory, getNameAndSid());

        // Make sure our directory exists.
        directory.mkdirs();
        if (!directory.exists()) {
            throw new ProtocolError("Unable to create directory!");
        }

        // Make sure we have a globally unique full name.
        setFullName(prot.ui.getLog(), prot.fullName + "/" + getNameAndSid());

        // A BullBoard should not have a BullBoard as a
        // subprotocol. The SID of a bulletin board has a special
        // meaning.
        if (sid.equals("BullBoard")) {

            this.bullBoard = null;

        // All other subprotocols should have their own BullBoard,
        // that is reduced to the BullBoard of the parent protocol,
        // but with a local "namespace".
        } else {
            this.bullBoard = new BullBoard(this, prot.bullBoard);
        }
    }

    /**
     * Returns the <code>PrivateInfo</code> this protocol was invoked with.
     *
     * @return PrivateInfo of this instance.
     */
    protected PrivateInfo getPrivateInfo() {
        return privateInfo;
    }

    /**
     * Returns the <code>ProtocolInfo</code> this protocol was invoked with.
     *
     * @return ProtocolInfo of this instance.
     */
    protected ProtocolInfo getProtocolInfo() {
        return protocolInfo;
    }

    /**
     * Returns the file corresponding to the input file name in the
     * directory of this instance.
     *
     * @param filename Filename.
     * @return A file with the given name in the directory of this
     * instance.
     */
    public final File getFile(String filename) {
        return new File(directory, filename);
    }

    /**
     * Returns the qualified name of the class of this instance.
     *
     * @return Name of this class.
     */
    protected final String getName() {
        return this.getClass().getName();
    }

    /**
     * Returns the concatenation of the (unqualified) name of this
     * class and the session identifier, or only the (unqualified)
     * name if session identifier is empty.
     *
     * @return Name of this class concatenated with the session
     * identifier.
     */
    protected final String getNameAndSid() {
        String name = getName();
        int index = name.lastIndexOf(".");
        if (index >= 0) {
            name = name.substring(index + 1);
        }
        if (sid.equals("")) {
            return name;
        } else {
            return name + "." + sid;
        }
    }

    /**
     * Sets the full name and verifies that no other protocol in this
     * virtual machine has the same full name.
     *
     * @param log Logging context.
     * @param fullName Full name of this protocol.
     */
    private void setFullName(Log log, String fullName) {
        this.fullName = fullName;
        if (allFullNames.contains(fullName)) {
            throw new ProtocolError("Full name "
                                    + fullName + " already exists.");
        }
        allFullNames.add(fullName);
    }

    /**
     * Returns the full name for this instance of the protocol that
     * is unique among all executing subprotocols.
     *
     * @return Full name of this instance.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Write the given integer to a file with the given name in the
     * working directory of this protocol. It is the responsibility of
     * the programmer to avoid naming conflicts with files created in
     * other ways.
     *
     * @param name Name of counter.
     * @param value Value to be stored.
     */
    public void writeCounter(String name, int value) {
        File file = getFile(name);
        ByteTree.intToByteTree(value).unsafeWriteTo(file);
    }

    /**
     * Read the integer stored in a file with the given name in the
     * working directory of this protocol.
     *
     * @param name Name of counter.
     * @param log Logging context.
     * @return Stored integer.
     */
    public int readCounter(String name, Log log) {
        File file = getFile(name);
        if (!file.exists()) {
            writeCounter(name, 0);
            return 0;
        } else {
            try {
                ByteTree bt = new ByteTree(file);
                return ByteTree.unsafeByteTreeToInt(bt);
            } catch (EIOException eioe) {
                throw new ProtocolError("Unable to read counter!", eioe);
            } catch (IOException ioe) {
                throw new ProtocolError("Unable to read counter!", ioe);
            }
        }
    }

    /**
     * Returns the value of a <code>boolean</code> value stored under
     * the given name in the working directory of this protocol. If no
     * value has been stored, then <code>false</code> is returned.
     *
     * @param name Name of value.
     * @return Boolean value of the stored value.
     */
    public boolean readBoolean(String name) {
        File file = getFile(name);
        return file.exists();
    }

    /**
     * Stores a <code>boolean</code> value with the given name in the
     * working directory of this protocol. It is the responsibility of
     * the programmer to avoid naming conflicts.
     *
     * @param name Name of value.
     */
    public void writeBoolean(String name) {
        try {
            File file = getFile(name);
            file.createNewFile();
        } catch (IOException ioe) {
            throw new ProtocolError("Unable to store boolean value!");
        }
    }

    /**
     * Deletes a previously stored boolean value.
     *
     * @param Name of value.
     */
    public void deleteBoolean(String name) {
        getFile(name).delete();
    }

    /**
     * Publish a special "done" message on the bulletin board and wait
     * until all other servers have done the same, and download these
     * messages. Then wait a little longer to allow the others to
     * download our message. This is invoked by {@link
     * #shutdown(String,Log)}. Use the label to call this method
     * sequentially more than once.
     *
     * @param label Label under which the waiting takes place.
     * @param log Logging context.
     */
    protected void waitForAllServers(String label, Log log) {

        log.info("Waiting for mutual shutdown acknowledgements.");

        Log tempLog = log.newChildLog();

        for (int i = 1; i <= k; i++) {

            if (i == j) {

                tempLog.info("Publish acknowledgement.");
                bullBoard.publish(label, new ByteTree(), tempLog);

            } else {

                tempLog.info("Wait for " + ui.getDescrString(i) +
                             " to acknowledge.");
                ByteTreeReader btr = bullBoard.waitFor(i, label, tempLog);
                btr.close();
            }
        }
    }
}
