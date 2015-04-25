
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

package verificatum.protocol.mixnet;

import java.io.*;
import java.text.*;
import java.util.*;

import verificatum.*;
import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.ui.*;
import verificatum.ui.info.*;
import verificatum.ui.gen.*;
import verificatum.ui.opt.*;
import verificatum.util.*;
import verificatum.protocol.*;
import verificatum.protocol.coinflip.*;
import verificatum.protocol.distrkeygen.*;
import verificatum.protocol.hvzk.*;

/**
 * Standalone verifier of a so called "universally verifiable"
 * heuristically sound proof of correctness of an execution of {@link
 * MixNetElGamal}.
 *
 * @author Douglas Wikstrom
 */
public class MixNetElGamalVerifyRO {

    /**
     * Protocol version implemented by this mix-net.
     */
    public static final String protocolVersion = "0.1";

    /**
     * Option instance.
     */
    Opt opt;

    /**
     * Protocol info for the protocol execution that is verified.
     */
    ProtocolInfo protocolInfo;

    /**
     * Amount of information printed during verification.
     */
    boolean verbose;

    /**
     * Certainty with which a modulus is deemed prime.
     */
    int certainty;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    int statDist;

    /**
     * Number of bits in the challenge.
     */
    int challengeBitLength;

    /**
     * Number of bits used during batching.
     */
    int batchBitLength;

    /**
     * Maximal number of ciphertexts that this mix-net can accept.
     */
    int maximalNoCiphertexts;

    /**
     * Number of parties executing the protocol.
     */
    int k;

    /**
     * Number of parties needed to violate privacy.
     */
    int threshold;

    /**
     * Group in which the protocol was executed.
     */
    PGroup pGroup;

    /**
     * PRG used to derive random vectors during batching.
     */
    PRG prg;

    /**
     * Source of random challenges.
     */
    ChallengerRO challenger;

    /**
     * Directory containing random oracle proofs.
     */
    File roProofDir;

    /**
     * Source of randomness.
     */
    RandomSource randomSource;

    /**
     * Name of the interface of the mix-net.
     */
    protected String interfaceName;

    /**
     * Interface of the mix-net.
     */
    protected MixNetElGamalInterface mixnetInterface;

    /**
     * Shuffle.
     */
    protected ElGamalReencShuffle shuffle;

    /**
     * Working directory of the verifier.
     */
    protected static File tmpDir;

