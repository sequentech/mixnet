
/*
 * Copyright 2008 2009 2010 Douglas Wikstrom
 * Copyright 2015 Eduardo Robles
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

package vfork.protocol.mixnet;

import java.io.*;
import java.util.*;

import vfork.*;
import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.ui.*;
import vfork.ui.info.*;
import vfork.ui.opt.*;
import vfork.ui.tui.*;
import vfork.util.*;
import vfork.protocol.*;
import vfork.protocol.coinflip.*;
import vfork.protocol.hvzk.*;
import vfork.protocol.distrkeygen.*;

/**
 * Implements a mix-net.
 *
 * @author Douglas Wikstrom
 */
public class MixNetElGamal extends Protocol {

    // FORK
    /**
     * Maxim ciphertexts width.
     */
    public static final int MAX_WIDTH = 100000;

    /**
     * Protocol version implemented by this mix-net.
     */
    public static final String protocolVersion = "0.1";

    /**
     * Group over which the protocol is executed.
     */
    protected PGroup pGroup;

    /**
     * Plain Cramer-Shoup public keys used in subprotocols.
     */
    protected CryptoPKey[] pkeys;

    /**
     * Plain Cramer-Shoup secret key used in subprotocols.
     */
    protected CryptoSKey skey;

    /**
     * Distributed keys.
     */
    protected DKG dkg;

    /**
     * Directory where intermediate results and RO-proofs are output.
     */
    protected File exportDir;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Factory for protocol used to generate independent generator.
     */
    protected IndependentGeneratorsFactory igsFactory;

    /**
     * Factory for creating proofs of shuffles.
     */
    protected PoSFactory posFactory;

    /**
     * Factory for creating commitment-consistent proofs of shuffles.
     */
    protected CCPoSFactory ccposFactory;

    /**
     * Protocol used for verifiable decryption.
     */
    protected VerifiableDecryption verifiableDecryption;

    /**
     * Commitment of our permutation used in pre-computation phase.
     */
    protected PermutationCommitment[] permutationCommitments;

    /**
     * Independent generators used to form permutation commitments.
     */
    protected PGroupElementArray generators;

    /**
     * Reencryption exponents.
     */
    protected PRingElementArray reencExponents;

    /**
     * Reencryption factors.
     */
    protected PGroupElementArray reencFactors;

    /**
     * Underlying shuffle.
     */
    protected ElGamalReencShuffle shuffle;

    /**
     * Interface that determines the input and output formats of the
     * mixnet, i.e., the encoding used for the public key,
     * ciphertexts, and plaintexts.
     */
    protected MixNetElGamalInterface mixnetInterface;

    /**
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param hom Underlying homomorphism.
     * @param pkeys Plain public keys of all parties.
     * @param skey Plain secret key.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     * @param exportDir Export directory for universal verifiability.
     *
     *
     */
    public MixNetElGamal(String sid,
                         Protocol protocol,
                         PGroup pGroup,
                         CryptoPKey[] pkeys,
                         CryptoSKey skey,
                         int statDist,
                         DKG dkg,
                         IndependentGeneratorsFactory igsFactory,
                         PoSFactory posFactory,
                         CCPoSFactory ccposFactory,
                         VerifiableDecryption verifiableDecryption,
                         ElGamalReencShuffle shuffle,
                         MixNetElGamalInterface mixnetInterface,
                         File exportDir) {
        super(sid, protocol);
        this.pGroup = pGroup;
        this.pkeys = pkeys;
        this.skey = skey;
        this.statDist = statDist;
        this.dkg = dkg;
        this.igsFactory = igsFactory;
        this.posFactory = posFactory;
        this.ccposFactory = ccposFactory;
        this.verifiableDecryption = verifiableDecryption;
        this.shuffle = shuffle;
        this.mixnetInterface = mixnetInterface;
        this.exportDir = exportDir;
    }

    public MixNetElGamalInterface getInterface() {
        return mixnetInterface;
    }

    public PGroup getCiphPGroup() {
        return shuffle.getEncryptor(1).getArrayRange();
    }

    /**
     * Writes some execution information on the log.
     *
     * @param log Logging context.
     * @param j Index of this mix-server.
     */
    protected static void initLogEntry(Log log, int j) {
        String s = ""
            + "-----------------------------------------------------------"
            + "\n Mix-server " + j + " running mixnet based on repeated "
            + "\n re-encryption and permutation followed by joint       "
            + "\n decryption. Detailed information about all parameters "
            + "\n are found in the info files.                "
            + "\n-----------------------------------------------------------";
        log.plainInfo(s);
    }

    /**
     * Name of file containing the public key of the mix-server with
     * the given index.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index Index of mix-server.
     * @return File where the public key of the given party is stored.
     */
    protected static File PKfile(File exportDir, int index) {
        return new File(exportDir, String.format("PublicKey%02d.bt", index));
    }

    /**
     * Name of file containing the full public key of the mix-net.
     *
     * @param exportDir Export directory for universal verifiability.
     * @return File where the public key is stored.
     */
    protected static File FPKfile(File exportDir) {
        return new File(exportDir, "FullPublicKey.bt");
    }

    /**
     * Name of file containing an intermediate list of ciphertexts.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index Index of mix-server.
     * @return File where intermediate file is stored.
     */
    protected static File Lfile(File exportDir, int index) {
        return new File(exportDir,
                        String.format("CiphertextList%02d.bt", index));
    }

    /**
     * Perform key generation.
     *
     * @param log Logging context.
     * @return Joint public key to be used by senders.
     */
    public PGroupElement keyGenerationPhase(Log log) {
        log.info("Generate distributed keys for mix-servers.");

        Log tempLog = log.newChildLog();

        BiPRingPGroup biKey = shuffle.getBiKey();
        dkg.generate(tempLog, biKey, biKey.getPGroupDomain().getg());

        if (exportDir != null) {

            MixNetElGamalInterface rawInterface =
                MixNetElGamalInterface.getInterface("raw");

            rawInterface.writePublicKey(dkg.getFullPublicKey(),
                                        FPKfile(exportDir));

            for (int l = 1; l <= k; l++) {
                PGroupElement pk = dkg.getPublicKey(l);
                pk.toByteTree().unsafeWriteTo(PKfile(exportDir, l));
            }
        }

        return dkg.getFullPublicKey();
    }

