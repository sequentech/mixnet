
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

package mixnet.protocol.distrkeygen;

import java.io.*;
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.ui.*;
import mixnet.ui.info.*;
import mixnet.ui.opt.*;
import mixnet.ui.tui.*;
import mixnet.util.*;
import mixnet.protocol.*;
import mixnet.protocol.coinflip.*;
import mixnet.protocol.hvzk.*;
import mixnet.protocol.distrkeygen.*;

/**
 * Implements a verifiable decryption protocol.
 *
 * @author Douglas Wikstrom
 */
public class VerifiableDecryption extends Protocol {

    /**
     * Underlying key generation protocol.
     */
    protected DKG dkg;

    /**
     * Source of challenges.
     */
    protected Challenger challenger;

    /**
     * PRG used for batching.
     */
    protected PRG prg;

    /**
     * Bit-size of the challenge.
     */
    protected int challengeBitLength;

    /**
     * Bit-size of each component when batching.
     */
    protected int batchBitLength;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Constructs a verifiable decryption protocol.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param dkg Underlying distributed key generation protocol.
     * @param challenger Source of challenges.
     * @param prg PRG used for batching homomorphisms that allow this.
     * @param challengeBitLength Number of bits in challenges.
     * @param batchBitLength Number of bits in each component when batching.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public VerifiableDecryption(String sid,
                                Protocol protocol,
                                DKG dkg,
                                Challenger challenger,
                                PRG prg,
                                int challengeBitLength,
                                int batchBitLength,
                                int statDist) {
        super(sid, protocol);
        this.dkg = dkg;
        this.prg = prg;
        this.challenger = challenger;
        this.challengeBitLength = challengeBitLength;
        this.batchBitLength = batchBitLength;
        this.statDist = statDist;
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
                        String.format("DecrFactCommitment%02d.bt", index));
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
     * Name of file containing the decrypted plaintext elements.
     *
     * @param exportDir Export directory for universal verifiability.
     * @return File where the output plaintext elements are stored.
     */
    protected static File Pfile(File exportDir) {
        return new File(exportDir, String.format("PlaintextElements.bt"));
    }

