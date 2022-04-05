
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

package mixnet.protocol.mixnet;

import java.io.*;
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.ui.*;
import mixnet.protocol.*;
import mixnet.protocol.coinflip.*;
import mixnet.protocol.hvzk.*;
import mixnet.ui.info.*;
import mixnet.protocol.secretsharing.*;

/**
 * Generates permutation commitments such that receivers are convinced
 * that the committer can open its commitment to a correct permutation
 * commitment.
 *
 * @author Douglas Wikstrom
 */
public class PermutationCommitment extends Protocol {

    /**
     * Index of committer.
     */
    protected int l;

    /**
     * Independent generators.
     */
    protected PGroupElementArray generators;

    /**
     * Group over which the commitment is defined.
     */
    protected PGroup pGroup;

    /**
     * Size of permutation.
     */
    protected int size;

    /**
     * Factory for the proof of a shuffle used.
     */
    protected PoSFactory posFactory;

    /**
     * Committed permutation.
     */
    protected Permutation permutation;

    /**
     * Commitment exponents.
     */
    protected PRingElementArray commitmentExponents;

    /**
     * Permutation commitment.
     */
    protected PGroupElementArray permutationCommitment;

    /**
     * Commitment of the identity permutation.
     */
    protected PGroupElementArray identityCommitment;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Underlying proof of a shuffle.
     */
    protected PoS pos;

    /**
     * Indicates that precomputation phase read data from file.
     */
    protected boolean fromFile;