    /**
     * Perform pre-computation.
     *
     * @param log Logging context.
     * @param precomputeOnly Indicates that precomputation is executed
     * and the result stored on file without any subsequent mixing.
     */
    protected void precomputationPhase(Log log, boolean precomputeOnly,
                                       int maximalNoCiphertexts) {

        ByteTreeReader reader;

        log.info("Perform pre-computation for " + maximalNoCiphertexts
                 + " senders.");
        Log tempLog = log.newChildLog();


        // Generate list of independent generators.
        IndependentGenerators igs =
            igsFactory.newInstance("generators", this);

        generators =
            igs.generate(tempLog, pGroup, maximalNoCiphertexts);

        // Generate permutation commitments.
        permutationCommitments = new PermutationCommitment[threshold + 1];
        for (int l = 1; l <= threshold; l++) {

            permutationCommitments[l] =
                new PermutationCommitment("" + l,
                                          this,
                                          l,
                                          maximalNoCiphertexts,
                                          generators,
                                          posFactory,
                                          statDist);
        }


        // Perform pre-computation in parallel.
        if (j <= threshold) {
            permutationCommitments[j].precompute(tempLog);
        }

        for (int l = 1; l <= threshold; l++) {

            permutationCommitments[l].generate(tempLog, exportDir);
        }

        BiKeyedArrayMap encryptor = shuffle.getEncryptor(maximalNoCiphertexts);

        PPRing pRingDomain = (PPRing)encryptor.getPRingDomain();
        APRing aPRing = (APRing)pRingDomain.project(1);
        PRing arrayPRing = aPRing.getContentPRing();

        PPGroup pGroupDomain = (PPGroup)encryptor.getPGroupDomain();
        APGroup aPGroupDomain = (APGroup)pGroupDomain.project(1);
        PGroup arrayPGroupDomain = aPGroupDomain.getContentPGroup();

        PPGroup pGroupRange = (PPGroup)encryptor.getRange();
        APGroup aPGroupRange = (APGroup)pGroupRange.project(1);
        PGroup arrayPGroupRange = aPGroupRange.getContentPGroup();

        if (j <= threshold) {

            File reencExponentsFile = getFile("reencExponents");
            File reencFactorsFile = getFile("reencFactors");

            if (reencExponentsFile.exists()) {

                // Read reencryption exponents from file.
                tempLog.info("Read reencryption exponents.");
                ByteTreeReader exponentsReader =
                    (new ByteTreeF(reencExponentsFile)).getByteTreeReader();
                try {
                    reencExponents =
                        arrayPRing.toElementArray(maximalNoCiphertexts,
                                                  exponentsReader);
                } catch (ArithmFormatException afe) {
                    throw new ArithmError("Unable to read exponents!", afe);
                }

                // Read reencryption factors from file.
                tempLog.info("Read reencryption factors.");
                ByteTreeReader factorsReader =
                    (new ByteTreeF(reencFactorsFile)).getByteTreeReader();
                try {
                    reencFactors =
                        arrayPGroupRange.toElementArray(maximalNoCiphertexts,
                                                        factorsReader);
                } catch (ArithmFormatException afe) {
                    throw new ArithmError("Unable to read factors!", afe);
                }

            } else {

                // Generate random exponents.
                tempLog.info("Generate reencryption exponents.");
                PRingElement pRingElement =
                    encryptor.getPRingDomain().randomElement(randomSource,
                                                             statDist);

                PRingElement reencExponent =
                    ((PPRingElement)pRingElement).project(1);
                reencExponents =
                    ((APRingElement)reencExponent).getContent();

                tempLog.info("Write reencryption exponents to file.");
                reencExponents.toByteTree().
                    unsafeWriteTo(reencExponentsFile);

                // Construct the group element input from the public key
                // and an array of ones.
                tempLog.info("Compute reencryption factors.");
                PGroupElement one = aPGroupDomain.getONE();
                PGroupElement pGroupElement =
                    pGroupDomain.product(dkg.getFullPublicKey(), one);

                // Compute reencryption factors.
                PGroupElement kcPair =
                    encryptor.map(pRingElement, pGroupElement);
                PGroupElement ciphertext = ((PPGroupElement)kcPair).project(1);
                reencFactors = ((APGroupElement)ciphertext).getContent();

                pGroupElement.free();

                tempLog.info("Write reencryption factors to file.");
                reencFactors.toByteTree().unsafeWriteTo(reencFactorsFile);
            }

        }

        if (precomputeOnly) {
            precomputationPhaseFree();
        }
    }

    protected void precomputationPhaseFree() {
        for (int l = 1; l <= threshold; l++) {
            permutationCommitments[l].free();
        }
        if (j <= threshold) {
            reencExponents.free();
            reencFactors.free();
        }
        generators.free();
    }

    /**
     * Shrinks generators, reencryption exponents and factors, as well
     * as commitments to the actual number of ciphertexts.
     *
     * @param log Logging context.
     * @param noCiphertexts Actual number of ciphertexts.
     */
    protected void shrinkToActualNoCiphertexts(Log log, int noCiphertexts) {

        log.info("Shrink precomputed values to " + noCiphertexts +
                 " ciphertexts.");
        Log tempLog = log.newChildLog();

        // Shrink independent generators.
        tempLog.info("Shrink number of generators.");
        PGroupElementArray oldGenerators = generators;
        generators = generators.copyOfRange(0, noCiphertexts);
        oldGenerators.free();

        if (j <= threshold) {

            // Shrink reencryption exponents and factors.
            tempLog.info("Shrink reencryption exponents.");
            PRingElementArray oldReencExponents = reencExponents;
            reencExponents = reencExponents.copyOfRange(0, noCiphertexts);
            oldReencExponents.free();

            tempLog.info("Shrink reencryption factors.");
            PGroupElementArray oldReencFactors = reencFactors;
            reencFactors = reencFactors.copyOfRange(0, noCiphertexts);
            oldReencFactors.free();
        }

        // Shrink permutation commitments.
        for (int l = 1; l <= threshold; l++) {
            permutationCommitments[l].shrink(tempLog, noCiphertexts, exportDir);
        }
    }