    /**
     * Performs the joint verifiable decryption.
     *
     * @param log Logging context.
     * @param decryptor Bilinear map used to compute individual
     * decryption factors.
     * @param finalizer Homomorphism used to compute the plaintexts
     * from the ciphertexts and the product of the decryption factors.
     * @param ciphertexts Ciphertexts to decrypt.
     * @param exportDir Export directory for universal verifiability.
     * @return Array of plaintexts.
     */
    public PGroupElementArray decrypt(Log log,
                                      BiKeyedArrayMap decryptor,
                                      HomPGroupPGroup finalizer,
                                      PGroupElementArray ciphertexts,
                                      File exportDir) {

        log.info("Jointly decrypt ciphertexts.");
        Log tempLog = log.newChildLog();

        // Construct wrapper for ciphertexts.
        APGroup aPGroup =
            (APGroup)((PPGroup)decryptor.getPGroupDomain()).project(1);
        APGroupElement ciphertextsAsElement = aPGroup.toElement(ciphertexts);

        // Basic public key
        PGroupElement basicPublicKey = dkg.getBasicPublicKey();

        // Combine to a single element.
        PGroupElement groupElement =
            ((PPGroup)decryptor.getPGroupDomain()).
            product(basicPublicKey, ciphertextsAsElement);

        // Form homomorphism by restricting the bilinear map.
        HomPRingPGroup hom = decryptor.restrict(groupElement);

        // Make room for all results.
        PGroupElement[] factorsElements = new PGroupElement[k + 1];

        // Perform actual computations.
        tempLog.info("Compute decryption factors.");
        factorsElements[j] = hom.map(dkg.getSecretKey(log, j));

        if (exportDir != null) {
            File dfFile = DFfile(exportDir, j);
            factorsElements[j].toByteTree().unsafeWriteTo(dfFile);
        }

        for (int l = 1; l <= k; l++) {

            if (l == j) {

                // Publish our decryption factors.
                tempLog.info("Publish decryption factors.");
                bullBoard.publish("Factors",
                                  factorsElements[j].toByteTree(),
                                  tempLog);

                // Prove correctness of our factors.
                SigmaProof P = new SigmaProof("" + j,
                                              this,
                                              challenger,
                                              prg,
                                              challengeBitLength,
                                              batchBitLength,
                                              statDist);
                P.prove(tempLog,
                        hom,
                        groupElement,
                        factorsElements[j],
                        dkg.getSecretKey(log, j),
                        exportDir);

                if (exportDir != null) {
                    SigmaProof.Cfile(exportDir).renameTo(DFCfile(exportDir, j));
                    SigmaProof.Rfile(exportDir).renameTo(DFRfile(exportDir, j));
                }

            } else {

                // Assume that everything will be good.
                boolean verdict = true;

                // Read and set the commitment of the prover.
                tempLog.info("Read decryption factors of " +
                             ui.getDescrString(l) + ".");
                ByteTreeReader factorsReader =
                    bullBoard.waitFor(l, "Factors", tempLog);

                try {
                    factorsElements[l] =
                        hom.getRange().toElement(factorsReader);

                } catch (ArithmFormatException afe) {
                    verdict = false;
                } finally {
                    factorsReader.close();
                }

                // Check that part of the output matches the public
                // key of the party.
                if (verdict) {
                    verdict = ((PPGroupElement)factorsElements[l]).project(0).
                        equals(dkg.getPublicKey(l));
                }

                // Verify the proof
                if (verdict) {
                    SigmaProof V = new SigmaProof("" + l,
                                                  this,
                                                  challenger,
                                                  prg,
                                                  challengeBitLength,
                                                  batchBitLength,
                                                  statDist);
                    verdict = V.verify(tempLog,
                                       l,
                                       hom,
                                       groupElement,
                                       factorsElements[l],
                                       exportDir);

                    if (!verdict) {
                        factorsElements[l].free();
                    }

                    if (exportDir != null) {
                        SigmaProof.Cfile(exportDir).
                            renameTo(DFCfile(exportDir, l));
                        SigmaProof.Rfile(exportDir).
                            renameTo(DFRfile(exportDir, l));
                    }
                }

                // If the proof failed we recover the secret key and
                // compute the result in the open.
                if (!verdict) {

                    PRingElement sk = dkg.recoverSecretKey(tempLog, l);

                    if (exportDir != null) {
                        File skFile = SKfile(exportDir, l);
                        sk.toByteTree().unsafeWriteTo(skFile);
                    }

                    tempLog.info("Compute decryption factors of " +
                                 ui.getDescrString(l) + " in the open.");
                    factorsElements[l] = hom.map(sk);
                }

                if (exportDir != null) {
                    File dfFile = DFfile(exportDir, l);
                    factorsElements[l].toByteTree().unsafeWriteTo(dfFile);
                }
            }
        }

        // Compute product of the factors.
        PGroupElement factorElement = factorsElements[1];
        for (int l = 2; l <= k; l++) {
            PGroupElement prod = factorElement.mul(factorsElements[l]);
            factorElement.free();
            factorsElements[l].free();
            factorElement = prod;
        }
        hom.free();

        // Pack factors and ciphertexts to be input to finalizer.
        PGroupElement factors = ((PPGroupElement)factorElement).project(1);

        PPGroup pPGroup = new PPGroup(factors.getPGroup(),
                                      ciphertextsAsElement.getPGroup());

        // Finalize decryption.
        PGroupElement res =
            finalizer.map(pPGroup.product(factors, ciphertextsAsElement));
        factors.free();

        // Return the plaintexts.
        PGroupElementArray plaintextsArray = ((APGroupElement)res).getContent();

        File plaintextFile = Pfile(exportDir);
        plaintextsArray.toByteTree().unsafeWriteTo(plaintextFile);

        return plaintextsArray;
    }
}