    /**
     * Name of protocol info file.
     */
    protected String protocolInfoFilename;

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
     * Creates an instance of the external verifier of a mix-net
     * execution.
     *
     * @param opt Instance containing options.
     * @param randomSource Source of randomness.
     */
    public MixNetElGamalVerifyRO(Opt opt, RandomSource randomSource) {
	this.opt = opt;
	this.randomSource = randomSource;
	this.verbose = opt.getBooleanValue("-v");

	protocolInfoFilename = opt.getStringValue("protocolInfo");

	// Read private info and protocol info.
	InfoGenerator generator = new MixNetElGamalGen();
	this.protocolInfo = generator.newProtocolInfo();
	try {
	    protocolInfo.parse(protocolInfoFilename);
	} catch (InfoException ie) {
	    failStop("Failed to parse protocol info files!" +
		     ie.getMessage(), ie);
	}

        // Check that the protocol info file was generated for this
        // version of the package.
        String piVersion =
            protocolInfo.getStringValue(ProtocolInfo.VERSION);
        if (!compatible(piVersion)) {
            failStop("Protocol info file requires protocol version " +
                     piVersion + "!");
        }
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
     * Name of file containing the decrypted plaintext elements.
     *
     * @param exportDir Export directory for universal verifiability.
     * @return File where the output plaintext elements are stored.
     */
    protected static File Pfile(File exportDir) {
        return new File(exportDir, String.format("PlaintextElements.bt"));
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
     * Name of file containing permutation commitment.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index index of mix-server.
     * @return File where permutation commitments are stored.
     */
    protected static File PCfile(File exportDir, int index) {
        return new File(exportDir,
                        String.format("PermutationCommitment%02d.bt", index));
    }

    /**
     * Name of file containing list used to shrink precomputed values
     * to the actual size.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index Index of mix-server.
     * @return File where shrinking list is stored.
     */
    protected static File KLfile(File exportDir, int index) {
        return new File(exportDir, String.format("KeepList%02d.bt", index));
    }

    /**
     * Name of file containing commitment of proof of shuffle.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index index of mix-server.
     * @return File where permutation commitments are stored.
     */
    protected static File PoSCfile(File exportDir, int index) {
        return new File(exportDir,
                        String.format("PoSCommitment%02d.bt", index));
    }

    /**
     * Name of file containing reply of proof of shuffle.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index index of mix-server.
     * @return File where permutation commitments are stored.
     */
    protected static File PoSRfile(File exportDir, int index) {
        return new File(exportDir,
                        String.format("PoSReply%02d.bt", index));
    }

    /**
     * Name of file containing commitment of proof of shuffle.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index index of mix-server.
     * @return File where permutation commitments are stored.
     */
    protected static File CCPoSCfile(File exportDir, int index) {
        return new File(exportDir,
                        String.format("CCPoSCommitment%02d.bt", index));
    }

    /**
     * Name of file containing reply of proof of shuffle.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index index of mix-server.
     * @return File where permutation commitments are stored.
     */
    protected static File CCPoSRfile(File exportDir, int index) {
        return new File(exportDir,
                        String.format("CCPoSReply%02d.bt", index));
    }

    /**
     * Name of file containing decryption factors.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index index of mix-server.
     * @return File where decryption factors are stored.
     */
    protected static File DFfile(File exportDir, int index) {
        return new File(exportDir,
                        String.format("DecryptionFactors%02d.bt", index));
    }

    /**
     * Name of file containing the secret El Gamal key.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index index of mix-server.
     * @return File where the secret key is stored.
     */
    protected static File SKfile(File exportDir, int index) {
        return new File(exportDir,
                        String.format("SecretKey%02d.bt", index));
    }

    /**
     * Name of file containing the commitment of a proof.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index index of mix-server.
     * @return File where the secret key is stored.
     */
    protected static File DFCfile(File exportDir, int index) {
        return new File(exportDir,
                        String.format("DecrFactCommitment%02d.bt",
                                      index));
    }

    /**
     * Name of file containing the reply of a proof.
     *
     * @param exportDir Export directory for universal verifiability.
     * @param index index of mix-server.
     * @return File where the secret key is stored.
     */
    protected static File DFRfile(File exportDir, int index) {
        return new File(exportDir,
                        String.format("DecrFactReply%02d.bt", index));
    }

    /**
     * Verify the execution of the protocol.
     */
    public void verify(boolean verifyExternal) {


        MixNetElGamalInterface rawInterface =
            MixNetElGamalInterface.getInterface("raw");

	try {

	    // Extract additional security parameters.
	    certainty = 100;
	    statDist = protocolInfo.getIntValue("statdist");
	    challengeBitLength = protocolInfo.getIntValue("cbitlenro");
	    batchBitLength = protocolInfo.getIntValue("vbitlenro");
	    maximalNoCiphertexts = protocolInfo.getIntValue("maxciph");


	    // Number of parties.
	    k = protocolInfo.getNumberOfParties();
	    threshold = protocolInfo.getIntValue(ProtocolInfo.THRESHOLD);

	    Hashfunction roHashfunction = null;
	    try {

		// Extract group over which to execute the protocol.
		String pGroupString = protocolInfo.getStringValue("pgroup");
		pGroup = Marshalizer.unmarshalHexAux_PGroup(pGroupString,
                                                            randomSource,
                                                            certainty);

		// Extract PRG used to derive random vectors.
		String prgString = protocolInfo.getStringValue("prg");
                if (prgString.equals("SHA-256")) {
                    prg =
                        new PRGHeuristic(new HashfunctionHeuristic("SHA-256"));
                } else if (prgString.equals("SHA-384")) {
                    prg =
                        new PRGHeuristic(new HashfunctionHeuristic("SHA-384"));
                } else if (prgString.equals("SHA-512")) {
                    prg =
                        new PRGHeuristic(new HashfunctionHeuristic("SHA-512"));
                } else {
                    prg = Marshalizer.unmarshalHexAux_PRG(prgString,
                                                          randomSource,
                                                          certainty);
                }


		// Hash function used to implement random oracles.
		String rohString = protocolInfo.getStringValue("rohash");
                if (rohString.equals("SHA-256")) {
                    roHashfunction = new HashfunctionHeuristic("SHA-256");
                } else if (rohString.equals("SHA-384")) {
                    roHashfunction = new HashfunctionHeuristic("SHA-384");
                } else if (rohString.equals("SHA-512")) {
                    roHashfunction = new HashfunctionHeuristic("SHA-512");
                } else {
                    roHashfunction =
                        Marshalizer.unmarshalHexAux_Hashfunction(rohString,
                                                                 randomSource,
                                                                 certainty);
                }

                // Mix-net interface.
                interfaceName = protocolInfo.getStringValue("inter");

                mixnetInterface =
                    MixNetElGamalInterface.getInterface(interfaceName);

	    } catch (EIOException eioe) {
		failStop("Unable to read group, prg, or hashfunction used as " +
			 "random oracle from file.");
	    }

            File pFile = new File(protocolInfoFilename);
            byte[] xmlbytes = ExtIO.readString(pFile).getBytes();
            byte[] globalPrefix = roHashfunction.hash(xmlbytes);

            challenger = new ChallengerRO(roHashfunction, globalPrefix);

            // Number of ciphertexts shuffled in parallel.
            int width = protocolInfo.getIntValue("width");

            // Create shuffle type.
            shuffle = new ElGamalReencShuffleStandard(pGroup, width);

	    // Directory holding RO-proofs.
	    String roProofDirname = opt.getStringValue("roProof");
	    roProofDir = new File(roProofDirname);

	    printHeader("Read and derive common values.");

	    // Read joint public key.
	    print("Read joint public key... ");
	    PGroupElement fullElGamalPKey =
                readFullElGamalPKey(rawInterface, FPKfile(roProofDir));
	    println("done.");

            // Verify joint public key.
            BiPRingPGroup biKey = shuffle.getBiKey();
            PGroupElement basicPublicKey =
                ((PPGroupElement)fullElGamalPKey).project(0);
            if (!basicPublicKey.equals(biKey.getPGroupDomain().getg())) {
                failStop("Full joint ElGamal key is flawed!");
            }
            HomPRingPGroup homKey = biKey.restrict(basicPublicKey);


	    // Read individual public keys.
	    print("Read individual El Gamal public keys... ");
	    PGroupElement[] elGamalPKeys = readElGamalPKeys(fullElGamalPKey);
	    println("done.");


            // Verify that the product of the individual public keys
            // matches the full public key.
	    print("Verify relation between public keys... ");
            PGroupElement agg = elGamalPKeys[1];
            for (int l = 2; l <= k; l++) {
                agg = agg.mul(elGamalPKeys[l]);
            }
            if (!((PPGroupElement)fullElGamalPKey).project(1).equals(agg)) {
                failStop("Mismatching public keys!");
            }
	    println("done.");


	    // Read ciphertexts.
	    print("Read input ciphertexts... ");
	    PGroupElementArray ciphertexts =
                readCiphertexts(rawInterface, shuffle, Lfile(roProofDir, 0));
	    println("done.");

            // If the mix-net was executed without precomputation we
            // use the actual number of ciphertexts.
            if (maximalNoCiphertexts == 0) {
                maximalNoCiphertexts = ciphertexts.size();
            }


	    // Derive independent generators.
	    print("Derive independent generators... ");
	    IndependentGeneratorsRO igRO =
	        new IndependentGeneratorsRO("generators",
                                            roHashfunction,
                                            globalPrefix,
                                            statDist);
	    PGroupElementArray generators =
                igRO.generate(null,
                              pGroup,
                              maximalNoCiphertexts);

            PGroupElementArray shrunkGenerators =
                generators.copyOfRange(0, ciphertexts.size());
	    println("done.");

            PGroupElementArray input = ciphertexts;

            // Verify the shuffles.
            for (int l = 1; l <= threshold; l++) {

                printHeader("Verify shuffle of Party " + l + ".");

                // Read permutation commitment
                print("Read permutation commitment... ");
                PGroupElementArray permutationCommitment =
                    readPermutationCommitment(generators.getPGroup(),
                                              l, generators.size());
                println("done.");

                // Verify proof of a shuffle for permutation commitment.
                print("Verify proof of shuffle of commitments... ");
                if (verifyPoS(l, generators, permutationCommitment)) {
                    println("done.");
                } else {
                    println("failed.");
                    failInfo("Setting permutation commitment to list of " +
                             "generators.");
                    permutationCommitment.free();
                    permutationCommitment =
                        generators.copyOfRange(0, generators.size());
                }


                // Shrink commitment.
                print("Shrink permutation commitment... ");
                PGroupElementArray shrunkPermutationCommitment =
                    shrinkPermutationCommitment(l,
                                                permutationCommitment,
                                                shrunkGenerators.size());
                permutationCommitment.free();
                println("done.");


                // Read output list.
                print("Read output of Party " + l + "... ");
                PGroupElementArray output =
                    readOutputList(shuffle, l, input.size());
                println("done.");


                // Verify commitment consistent proof of shuffle.
                print("Verify commitment-consistent proof of shuffle... ");
                if (verifyCCPoS(l,
                                shrunkGenerators,
                                shrunkPermutationCommitment,
                                fullElGamalPKey,
                                input, output)) {
                    println("done.");
                } else {
                    println("failed.");
                    failInfo("Replacing output of Party " + l +
                             " by its input.");
                    PGroupElementArray tmp = output;
                    output = input.copyOfRange(0, input.size());
                    tmp.free();
                }

                // Free resources.
                PGroupElementArray tmp = input;
                input = output;

                if (l > 1) {
                    tmp.free();
                }
                shrunkPermutationCommitment.free();

            }

            // Free resources.
            generators.free();
            shrunkGenerators.free();


            // Verify joint decryption.

            // Fetch decryption algorithm.
            BiKeyedArrayMap decryptor = shuffle.getPartDecryptor(input.size());

            // Construct wrapper for ciphertexts.
            APGroup aPGroup =
                (APGroup)((PPGroup)decryptor.getPGroupDomain()).project(1);
            APGroupElement inputAsElement = aPGroup.toElement(input);

            // Combine to a single element.
            PGroupElement groupElement =
                ((PPGroup)decryptor.getPGroupDomain()).
                product(basicPublicKey, inputAsElement);

            // Form homomorphism by restricting the bilinear map.
            HomPRingPGroup hom = decryptor.restrict(groupElement);

            PGroupElement factorElement = null;

            for (int l = 1; l <= k; l++) {

                printHeader("Verify decryption of Party " + l + ".");

                PGroupElement tmpFactorElement = null;

                File skFile = SKfile(roProofDir, l);

                if (skFile.exists()) {

                    print("Read key recovered during mixing... ");
                    ByteTreeReader btr = (new ByteTreeF(SKfile(roProofDir, l))).
                        getByteTreeReader();
                    PRingElement sk = homKey.getDomain().toElement(btr);
                    btr.close();
                    println("done.");

                    print("Verify correctness of secret key... ");
                    if (!homKey.map(sk).equals(elGamalPKeys[l])) {
                        failStop("Secret key of Party " + l +
                                 " is malformed!");
                    }
                    println("done.");

                    print("Compute decryption explicitly... ");
                    tmpFactorElement = hom.map(sk);
                    println("done.");

                } else {

                    // Read decryption factor.
                    print("Read decryption factors... ");
                    ByteTreeReader btr = (new ByteTreeF(DFfile(roProofDir, l))).
                        getByteTreeReader();
                    tmpFactorElement = hom.getRange().toElement(btr);
                    btr.close();
                    println("done.");

                    // Verify that the secret key corresponding to the
                    // public key was used to decrypt.
                    print("Verify decryption proof... ");
                    if (!((PPGroupElement)tmpFactorElement).project(0).
                        equals(elGamalPKeys[l])
                        || !verifyDecryption(l,
                                             hom,
                                             groupElement,
                                             tmpFactorElement)) {
                        failStop("Decryption proof is malformed!");
                    }
                    println("done.");
                }

                if (factorElement == null) {

                    factorElement = tmpFactorElement;

                } else {

                    PGroupElement tmp = factorElement;
                    factorElement = factorElement.mul(tmpFactorElement);
                    tmp.free();
                    tmpFactorElement.free();

                }
            }

            printHeader("Verify finalization.");

            // Pack factors and ciphertexts to be input to finalizer.
            PGroupElement factors = ((PPGroupElement)factorElement).project(1);

            PPGroup pPGroup = new PPGroup(factors.getPGroup(),
                                          inputAsElement.getPGroup());

            // Finalize decryption.
            print("Decrypt using verified decryption factors... ");
            HomPGroupPGroup finalizer = shuffle.getFinalizer();
            PGroupElement res =
                finalizer.map(pPGroup.product(factors, inputAsElement));
            factors.free();
            println("done.");

            // Extract plaintext group elements.
            PGroupElementArray computedPlaintextElements =
                ((APGroupElement)res).getContent();

            print("Read and verify plaintexts... ");
            PGroupElementArray plaintextElements =
                readArray(Pfile(roProofDir),
                          computedPlaintextElements.getPGroup(),
                          computedPlaintextElements.size());

            if (!plaintextElements.equals(computedPlaintextElements)) {
                failStop("Plaintexts are incorrect!");
            }
            println("done.");

            computedPlaintextElements.free();

            if (verifyExternal) {

                printHeader("Verify correspondence with external values.");

                // Read and verify external joint public key.
                print("Read and verify external joint public key... ");
                File pkeyFile = new File(opt.getStringValue("pkey"));
                PGroupElement extFullElGamalPKey =
                    readFullElGamalPKey(mixnetInterface, pkeyFile);

                if (!extFullElGamalPKey.equals(fullElGamalPKey)) {
                    failStop("External joint public key does not match " +
                             "internal joint public key!");
                }
                println("done.");

                print("Read and verify external ciphertexts... ");
                File ciphFile = new File(opt.getStringValue("ciphertexts"));

                PGroupElementArray extCiphertexts =
                    readCiphertexts(mixnetInterface, shuffle, ciphFile);

                if (!extCiphertexts.equals(ciphertexts)) {
                    failStop("External ciphertexts do not match the " +
                             "internal ciphertexts!");
                }
                println("done.");
                extCiphertexts.free();


                // Verify that plaintext elements match internal
                // plaintext elements.

                // Decode plaintext elements.
                File plaintextsFile = TempFile.getFile();
                mixnetInterface.decodePlaintexts(plaintextElements,
                                                 plaintextsFile);

                // Compare output to claimed output.
                print("Read and verify external plaintexts... ");
                File claimedPlaintextsFile =
                    new File(opt.getStringValue("plaintexts"));
                if (!ExtIO.equals(plaintextsFile, claimedPlaintextsFile)) {
                    failStop("Verified output plaintexts do not match " +
                             "claimed plaintexts!");
                }
                plaintextsFile.delete();
                println("done.");
                println("");

            } else {
                println("");
            }

            ciphertexts.free();
            plaintextElements.free();

	} catch (Throwable e) {
	    failStop("Fatal error!", e);
	}
    }

    /**
     * Read full El Gamal public key.
     *
     * @return Full El Gamal public key.
     */
    PGroupElement readFullElGamalPKey(MixNetElGamalInterface mixnetInterface,
                                      File file) {
        try {

            return mixnetInterface.readPublicKey(file, randomSource, certainty);

        } catch (ProtocolFormatException pfe) {
            failStop("Could not read full El Gamal public key from file!", pfe);
            return null; // Compiler complains.
        }
    }

    /**
     * Read El Gamal public keys of the mix-servers.
     *
     * @param fullElGamalPKey Full El Gamal public key.
     * @return El Gamal public keys.
     */
    PGroupElement[] readElGamalPKeys(PGroupElement fullElGamalPKey) {

        PGroup fullPublicKeyPGroup = fullElGamalPKey.getPGroup();
        PGroup publicKeyPGroup =
            ((PPGroup)fullPublicKeyPGroup).project(0);

        PGroupElement[] elGamalPKeys = new PGroupElement[k + 1];

        for (int l = 1; l <= k; l++) {

            ByteTreeReader btr = null;
            try {

        	ByteTreeBasic bt = new ByteTreeF(PKfile(roProofDir, l));
                btr = bt.getByteTreeReader();
        	elGamalPKeys[l] = publicKeyPGroup.toElement(btr);

            } catch (ArithmFormatException afe) {
        	failStop("Unable to read public key of Party " + l + "!", afe);
            } finally {
                btr.close();
            }
        }
        return elGamalPKeys;
    }

    /**
     * Read original El Gamal ciphertexts.
     *
     * @param mixnetInterface Mix-net interface.
     * @param shuffle Underlying shuffle.
     * @param ciphFile File containing ciphertexts.
     * @return Original El Gamal ciphertexts.
     */
    PGroupElementArray readCiphertexts(MixNetElGamalInterface mixnetInterface,
                                       ElGamalReencShuffle shuffle,
                                       File ciphFile) {

        PGroup ciphPGroup = shuffle.getEncryptor(1).getArrayRange();

        try {

            return mixnetInterface.readCiphertexts(ciphPGroup, ciphFile);

        } catch (ProtocolFormatException pfe) {
            failStop("Unable to read ciphertexts!", pfe);
            return null;
        }
    }

    PGroupElementArray readArray(File arrayFile, PGroup pGroup, int size) {
        try {

            ByteTreeBasic bt = new ByteTreeF(arrayFile);
            ByteTreeReader btr = bt.getByteTreeReader();
            PGroupElementArray array = pGroup.toElementArray(size, btr);
            btr.close();
            return array;

        } catch (ArithmFormatException afe) {
            failStop("Unable to read array " + arrayFile.getName() + "!", afe);
            return null; // Compiler complains.
        }
    }

    PGroupElementArray readDecryptionFactors(PGroupElement fullElGamalPKey,
                                             int l, int size) {
        return readArray(DFfile(roProofDir, l), fullElGamalPKey.getPGroup(),
                         size);
    }

    PGroupElementArray readPermutationCommitment(PGroup pGroup,
                                                 int l, int size) {
        return readArray(PCfile(roProofDir, l), pGroup, size);
    }

    PGroupElementArray readOutputList(ElGamalReencShuffle shuffle,
                                      int l, int size) {
        return readArray(Lfile(roProofDir, l),
                         shuffle.getEncryptor(1).getArrayRange(),
                         size);
    }

    boolean verifyPoS(int l, PGroupElementArray generators,
                      PGroupElementArray permutationCommitment) {

        // Initialize proof.
        PoSBasicTW V = new PoSBasicTW(challengeBitLength,
                                      batchBitLength,
                                      statDist,
                                      prg);
        PGroupElement g = generators.getPGroup().getg();
        V.setInstance(g, generators, permutationCommitment);

        // Generate and set batching vector.
        ByteTreeContainer challengeData =
            new ByteTreeContainer(g.toByteTree(),
                                  generators.toByteTree(),
                                  permutationCommitment.toByteTree());
        byte[] prgSeed = challenger.challenge(challengeData,
                                              8 * prg.minNoSeedBytes(),
                                              statDist);
        V.setBatchVector(prgSeed);

        // Read commitment.
        ByteTreeReader commitmentReader =
            (new ByteTreeF(PoSCfile(roProofDir, l))).getByteTreeReader();
        ByteTreeBasic commitment = V.setCommitment(commitmentReader);
        commitmentReader.close();


        // Generate a challenge.
        challengeData = new ByteTreeContainer(new ByteTree(prgSeed),
                                              commitment);
        byte[] challengeBytes = challenger.challenge(challengeData,
                                                     challengeBitLength,
                                                     statDist);
        LargeInteger integerChallenge = LargeInteger.toPositive(challengeBytes);

        // Set the commitment and challenge.
        V.setChallenge(integerChallenge);


        // Read and verify reply.
        ByteTreeReader replyReader =
            (new ByteTreeF(PoSRfile(roProofDir, l))).getByteTreeReader();

        boolean verdict = V.verify(replyReader);
        replyReader.close();

        V.free();

        return verdict;
    }

    PGroupElementArray
        shrinkPermutationCommitment(int l,
                                    PGroupElementArray permComm,
                                    int newSize) {
        try {

            ByteTreeReader btr =
                (new ByteTreeF(KLfile(roProofDir, l))).getByteTreeReader();
            boolean[] keepList = btr.readBooleans(permComm.size());
            btr.close();

            int total = 0;
            for (int i = 0; i < keepList.length; i++) {
                if (keepList[i]) {
                    total++;
                }
            }
            if (total != newSize) {
                failStop("Wrong number of true elements in keep list of " +
                         "Party " + l + "!");
                return null;
            }

            return permComm.extract(keepList);

        } catch (EIOException eioe) {

            failStop("Unable to open keeplist of Party " + l + "!");
            return null;
        }
    }

    boolean verifyCCPoS(int l,
                        PGroupElementArray generators,
                        PGroupElementArray permutationCommitment,
                        PGroupElement fullElGamalPKey,
                        PGroupElementArray input,
                        PGroupElementArray output) {

        BiKeyedArrayMap encryptor = shuffle.getEncryptor(1);

        PPGroup pGroupDomain = (PPGroup)encryptor.getPGroupDomain();
        APGroup aPGroupDomain = (APGroup)pGroupDomain.project(1);
        PGroup arrayPGroup = encryptor.getArrayRange();

        // Homomorphism
        PGroupElement one = aPGroupDomain.getONE();
        PGroupElement pGroupElement =
            pGroupDomain.product(fullElGamalPKey, one);
        HomPRingPGroup hom = encryptor.restrict(pGroupElement);

        // Initialize proof.
        CCPoSBasicW V = new CCPoSBasicW(challengeBitLength,
                                        batchBitLength,
                                        statDist,
                                        prg);
        PGroupElement g = generators.getPGroup().getg();
        V.setInstance(g, generators,
                      permutationCommitment,
                      hom,
                      input, output);

        // Generate and set batching vector.
        ByteTreeContainer challengeData =
            new ByteTreeContainer(g.toByteTree(),
                                  generators.toByteTree(),
                                  permutationCommitment.toByteTree(),
                                  fullElGamalPKey.toByteTree(),
                                  input.toByteTree(),
                                  output.toByteTree());
        byte[] prgSeed = challenger.challenge(null,
                                              challengeData,
                                              8 * prg.minNoSeedBytes(),
                                              statDist);

        V.setBatchVector(prgSeed);


        // Read commitment.
        ByteTreeReader commitmentReader =
            (new ByteTreeF(CCPoSCfile(roProofDir, l))).getByteTreeReader();
        ByteTreeBasic commitment = V.setCommitment(commitmentReader);
        commitmentReader.close();


        // Generate a challenge.
        challengeData = new ByteTreeContainer(new ByteTree(prgSeed),
                                              commitment);
        byte[] challengeBytes = challenger.challenge(null,
                                                     challengeData,
                                                     challengeBitLength,
                                                     statDist);
        LargeInteger integerChallenge = LargeInteger.toPositive(challengeBytes);

        // Set the commitment and challenge.
        V.setChallenge(integerChallenge);


        // Read and verify reply.
        ByteTreeReader replyReader =
            (new ByteTreeF(CCPoSRfile(roProofDir, l))).getByteTreeReader();

        boolean verdict = V.verify(replyReader);
        replyReader.close();

        V.free();
        pGroupElement.free();

        return verdict;
    }

    public boolean verifyDecryption(int l,
                                    HomPRingPGroup hom,
                                    PGroupElement groupElement,
                                    PGroupElement commonInput) {

        SigmaProofBasic V = new SigmaProofBasic(hom,
                                                challengeBitLength,
                                                statDist);
        V.setInstance(commonInput);

        // Generate a seed.
        ByteTreeBasic seedData =
            new ByteTreeContainer(groupElement.toByteTree(),
                                  commonInput.toByteTree());
        byte[] seed = challenger.challenge(null,
                                           seedData,
                                           8 * prg.minNoSeedBytes(),
                                           statDist);

        // Batch our verifier.
        prg.setSeed(seed);
        V.batch(prg, batchBitLength);


        // Read and set the commitment of the prover.
        ByteTreeReader commitmentReader =
            (new ByteTreeF(DFCfile(roProofDir, l))).getByteTreeReader();
        ByteTreeBasic commitment = V.setCommitment(commitmentReader);
        commitmentReader.close();


        // Generate a challenge
        ByteTreeContainer challengeData =
            new ByteTreeContainer(new ByteTree(seed), commitment);


        byte[] challengeBytes = challenger.challenge(null,
                                                     challengeData,
                                                     challengeBitLength,
                                                     statDist);
        LargeInteger integerChallenge = LargeInteger.toPositive(challengeBytes);


        // Set the commitment and challenge.
        V.setChallenge(integerChallenge);

        // Read and verify reply.
        ByteTreeReader replyReader =
            (new ByteTreeF(DFRfile(roProofDir, l))).getByteTreeReader();
        boolean verdict = V.verify(replyReader);
        replyReader.close();

        V.free();
        return verdict;
    }

    /**
     * Print failure information, inclusing the stacktrace of the
     * given throwable, and then halt.
     *
     * @param message Message to print.
     * @param throwable Throwable causing the failure.
     */
    protected void failStop(String message, Throwable throwable) {
        if (verbose) {

            println("");
            println("");
            println("###############################################");
            println("################## FAIL! ######################");
            println("###############################################");
            println(message);
            println("###############################################");
            println("");

            if (throwable != null && opt.getBooleanValue("-e")) {
                throwable.printStackTrace(System.err);
            }
            TempFile.free();
        }
        System.exit(1);
    }

    /**
     * Print failure information and then halt.
     *
     * @param message Message to print.
     */
    protected void failStop(String message) {
        failStop(message, null);
    }

    /**
     * Print header.
     *
     * @param message Message to print.
     */
    void printHeader(String message) {
        if (!message.equals("")) {
            message = " " + message + " ";
        }
        if (verbose) {
            println("");
            StringBuilder sb = new StringBuilder();
            sb.append("============" + message);
            int a = Math.max(0, 64 - sb.length());
            for (int i = 0; i < a; i++) {
                sb.append("=");
            }
            println(sb.toString());
        }
    }

    /**
     * Print message.
     *
     * @param message Message to print.
     */
    void print(String message) {
        if (verbose) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd HH:mm:ss");
            System.out.print(sdf.format(new Date()) + " " + message);
        }
    }

    /**
     * Print message and a newline.
     *
     * @param message Message to print.
     */
    void println(String message) {
        if (verbose) {
            System.out.println(message);
        }
    }

    /**
     * Print message and a newline informatively after a failure.
     *
     * @param message Message to print.
     */
    void failInfo(String message) {
        if (verbose) {
            System.out.println("--> " + message);
        }
    }

    /**
     * Generates option instance.
     *
     * @param commandName Command name used when printing usage
     * info.
     * @return Option instance.
     */
    static Opt opt(String commandName) {

        String defaultErrorString =
            "Invalid invocation. Please use \"" + commandName +
            " -h\" for usage information!";

        Opt opt = new Opt(commandName, defaultErrorString);

        opt.addParameter("protocolInfo", "Protocol info file.");
        opt.addParameter("pkey", "El Gamal public key.");
        opt.addParameter("ciphertexts", "Input ciphertexts.");
        opt.addParameter("plaintexts", "Output plaintexts.");
        opt.addParameter("roProof", "Directory containing the RO-proofs.");

        opt.addOption("-h", "", "Print usage information.");
        opt.addOption("-version", "", "Print the package version and list " +
                      "protocol versions it can handle.");
        opt.addOption("-wd", "dir",
                      "Directory for temporary files (default is " +
                      "/tmp/verificatum). This directory is deleted on " +
                      "exit.");
        opt.addOption("-a", "value",
                      "Use file based arrays or not must be either \"file\" " +
                      "or \"ram\". Default is \"ram\".");
        opt.addOption("-v", "", "Verbose output, i.e., turn on output.");
        opt.addOption("-e", "", "Show stack trace of an exception.");

        opt.addUsageForm();
        opt.appendToUsageForm(0, "-h###");

        opt.addUsageForm();
        opt.appendToUsageForm(1,
                              "#-v,-e,-wd,-a#protocolInfo,roProof#");

        opt.addUsageForm();
        opt.appendToUsageForm(2,
                              "#-v,-e,-wd,-a#protocolInfo,roProof,pkey," +
                              "ciphertexts,plaintexts#");

        opt.addUsageForm();
        opt.appendToUsageForm(3, "-version###");

        String s = "Verifies the overall correctness of an execution using " +
            "the intermediate results and the heuristically secure " +
            "Fiat-Shamir proofs in the given proof directory. If the " +
            "public key, ciphertexts, and plaintexts are given then a " +
            "full verification is performed, and otherwise only the contents " +
            "of the proof directory is verified.";
        opt.appendDescription(s);

        return opt;
    }

    /**
     * Command line interface.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {

        if (args.length < 3) {
            System.err.println("The first three parameters must be " +
                               "<commandname> <random source file> " +
                               "<random seed file>");
        }

        String commandName = args[0];

        RandomSource randomSource = null;
        Opt opt = null;
        try {

            File rsFile = new File(args[1]);
            File seedFile = new File(args[2]);
            File tmpSeedFile = new File(args[2] + "_TMP");
            randomSource = GeneratorTool.standardRandomSource(rsFile,
                                                              seedFile,
                                                              tmpSeedFile);
            opt = opt(args[0]);
            opt.parse(Arrays.copyOfRange(args, 3, args.length));

        } catch (Exception e) {
            String es = "\n" + "ERROR: " + e.getMessage() + "\n";
            System.err.println(es);
            System.exit(0);
        }

        if (opt.getBooleanValue("-h")) {

            System.out.println(opt.usage());
            System.exit(0);

        } else if (opt.getBooleanValue("-version")) {

            System.out.println(Version.packageVersion + " " +
                               "(" + compatibleProtocolVersions() + ")");
            System.exit(0);

        } else {

            tmpDir = new File("/tmp/verificatum");
            if (opt.valueIsGiven("-wd")) {
                tmpDir = new File(opt.getStringValue("-wd"));
            }
            tmpDir.mkdirs();
            TempFile.init(tmpDir);

            if (opt.valueIsGiven("-a")) {
                String arrays = opt.getStringValue("-a");
                if (arrays.equals("file")) {

                    LargeIntegerArray.useFileBased();

                } else if (!arrays.equals("ram")) {
                    System.err.println("Unknown parameter to \"-a\"!");
                    System.exit(1);
                }
            }

            boolean verifyExternal = false;
            if (opt.valueIsGiven("pkey")) {
                verifyExternal = true;
            }

            SimpleTimer simpleTimer = new SimpleTimer();

            MixNetElGamalVerifyRO verifier =
                new MixNetElGamalVerifyRO(opt, randomSource);
            verifier.verify(verifyExternal);

            if (opt.getBooleanValue("-v")) {
                System.out.println("Verification completed SUCCESSFULLY " +
                                   "after " + simpleTimer + ".");
            }
            TempFile.free();
            System.exit(0);
        }
    }
}