    /**
     *
     *
     */
    protected PGroupElementArray
        identifyCiphertexts(Log log, PGroupElementArray ciphertexts) {

        log.info("Identify common input ciphertexts.");
        Log tempLog = log.newChildLog();

        PGroupElementArray[] candidateCiphertexts =
            new PGroupElementArray[k + 1];
        Arrays.fill(candidateCiphertexts, null);

        int[] count = new int[k + 1];
        Arrays.fill(count, 0);

        HashfunctionHeuristic sha256 = new HashfunctionHeuristic("SHA-256");
        byte[][] candidateHashes = new byte[k + 1][];

        for (int l = 1; l <= k; l++) {

            if (l == j) {

                tempLog.info("Publish our input list of ciphertexts.");
                candidateCiphertexts[j] = ciphertexts;
                bullBoard.publish("IdentifyCiphertexts",
                                  ciphertexts.toByteTree(),
                                  tempLog);
            } else {

                tempLog.info("Read input list of ciphertexts of " +
                             ui.getDescrString(l) + ".");
                ByteTreeReader listReader =
                    bullBoard.waitFor(l, "IdentifyCiphertexts", tempLog);
                try {

                    candidateCiphertexts[l] =
                        getCiphPGroup().toElementArray(0, listReader);

                } catch (ArithmFormatException afe) {
                    tempLog.info("Malformed list! Ignored.");
                    continue;
                } finally {
                    listReader.close();
                }
            }

            if (candidateCiphertexts[l] != null) {
                Hashdigest hd = sha256.getDigest();
                candidateCiphertexts[l].toByteTree().update(hd);
                candidateHashes[l] = hd.digest();
            }
        }


        for (int l = 1; l <= k; l++) {

            if (candidateCiphertexts[l] != null) {

                for (int ll = l + 1; ll <= k; ll++) {

                    if (candidateCiphertexts[ll] != null
                        && Arrays.equals(candidateHashes[ll],
                                         candidateHashes[l])
                        && candidateCiphertexts[ll].
                           equals(candidateCiphertexts[l])) {

                        candidateCiphertexts[ll].free();
                        candidateCiphertexts[ll] = null;

                        count[l]++;
                    }
                }
            }
        }
        int ciphIndex = 0;
        for (int l = 1; l <= k; l++) {
            if (count[l] >= threshold) {
                ciphIndex = l;
            } else if (candidateCiphertexts[l] != null) {
                candidateCiphertexts[l].free();
            }
        }
        if (ciphIndex == 0) {
            throw new ProtocolError("Honest parties are using distinct " +
                                    "input lists of ciphertexts!");
        } else {
            return candidateCiphertexts[ciphIndex];
        }
    }

    /**
     * Perform re-encryption of the list of ciphertexs.
     *
     * @param log Logging context.
     * @param ciphertexts Ciphertexts to be decrypted
     */
    protected PGroupElementArray
        mixingPhase(Log log, PGroupElementArray ciphertexts) {

        log.info("Mix " + ciphertexts.size() + " ciphertexts.");
        Log tempLog = log.newChildLog();

        if (exportDir != null) {

            // Store original input for universal verifiability.
            ciphertexts.toByteTree().unsafeWriteTo(Lfile(exportDir, 0));
        }

        PGroupElementArray inputList = ciphertexts;
        PGroupElementArray outputList = null;

        // Bilinear encryption map used to prove
        BiKeyedArrayMap encryptor = shuffle.getEncryptor(1);

        PPGroup pGroupDomain = (PPGroup)encryptor.getPGroupDomain();
        APGroup aPGroupDomain = (APGroup)pGroupDomain.project(1);
        PGroup arrayPGroup = encryptor.getArrayRange();

        // Homomorphism
        PGroupElement one = aPGroupDomain.getONE();
        PGroupElement pGroupElement =
            pGroupDomain.product(dkg.getFullPublicKey(), one);
        HomPRingPGroup hom = encryptor.restrict(pGroupElement);

        for (int l = 1; l <= threshold; l++) {

            if (l == j) {

                // Process input list.
                PGroupElementArray reencList = inputList.mul(reencFactors);

                Permutation permutation =
                    permutationCommitments[j].getPermutation();
                outputList = reencList.permute(permutation.inv());
                reencList.free();

                // Publish our output.
                tempLog.info("Publish mixed list.");
                bullBoard.publish("CiphertextList",
                                  outputList.toByteTree(),
                                  tempLog);

                // Prove correctness of our output.
                CCPoS P = ccposFactory.newPoS("" + j, this);
                P.prove(tempLog,
                        generators.getPGroup().getg(),
                        generators,
                        permutationCommitments[j].getCommitment(),
                        hom,
                        dkg.getFullPublicKey(),
                        inputList,
                        outputList,
                        permutationCommitments[j].getCommitmentExponents(),
                        permutation,
                        reencExponents,
                        exportDir);

                if (exportDir != null) {

                    // Store our output for universal verifiability.
                    outputList.toByteTree().unsafeWriteTo(Lfile(exportDir, j));
                }

            } else {

                boolean correct = true;

                // Read the output of Party l.
                tempLog.info("Read output from " + ui.getDescrString(l) + ".");

                ByteTreeReader listReader =
                    bullBoard.waitFor(l, "CiphertextList", tempLog);
                try {

                    outputList =
                        arrayPGroup.toElementArray(inputList.size(),
                                                   listReader);

                } catch (ArithmFormatException afe) {

                    tempLog.info("");
                    correct = false;
                } finally {
                    listReader.close();
                }

                // Verify proof of correctness of Party l
                if (correct) {
                    CCPoS V = ccposFactory.newPoS("" + l, this);
                    correct = V.verify(tempLog,
                                       l,
                                       generators.getPGroup().getg(),
                                       generators,
                                       permutationCommitments[l].
                                            getCommitment(),
                                       hom,
                                       dkg.getFullPublicKey(),
                                       inputList,
                                       outputList,
                                       exportDir);
                    if (!correct) {
                        outputList.free();
                    }
                }

                if (!correct) {
                    outputList = inputList.copyOfRange(0, inputList.size());
                }

                if (exportDir != null) {

                    // Store the output for universal verifiability.
                    outputList.toByteTree().unsafeWriteTo(Lfile(exportDir, l));
                }
            }
            PGroupElementArray tmp = inputList;
            inputList = outputList;
            tmp.free();
        }
        pGroupElement.free();

        return inputList;
    }