    /**
     * Creates a correct permutation commitment with the given
     * committer.
     *
     * @param sid Session identifier of this instance.
     * @param protocol Protocol which invokes this one.
     * @param l Index of committer.
     * @param size Size of permutation.
     * @param generators Independent generators used to commit to a
     * permutation.
     * @param posFactory Factory for proofs of shuffles.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public PermutationCommitment(String sid,
                                 Protocol protocol,
                                 int l,
                                 int size,
                                 PGroupElementArray generators,
                                 PoSFactory posFactory,
                                 int statDist) {
        super(sid, protocol);
        this.l = l;
        this.size = size;
        this.generators = generators;
        this.pGroup = generators.getPGroup();
        this.posFactory = posFactory;
        this.statDist = statDist;
    }

    /**
     * Precompute as much as possible of the generation.
     *
     * @param log Logging context.
     */
    public void precompute(Log log) {

        log.info("Precompute permutation commitment.");
        Log tempLog = log.newChildLog();

        File permFile = getFile("Permutation");
        File commExpFile = getFile("CommitmentExponents");
        File idCommFile = getFile("IdentityCommitment");

        if (permFile.exists()) {

            try {

                // Read permutation
                tempLog.info("Read permutation from file.");
                ByteTreeReader permutationReader =
                    (new ByteTreeF(permFile)).getByteTreeReader();
                permutation = new Permutation(size, permutationReader);
                permutationReader.close();

                // Read commitment exponents
                tempLog.info("Read commitment exponents from file.");
                ByteTreeReader exponentsReader =
                    (new ByteTreeF(commExpFile)).getByteTreeReader();
                commitmentExponents =
                    pGroup.getPRing().toElementArray(size, exponentsReader);
                exponentsReader.close();

                // Read identity commitment.
                tempLog.info("Read identity commitment from file.");
                ByteTreeReader idCommitmentReader =
                    (new ByteTreeF(idCommFile)).getByteTreeReader();
                identityCommitment =
                    pGroup.toElementArray(size, idCommitmentReader);
                idCommitmentReader.close();

                // Compute permutation commitment.
                permutationCommitment = identityCommitment.permute(permutation);

            } catch (ArithmFormatException afe) {
                throw new ProtocolError("Unable to read commitment!", afe);
            }

            fromFile = true;

        } else {

            // Generate and store permutation.
            tempLog.info("Generate random permutation.");
            permutation = new Permutation(size, randomSource, statDist);

            tempLog.info("Write permutation to file.");
            permutation.toByteTree().unsafeWriteTo(permFile);


            // Generate and store commitment exponents.
            tempLog.info("Generate commitment exponents.");
            commitmentExponents =
                pGroup.getPRing().randomElementArray(size,
                                                     randomSource,
                                                     statDist);
            tempLog.info("Write commitment exponents to file.");
            commitmentExponents.toByteTree().unsafeWriteTo(commExpFile);


            // Compute and store identity commitment.
            tempLog.info("Compute identity commitment.");
            PGroupElementArray tmp = pGroup.getg().exp(commitmentExponents);
            identityCommitment = generators.mul(tmp);
            tmp.free();

            tempLog.info("Write identity commitment to file.");
            identityCommitment.toByteTree().unsafeWriteTo(idCommFile);


            // Permute identity commitment.
            permutationCommitment = identityCommitment.permute(permutation);

            fromFile = false;
        }
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
    protected File KLfile(File exportDir, int index) {
        return new File(exportDir, String.format("KeepList%02d.bt", index));
    }

    /**
     * Generate permutation commitment.
     *
     * @param log Logging context.
     * @param exportDir Export directory for universal verifiability.
     */
    public void generate(Log log, File exportDir) {

        File permCommFile = getFile("PermutationCommitment");

        log.info("Generate permutation commitment of " + ui.getDescrString(l));
        Log tempLog = log.newChildLog();

        if (l == j) {

            if (fromFile) {

                // If we are the committer then we have already read
                // the data we need from file in the call to
                // precompute(Log).
                return;
            }

        } else {

            if (permCommFile.exists()) {

                try {

                    // Read permutation commitment
                    tempLog.info("Read permutation commitment from file.");
                    ByteTreeReader commitmentReader =
                        (new ByteTreeF(permCommFile)).getByteTreeReader();
                    permutationCommitment =
                        pGroup.toElementArray(size, commitmentReader);
                    commitmentReader.close();

                    // If we read data from file, then all parties have it
                    // and we return.
                    return;

                } catch (ArithmFormatException afe) {
                    throw new ProtocolError("Unable to read commitment!", afe);
                }
            }
        }

        // We did not read from file, so we need to prove/verify.
        this.pos = posFactory.newPoS("", this);

        if (l == j) {

            // Publish permutation commitment
            tempLog.info("Publish permutation commitment.");
            bullBoard.publish("PermutationCommitment",
                              permutationCommitment.toByteTree(),
                              tempLog);

            // Prove knowledge of commitment exponents.
            pos.prove(tempLog,
                      pGroup.getg(),
                      generators,
                      permutationCommitment,
                      commitmentExponents,
                      permutation,
                      exportDir);

        } else {

            boolean trivial = false;

            // Read permutation commitment.
            tempLog.info("Read permutation commitment of "
                         + ui.getDescrString(l) + ".");
            ByteTreeReader commitmentReader =
                bullBoard.waitFor(l, "PermutationCommitment", tempLog);
            try {

                permutationCommitment =
                    pGroup.toElementArray(size, commitmentReader);

            } catch (ArithmFormatException afe) {

                trivial = true;
            } finally {
                commitmentReader.close();
            }

            // Verify knowledge of commitment exponents.
            if (!trivial) {

                trivial = !pos.verify(tempLog,
                                      l,
                                      pGroup.getg(),
                                      generators,
                                      permutationCommitment,
                                      exportDir);
            }

            if (trivial) {

                // If there is any error we set the commitment to the
                // trivial one.
                tempLog.info("Trivial commitment of identity permutation.");
                permutationCommitment =
                    generators.copyOfRange(0, generators.size());
            }

            tempLog.info("Write permutation commitment to file.");
            permutationCommitment.toByteTree().unsafeWriteTo(permCommFile);

        }
        if (exportDir != null) {
            File file = PCfile(exportDir, l);
            permutationCommitment.toByteTree().unsafeWriteTo(file);
        }

    }

    /**
     * Shrinks this instance to the given size.
     *
     * @param log Logging context.
     * @param noCiphertexts Number of ciphertexts after shrinking.
     * @param exportDir Export directory for universal verifiability.
     */
    public void shrink(Log log, int noCiphertexts, File exportDir) {

        if (noCiphertexts > permutationCommitment.size()) {
            throw new ArithmError("Attempting to increase size!");
        }

        log.info("Shrink permutation commitment of " + ui.getDescrString(l));
        Log tempLog = log.newChildLog();

        boolean[] keepList = null;

        if (l == j) {

            // Figure out which elements in the permutation commitment
            // we should keep.
            keepList = new boolean[permutation.size()];
            for (int i = 0; i < noCiphertexts; i++) {
                keepList[permutation.map(i)] = true;
            }

            ByteTree bt = ByteTree.booleanArrayToByteTree(keepList);
            bullBoard.publish("KeepList", bt, tempLog);

            if (exportDir != null) {
                bt.unsafeWriteTo(KLfile(exportDir, j));
            }

            // Shrink private data.
            tempLog.info("Shrink commitment exponents.");
            PRingElementArray oldCommitmentExponents = commitmentExponents;
            commitmentExponents =
                commitmentExponents.copyOfRange(0, noCiphertexts);
            oldCommitmentExponents.free();

            tempLog.info("Shrink permutation.");
            permutation = permutation.shrink(noCiphertexts);

        } else {

            boolean correct = true;

            ByteTreeReader btr = bullBoard.waitFor(l, "KeepList", tempLog);
            try {
                keepList = btr.readBooleans(permutationCommitment.size());
            } catch (EIOException eioe) {
                correct = false;
            } finally {
                btr.close();
            }

            // Compute total number of chosen elements.
            if (correct) {
                int total = 0;
                for (int i = 0; i < keepList.length; i++) {
                    if (keepList[i]) {
                        total++;
                    }
                }
                if (total != noCiphertexts) {
                    correct = false;
                }
            }

            // Set to trivial array if there is a problem.
            if (!correct) {
                keepList = new boolean[permutationCommitment.size()];
                Arrays.fill(keepList, 0, noCiphertexts, true);
            }
        }

        if (exportDir != null) {
            ByteTreeBasic bt = ByteTree.booleanArrayToByteTree(keepList);
            bt.unsafeWriteTo(KLfile(exportDir, l));
        }

        tempLog.info("Shrink permutation commitment.");
        PGroupElementArray oldPermutationCommitment = permutationCommitment;
        permutationCommitment = permutationCommitment.extract(keepList);
        oldPermutationCommitment.free();
    }

    /**
     * Returns the generated permutation.
     *
     * @return Underlying permutation.
     */
    public Permutation getPermutation() {
        return permutation;
    }

    /**
     * Returns the exponents used to form the commitment.
     *
     * @return Exponents used to form the commitment.
     */
    public PRingElementArray getCommitmentExponents() {
        return commitmentExponents;
    }

    /**
     * Returns the permutation commitment.
     *
     * @return The permutation commitment.
     */
    public PGroupElementArray getCommitment() {
        return permutationCommitment;
    }

    /**
     * Frees any resources allocated by this instance.
     */
    public void free() {
        if (identityCommitment != null) {
            identityCommitment.free();
        }
        if (commitmentExponents != null) {
            commitmentExponents.free();
        }
        if (permutationCommitment != null) {
            permutationCommitment.free();
        }
    }
}