    /**
     * Perform verifiable decryption of input.
     *
     * @param log Logging context.
     * @param ciphertexts Ciphertexts to be decrypted
     */
    protected PGroupElementArray
        decryptionPhase(Log log, PGroupElementArray ciphertexts) {

        if (exportDir != null) {

            File f = Lfile(exportDir, threshold);

            // If this is executed as a standalone step we need to
            // dump the input ciphertexts.
            if (!f.exists()) {

                // Store original input for universal verifiability.
                ciphertexts.toByteTree().unsafeWriteTo(f);
            }
        }


        int size = ciphertexts.size();
        return verifiableDecryption.decrypt(log,
                                            shuffle.getPartDecryptor(size),
                                            shuffle.getFinalizer(),
                                            ciphertexts,
                                            exportDir);
    }

    protected static void generateREADME(File exportDir) {
        File READMEfile = new File(exportDir, "README");

        String README =
"\n" +
"         UNIVERSALLY VERIFIABLE FIAT-SHAMIR PROOFS \n" +
"\n" +
"This directory contains files that together give a proof of\n" +
"correctness of an execution. A detailed description of the\n" +
"formats used and the contents of each file can be found at\n" +
"http://www.vfork.org. Here we only give a brief\n" +
"description of each file.\n" +
"\n" +
"FullPublickey<i>.bt          - Full public key used by senders to form\n" +
"                               their ciphertexts.\n" +
"PublicKey<i>.bt              - Public key of the ith mix-server.\n" +
"\n" +
"SecretKey<i>.bt              - Contains the secret key of the ith party if\n" +
"                               it was recovered during joint decryption.\n" +
"\n" +
"PermutationCommitment<i>.bt  - Permutation commitment of the ith\n" +
"                               mix-server.\n" +
"\n" +
"PoSCommitment<i>.bt          - The \"commitment\" of the prover in the\n" +
"                               proof of a shuffle used to show that\n" +
"                               the permutation commitment is correctly\n" +
"                               formed.\n" +
"\n" +
"PoSReply<i>.bt               - The \"reply\" of the prover in the\n" +
"                               proof of a shuffle used to show that\n" +
"                               the permutation commitment is correctly\n" +
"                               formed.\n" +
"\n" +
"KeepList<i>.bt               - If precomputation is used, then the\n" +
"                               permutation commitment must be shrunk\n" +
"                               before usage. The keep list is a boolean\n" +
"                               array indicating which components to keep.\n" +
"\n" +
"CiphertextList<i>.bt         - The ith intermediate list of ciphertexts,\n" +
"                               i.e., the output of the ith mix-server.\n" +
"                               The 0th list is the input ciphertexts.\n" +
"\n" +
"CCPoSCommitment<i>.bt        - The \"commitment\" of the prover in the\n" +
"                               commitment-consistent proof of a shuffle\n" +
"                               used to show that the permutation commitment\n"+
"                               is correctly formed.\n" +
"\n" +
"CCPoSReply<i>.bt             - The \"reply\" of the prover in the\n" +
"                               commitment-consistent proof of a shuffle\n" +
"                               used to show that the permutation commitment\n"+
"                               is correctly formed.\n" +
"\n" +
"DecryptionFactors<i>.bt      - The decryption factors of the ith mix-server\n"+
"                               used to jointly decrypt the shuffled\n" +
"                               ciphertexts.\n" +
"\n" +
"DecrFactCommitment<i>.bt     - The \"commitment\" of the prover in the\n" +
"                               proof of correctness of the decryption \n" +
"                               factors.\n" +
"\n" +
"DecrFactReply<i>.bt          - The \"reply\" of the prover in the proof of \n"+
"                               correctness of the decryption factors.\n" +
"\n" +
"PlaintextElements.bt         - List of plaintext elements (not decoded into " +
"                               strings)";


        try {
            ExtIO.writeString(READMEfile, README);
        } catch (IOException ioe) {
            // We ignore any problems to write this file.
        }
    }

    /**
     * Constructs an instance of the mix-net based on the protocol and
     * private info files.
     *
     * @param rootProtocol Root protocol.
     * @param privateInfo Private info of this party.
     * @param protocolInfo Protocol info of this party.
     * @param protocolInfoFilename Name of protocol info file.
     * @param ui User interface.
     * @return Instance of this protocol initialized as defined by the
     * protocol and private info files.
     */
    public static MixNetElGamal
        newMixNetElGamal(Protocol rootProtocol,
                         PrivateInfo privateInfo,
                         ProtocolInfo protocolInfo,
                         String protocolInfoFilename,
                         UI ui) {
        try {

            // Init root protocol.
            rootProtocol.startServers();

            // Extract additional security parameters.
            int statDist = protocolInfo.getIntValue("statdist");
            int cbitlen = protocolInfo.getIntValue("cbitlen");
            int cbitlenro = protocolInfo.getIntValue("cbitlenro");
            int vbitlen = protocolInfo.getIntValue("vbitlen");
            int vbitlenro = protocolInfo.getIntValue("vbitlenro");

            // Directory for temporary files.
            File tmpDir = rootProtocol.getFile("tmp");
            tmpDir.mkdirs();
            TempFile.init(tmpDir);

            // Decide if we are using arrays mapped to files or not.
            if (privateInfo.getStringValue("arrays").equals("file")) {

                LargeIntegerArray.useFileBased();

            } else if (!privateInfo.getStringValue("arrays").equals("ram")) {
                throw new ProtocolError("Unknown value (" +
                                        privateInfo.getStringValue("arrays") +
                                        ") of <arrays></arrays>!");
            }

            // Extract PRG to use to derive random vectors from
            // jointly generateed random seeds in batching.
            String prgString = protocolInfo.getStringValue("prg");

            PRG prg;
            if (prgString.equals("SHA-256")) {
                prg = new PRGHeuristic(new HashfunctionHeuristic("SHA-256"));
            } else if (prgString.equals("SHA-384")) {
                prg = new PRGHeuristic(new HashfunctionHeuristic("SHA-384"));
            } else if (prgString.equals("SHA-512")) {
                prg = new PRGHeuristic(new HashfunctionHeuristic("SHA-512"));
            } else {
                prg = Marshalizer.unmarshalHexAux_PRG(prgString,
                                                      rootProtocol.randomSource,
                                                      rootProtocol.certainty);
            }

            // Key generator for keys of the cryptosystem used by
            // subprotocols.
            String keygenString = privateInfo.getStringValue("keygen");
            RandomSource randomSource = rootProtocol.randomSource;
            CryptoKeyGen keyGen =
                Marshalizer.unmarshalHexAux_CryptoKeyGen(keygenString,
                                                       randomSource,
                                                       rootProtocol.certainty);

            // Generate plain keys.
            PlainKeys plainKeys = new PlainKeys("",
                                                rootProtocol,
                                                keyGen,
                                                statDist);
            plainKeys.generate(ui.getLog());
            CryptoPKey[] pkeys = plainKeys.getPKeys();
            CryptoSKey skey = plainKeys.getSKey();



            // Extract group over which to execute the protocol.
            String pGroupString = protocolInfo.getStringValue("pgroup");
            PGroup pGroup =
                Marshalizer.unmarshalHexAux_PGroup(pGroupString,
                                                   rootProtocol.randomSource,
                                                   rootProtocol.certainty);

            // Number of ciphertexts shuffled in parallel.
            int width = protocolInfo.getIntValue("width");

            // Generate an "independent" generator.
            IndependentGenerator ig =
                new IndependentGenerator("",
                                         rootProtocol,
                                         pGroup,
                                         pkeys,
                                         skey,
                                         statDist);
            PGroupElement h = ig.generate(ui.getLog());
            ig = null;


            // Construct a source of jointly generated random coins.
            CoinFlipPRingSource coins =
                new CoinFlipPRingSource("",
                                        rootProtocol,
                                        h,
                                        pkeys,
                                        skey,
                                        statDist);


            // Hash function used to implement random oracles.
            String rohString = protocolInfo.getStringValue("rohash");
            Hashfunction roHashfunction;
            if (rohString.equals("SHA-256")) {
                roHashfunction = new HashfunctionHeuristic("SHA-256");
            } else if (rohString.equals("SHA-384")) {
                roHashfunction = new HashfunctionHeuristic("SHA-384");
            } else if (rohString.equals("SHA-512")) {
                roHashfunction = new HashfunctionHeuristic("SHA-512");
            } else {
                roHashfunction =
                    Marshalizer.unmarshalHexAux_Hashfunction(rohString,
                                                    rootProtocol.randomSource,
                                                    rootProtocol.certainty);
            }

            // Export directory for intermediate results and proofs.
            File exportDir = null;

            ElGamalReencShuffle shuffle =
                new ElGamalReencShuffleStandard(pGroup, width);

            // Create key generation protocol.
            DKG dkg = new DKG("",
                              rootProtocol,
                              pkeys,
                              skey,
                              statDist);

            // Construct proofs of shuffles instance.
            IndependentGeneratorsFactory igsFactory = null;
            Challenger challenger = null;
            PoSFactory posFactory = null;
            CCPoSFactory ccposFactory = null;
            VerifiableDecryption verifiableDecryption = null;

            if (protocolInfo.getStringValue("corr").equals("noninteractive")) {


                exportDir = rootProtocol.getFile("roProof");
                exportDir.mkdirs();
                generateREADME(exportDir);


                // Set prefix used in all calls to random oracles.
                byte[] globalPrefix;
                try {

                    File pFile = new File(protocolInfoFilename);
                    byte[] xmlbytes = ExtIO.readString(pFile).getBytes();
                    globalPrefix = roHashfunction.hash(xmlbytes);

                } catch (IOException ioe) {
                    throw new ProtocolError("Unable to read and hash " +
                                            "protocol info file!");
                }

                igsFactory = new IndependentGeneratorsFactory(roHashfunction,
                                                              globalPrefix,
                                                              statDist);

                challenger = new ChallengerRO(roHashfunction, globalPrefix);

                posFactory = new PoSFactory(challenger,
                                            prg,
                                            cbitlenro,
                                            vbitlenro,
                                            statDist);

                ccposFactory = new CCPoSFactory(challenger,
                                                prg,
                                                cbitlenro,
                                                vbitlenro,
                                                statDist);
                verifiableDecryption =
                    new VerifiableDecryption("",
                                             rootProtocol,
                                             dkg,
                                             challenger,
                                             prg,
                                             cbitlenro,
                                             vbitlenro,
                                             statDist);
            } else {

                igsFactory = new IndependentGeneratorsFactory(coins,
                                                              prg,
                                                              cbitlen,
                                                              vbitlen,
                                                              statDist);
                challenger = new ChallengerI(coins);

                posFactory = new PoSFactory(challenger,
                                            prg,
                                            cbitlen,
                                            vbitlen,
                                            statDist);

                ccposFactory = new CCPoSFactory(challenger,
                                                prg,
                                                cbitlen,
                                                vbitlen,
                                                statDist);
                verifiableDecryption =
                    new VerifiableDecryption("",
                                             rootProtocol,
                                             dkg,
                                             challenger,
                                             prg,
                                             cbitlen,
                                             vbitlen,
                                             statDist);
            }

            String interfaceName = protocolInfo.getStringValue("inter");

            MixNetElGamalInterface mixnetInterface =
                    MixNetElGamalInterface.getInterface(interfaceName);

            return new MixNetElGamal("",
                                     rootProtocol,
                                     pGroup,
                                     pkeys,
                                     skey,
                                     statDist,
                                     dkg,
                                     igsFactory,
                                     posFactory,
                                     ccposFactory,
                                     verifiableDecryption,
                                     shuffle,
                                     mixnetInterface,
                                     exportDir);

        } catch (EIOException eioe) {
            throw new ProtocolError(eioe.getMessage(), eioe);
        }
    }



    /**
     * Generates an option instance representing the various ways this
     * protocol an be invoked.
     *
     * @param commandName Name of the command executed by the user to
     * invoke this protocol, i.e., the name of the shell script
     * wrapper.
     * @return Option instance representing how this protocol can be
     * invoked.
     */
    protected static Opt opt(String commandName) {

        String defaultErrorString =
            "Invalid usage form, please use \"" + commandName +
            " -h\" for usage information!";

        Opt opt = new Opt(commandName, defaultErrorString);

        opt.addParameter("protocolInfo", "Protocol info file.");
        opt.addParameter("privateInfo", "Private info file.");
        opt.addParameter("publicKey", "Destination of public key.");
        opt.addParameter("ciphertexts", "Ciphertexts to be mixed.");
        opt.addParameter("ciphertextsout", "Mixed ciphertexts.");
        opt.addParameter("plaintexts", "Resulting plaintexts from mixnet.");

        opt.addOption("-h", "", "Print usage information.");
        opt.addOption("-version", "", "Print the package version and list " +
                      "protocol versions it can handle.");
        opt.addOption("-keygen", "", "Execute joint key generation.");
        opt.addOption("-precomp", "", "Perform joint precomputation.");
        opt.addOption("-mix", "", "Execute mixing phase.");
        opt.addOption("-mixonly", "",
                      "Execute mixing phase except the final decrypting.");
        opt.addOption("-deconly", "",
                      "Decrypt the input ciphertexts without mixing.");
        opt.addOption("-f", "", "Force a reset without query.");
        opt.addOption("-reset", "",
                      "Securely reset to state after key generation.");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        opt.addUsageForm();
        opt.appendToUsageForm(1, "-keygen##privateInfo,protocolInfo,publicKey#");

        opt.addUsageForm();
        opt.appendToUsageForm(2, "-precomp##privateInfo,protocolInfo#");

        opt.addUsageForm();
        opt.appendToUsageForm(3, "-mix##" +
                              "privateInfo,protocolInfo," +
                              "ciphertexts,plaintexts#");

        opt.addUsageForm();
        opt.appendToUsageForm(4, "-reset#-f#privateInfo,protocolInfo#");

        opt.addUsageForm();
        opt.appendToUsageForm(5, "-mixonly##" +
                              "privateInfo,protocolInfo," +
                              "ciphertexts,ciphertextsout#");

        opt.addUsageForm();
        opt.appendToUsageForm(6, "-deconly##" +
                              "privateInfo,protocolInfo," +
                              "ciphertexts,plaintexts#");

        opt.addUsageForm();
        opt.appendToUsageForm(7, "-version###");


        String s =
"Executes the various phases of a mix-net." +
"\n\n" +
"In all commands info file names can be dropped in which case they are " +
"assumed to be \"privInfo.xml\" and \"protInfo.xml\" and exist in the " +
"current working directory." +
"\n\n" +
"Use \"-keygen\" to execute the joint key generation phase of the mix-net. " +
"This results in a joint public key." +
"\n\n" +
"Optionally use \"-precomp\" to execute the pre-computation phase of the " +
"mix-net for the number of ciphertexts specified the protocol info file." +
"\n\n" +
"Use \"-mix\" to execute the mixing phase of the mixnet on ciphertexts " +
"<ciphertexts> and write the output plaintexts to <plaintexts>. If no " +
"precomputation is performed in a separate phase using the \"-precomp\" " +
"option, then the value in the protocol info file must be zero " +
"and precomputation it is performed as part of the mixing phase " +
"for the actual number of ciphertexts." +
"\n\n" +
"Use \"-reset\" to reset the state of the mix-server to its state directly " +
"after key generation. WARNING! All operators must execute this command " +
"if you do, since everything generated after key generation is deleted " +
"permanently." +
"\n\n" +
"Use \"-mixonly\" to only executing the mixing phase of the mix-net, i.e., " +
"no decryption takes place and the output is a list of ciphertexts." +
"\n\n" +
"Use \"-deconly\" to only execute the decryption phase of the mix-net, i.e., " +
"no mixing takes place and the output is a list of plaintexts.";

        opt.appendDescription(s);

        return opt;
    }

    /**
     * Returns true if and only if this implementation can handle the
     * given protocol version.
     *
     * @return True if and only if this implementation can handle the
     * given protocol version.
     */
    public static boolean compatible(String aProtocolVersion) {
        return protocolVersion.equals(aProtocolVersion);
    }

    /**
     * Returns a comma-separated list of protocol versions compatible
     * with this implementation.
     *
     * @return Comma-separated list of protocol versions compatible
     * with this implementation.
     */
    public static String compatibleProtocolVersions() {
        return protocolVersion;
    }

    /**
     * FORK
     */
    public static int deriveWidth(int width, Opt option)
    {
        if (width != 0)
        {
            if (option.valueIsGiven("-width") &&
                !option.getStringValue("-width").equals("" + width))
            {
                throw new ProtocolError("The option \"-width\" can't be used " +
                    "when the protocol width isn't zero");
            }
        } else {
            if (!option.valueIsGiven("-width"))
            {
                throw new ProtocolError("When \"width\" is zero in the " +
                    "protocol info you need to pass the option \"-width\"");
            } else {
                // check that width is a proper int value, an integer
                try {
                    width = option.getIntValue("-width");
                } catch (OptError oe) {
                    throw new ProtocolError("The option \"-width\" isn't an int value");
                }
            }
        }

        if (MAX_WIDTH < width || width <= 0) {
            throw new ProtocolError("Width MUST be not only a positive " +
                "number, but also inferior to " + MAX_WIDTH + ", but value " +
                width + " was given instead");
        }
        return width;
    }

    /**
     * Allows a user to invoke this protocol from the command line.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {

        String e;

        if (args.length == 0) {
            System.err.println("Missing command name!");
        }
        String commandName = args[0];

        args = Arrays.copyOfRange(args, 1, args.length);

        Opt opt = opt(commandName);
        try {
            opt.parse(args);
        } catch (OptException oe) {

            // If parsing fails, then we assume that the user is
            // executing the commands "in the working directory" and
            // make another attempt with the default file names.

            String privInfoFileName = "privInfo.xml";
            String protInfoFileName = "protInfo.xml";

            boolean patch = true;
            String[] newargs = new String[0];

            if (args.length > 0
                && (args[0].equals("-keygen")
                    || args[0].equals("-mix")
                    || args[0].equals("-precomp")
                    || args[0].equals("-reset")
                    || args[0].equals("-mixonly")
                    || args[0].equals("-deconly"))) {

                newargs = new String[args.length + 2];
                newargs[0] = args[0];
                newargs[1] = privInfoFileName;
                newargs[2] = protInfoFileName;

                if (args.length > 1) {
                    System.arraycopy(args, 1, newargs, 3, args.length - 1);
                }

                opt = opt(commandName);
                try {
                    opt.parse(newargs);
                } catch (OptException oe2) {
                    patch = false;
                }
            } else {
                patch = false;
            }

            if (!patch) {
                e = "\n" + "ERROR: " + oe.getMessage() + "\n";
                System.err.println(e);
                System.exit(0);
            }
        }

        if (opt.getBooleanValue("-h")) {

            System.out.println(opt.usage());
            System.exit(0);

        } else if (opt.getBooleanValue("-version")) {

            System.out.println(Version.packageVersion + " " +
                               "(" + compatibleProtocolVersions() + ")");
            System.exit(0);

        } else {

            try {

                String privateInfoFilename =
                    opt.getStringValue("privateInfo");
                String protocolInfoFilename =
                    opt.getStringValue("protocolInfo");

                // Simple textual interface.
                UI ui = new TextualUI(new TConsole());
                ui.getLog().addLogStream(System.err);

                // Read private info and protocol info.
                InfoGenerator generator = new MixNetElGamalGen();
                PrivateInfo privateInfo = generator.newPrivateInfo();
                ProtocolInfo protocolInfo = generator.newProtocolInfo();
                try {
                    privateInfo.parse(privateInfoFilename);
                    protocolInfo.parse(protocolInfoFilename);
                } catch (InfoException ie) {
                    throw new ProtocolError("Failed to parse info files!", ie);
                }

                // Check that this package can handle the protocol
                // version.
                String piVersion =
                    protocolInfo.getStringValue(ProtocolInfo.VERSION);
                if (!compatible(piVersion)) {
                    throw new ProtocolError("Protocol info file requires " +
                                            "incompatible protocol version " +
                                            piVersion + "!");
                }
                String priVersion =
                    privateInfo.getStringValue(PrivateInfo.VERSION);
                if (!compatible(priVersion)) {
                    throw new ProtocolError("Private info file requires " +
                                            "incompatible protocol version " +
                                            priVersion + "!");
                }


                // Create root protocol.
                Protocol rootProtocol =
                    new Protocol(privateInfo, protocolInfo, ui);

                SimpleTimer simpleTimer = new SimpleTimer();

                // Invoke one of the phases of the mixnet.
                if (opt.getBooleanValue("-keygen")) {

                    if (rootProtocol.readBoolean(".keygen")) {
                        e = "The option \"-keygen\" can only be used once!";
                        throw new ProtocolError(e);
                    }
                    rootProtocol.writeBoolean(".keygen");

                    // Put headers in log.
                    rootProtocol.licenseLogEntry();
                    initLogEntry(ui.getLog(), rootProtocol.j);

                    MixNetElGamal mixnet =
                        newMixNetElGamal(rootProtocol,
                                         privateInfo,
                                         protocolInfo,
                                         protocolInfoFilename,
                                         ui);

                    PGroupElement fullPublicKey =
                        mixnet.keyGenerationPhase(ui.getLog());

                    ui.getLog().info("Joint key generation completed after "
                                     + simpleTimer + ".");

                    // FORK
                    MixNetElGamalInterface mixnetInterface =
                        MixNetElGamalInterface.getInterface("raw");

                    File publicKeyFile =
                        new File(opt.getStringValue("publicKey"));

                    mixnetInterface.writePublicKey(fullPublicKey,
                                                   publicKeyFile);

                    mixnet.shutdown("keygen", ui.getLog());

                    // Make snapshot to allow reset.
                    mixnet.snapShot(0);

                } else if (opt.getBooleanValue("-precomp")) {

                    if (!rootProtocol.readBoolean(".keygen")) {
                        e = "The option \"-keygen\" must be used before " +
                            "\"-precomp\"!";
                        throw new ProtocolError(e);
                    }
                    if (rootProtocol.readBoolean(".precomp")) {
                            e = "The option \"-precomp\" can only " +
                                "be used once! Use the \"-reset\" option " +
                                "if you really need to run it again!";
                            throw new ProtocolError(e);
                    }

                    // Figure out for how many ciphertexts we should
                    // perform precomputation.
                    int maximalNoCiphertexts =
                        protocolInfo.getIntValue("maxciph");
                    if (maximalNoCiphertexts <= 0) {
                        e = "Precomputation for zero number of ciphertexts " +
                            "is illegal. Please consult your protocol info " +
                            "file and increase the \"maxciph\"-value!";
                        throw new ProtocolError(e);
                    }
                    rootProtocol.writeBoolean(".precomp");
                    initLogEntry(ui.getLog(), rootProtocol.j);

                    MixNetElGamal mixnet =
                        newMixNetElGamal(rootProtocol,
                                         privateInfo,
                                         protocolInfo,
                                         protocolInfoFilename,
                                         ui);
                    mixnet.keyGenerationPhase(ui.getLog());

                    mixnet.precomputationPhase(ui.getLog(),
                                               true,
                                               maximalNoCiphertexts);

                    ui.getLog().info("Pre-computation phase completed after "
                                          + simpleTimer + ".");

                    mixnet.shutdown("precomp", ui.getLog());

                } else if (opt.getBooleanValue("-mix")
                           || opt.getBooleanValue("-mixonly")
                           || opt.getBooleanValue("-deconly")) {

                    if (!rootProtocol.readBoolean(".keygen")) {

                        String com = "-mix";
                        if (opt.getBooleanValue("-mixonly")) {
                            com = "-mixonly";
                        } else if (opt.getBooleanValue("-deconly")) {
                            com = "-deconly";
                        }
                        e = "The option \"-keygen\" must be used before \"" +
                            com + "\"!";
                        throw new ProtocolError(e);
                    }

                    if (rootProtocol.readBoolean(".dec")) {

                        e = "You can not decrypt or mix after decrypting " +
                            "without resetting first! " +
                            "Please use the \"-reset\" option to reset " +
                            "the mix-server before attempting again.";
                        throw new ProtocolError(e);
                    }

                    // Make sure we never re-use any pre-computed
                    // values.
                    if ((opt.getBooleanValue("-mix")
                         || opt.getBooleanValue("-mixonly"))
                        && rootProtocol.readBoolean(".mix")) {

                        e = "You can not mix twice without resetting! " +
                            "Please use the \"-reset\" option to reset " +
                            "the mix-servers before attempting to mix " +
                            "again.";
                        throw new ProtocolError(e);
                    }

                    initLogEntry(ui.getLog(), rootProtocol.j);
                    MixNetElGamal mixnet =
                        newMixNetElGamal(rootProtocol,
                                         privateInfo,
                                         protocolInfo,
                                         protocolInfoFilename,
                                         ui);
                    mixnet.keyGenerationPhase(ui.getLog());

                    // FORK
                    MixNetElGamalInterface mixnetInterface =
                        MixNetElGamalInterface.getInterface("raw");

                    // Import ciphertexts.
                    ui.getLog().info("Importing ElGamal ciphertexts.");
                    File ciphFile = new File(opt.getStringValue("ciphertexts"));

                    PGroupElementArray ciphertexts = null;
                    try {
                        ciphertexts = mixnetInterface.
                            readCiphertexts(mixnet.getCiphPGroup(), ciphFile);

                    } catch (ProtocolFormatException pfe) {
                        throw new ProtocolError(pfe.getMessage(), pfe);
                    }

                    // Make sure that all honest parties start with
                    // identical lists of ciphertexts.

                    // ciphertexts = mixnet.identifyCiphertexts(ui.getLog(),
                    //                                          ciphertexts);

                    if (ciphertexts.size() == 0) {
                        throw new ProtocolError("No valid ciphertexts were " +
                                                "found!");
                    }

                    PGroupElementArray reencryptedCiphertexts;
                    if (opt.getBooleanValue("-mix")
                        || opt.getBooleanValue("-mixonly")) {

                        // Figure out for how many ciphertexts
                        // precomputation was performed.
                        int maximalNoCiphertexts =
                            protocolInfo.getIntValue("maxciph");
                        if (maximalNoCiphertexts == 0) {

                            // No precomputation was performed, so we do
                            // it now for the actual number of ciphertexts.
                            maximalNoCiphertexts = ciphertexts.size();

                        } else if (maximalNoCiphertexts < ciphertexts.size()) {

                            e = "Precomputation was performed for " +
                                maximalNoCiphertexts + " ciphertexts, but " +
                                "there are " + ciphertexts.size() +
                                " ciphertexts to process. Please use the " +
                                "\"-reset\" option to reset the mix-server. " +
                                "Then set the \"maxciph\" value in the " +
                                "protocol info file to zero and execute the " +
                                "mixing phase directly using the \"-mix\" " +
                                "option.";

                            throw new ProtocolError(e);

                        }

                        mixnet.precomputationPhase(ui.getLog(),
                                                   false,
                                                   maximalNoCiphertexts);

                        // Shrink precomputed objects to fit the
                        // actual number of ciphertexts.
                        mixnet.shrinkToActualNoCiphertexts(ui.getLog(),
                                                           ciphertexts.size());

                        // Perform reencryption.
                        reencryptedCiphertexts =
                            mixnet.mixingPhase(ui.getLog(), ciphertexts);

                        rootProtocol.writeBoolean(".mix");

                        if (opt.getBooleanValue("-mixonly")) {
                            File exportedCiphertexts =
                                new File(opt.getStringValue("ciphertextsout"));
                            mixnetInterface.
                                writeCiphertexts(reencryptedCiphertexts,
                                                 exportedCiphertexts);

                            reencryptedCiphertexts.free();

                            ui.getLog().info("Mixing (only) phase completed " +
                                             "after " + simpleTimer + ".");
                        }

                        mixnet.precomputationPhaseFree();

                    } else {

                        reencryptedCiphertexts = ciphertexts;
                    }

                    if (opt.getBooleanValue("-mix")
                        || opt.getBooleanValue("-deconly")) {

                        // Perform decryption
                        PGroupElementArray plaintextsArray =
                            mixnet.decryptionPhase(ui.getLog(),
                                                   reencryptedCiphertexts);
                        reencryptedCiphertexts.free();

                        File exportedPlaintexts =
                            new File(opt.getStringValue("plaintexts"));
                        mixnetInterface.decodePlaintexts(plaintextsArray,
                                                         exportedPlaintexts);

                        plaintextsArray.free();

                        rootProtocol.writeBoolean(".dec");

                        if (opt.getBooleanValue("-mix")) {
                            ui.getLog().info("Mixing phase completed after "
                                             + simpleTimer + ".");
                        } else {
                            ui.getLog().info("Decryption phase completed after "
                                             + simpleTimer + ".");
                        }
                    }

                    mixnet.shutdown("mix", ui.getLog());

                } else if (opt.getBooleanValue("-reset")) {

                    if (!rootProtocol.readBoolean(".keygen")) {
                        e = "The option \"-keygen\" must be used before " +
                            "\"-reset\"!";
                        throw new ProtocolError(e);
                    }

                    if (opt.getBooleanValue("-f")
                        || ui.dialogQuery("WARNING! " +
                                          "Make sure that you do not have " +
                                          "any of your own files in the " +
                                          "working directory of the mix-net, " +
                                          "in particular your info files. " +
                                          "Do not forget to copy the " +
                                          "Fiat-Shamir proofs before you " +
                                          "execute this command. " +
                                          "Resetting to the state " +
                                          "after key generation completed " +
                                          "can not be undone later. Are you " +
                                          "sure that you want to perform a " +
                                          "reset?")) {
                        rootProtocol.resetToSnapShot(0);
                    }
                }

            } catch (ProtocolError pe) {
                e = "\n" + "ERROR: " + pe.getMessage() + "\n";
                System.err.println(e);
                //pe.printStackTrace(System.err);
                System.exit(0);
            }
        }
    }
}
