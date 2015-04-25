package verificatum.protocol.distrkeygen;

import java.io.*;
import java.util.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.ui.*;
import verificatum.protocol.*;
import verificatum.protocol.coinflip.*;
import verificatum.ui.info.*;
import verificatum.protocol.secretsharing.*;

/**
 * Uses a "random oracle" to derive a list of "independent"
 * generators, i.e., a list of generators for which finding any
 * non-trivial representation implies that the discrete logarithm
 * assumption is violated.
 *
 * @author Douglas Wikstrom
 */
public class IndependentGeneratorsRO implements IndependentGenerators{

    /**
     * Hashfunction on which the "random oracle" is based.
     */
    protected Hashfunction roHashfunction;

    /**
     * Prefix used with each invocation of the random oracle.
     */
    protected byte[] globalPrefix;

    /**
     * Decides the statistical distance from the uniform distribution
     * assuming that the random oracle is truly random.
     */
    protected int statDist;

    /**
     * Session identifier distinguishing this derivation from other.
     */
    protected String sid;

    /**
     * Creates an instance. It is the responsibility of the user to
     * ensure that the session identifier is unique among all
     * applications that should give different "independent"
     * arrays of generators.
     *
     * @param sid Session identifier which separates this derivation
     * from others.
     * @param roHashfunction Hashfunction on which the random oracle
     * is based.
     * @param globalPrefix Prefix used with each invocation of the
     * random oracle used to derive the independent generators.
     * @param statDist Decides the statistical distance from the
     * uniform distribution assuming that the random oracle is truly
     * random.
     */
    public IndependentGeneratorsRO(String sid,
                                   Hashfunction roHashfunction,
                                   byte[] globalPrefix,
                                   int statDist) {
        this.sid = sid;
        this.roHashfunction = roHashfunction;
        this.globalPrefix = globalPrefix;
        this.statDist = statDist;
    }

    /**
     * Generate the independent generators.
     *
     * @param log Logging context.
     * @param pGroup Underlying group.
     * @param numberOfGenerators Number of generators to generate.
     * @return Independent generators.
     */
    public PGroupElementArray generate(Log log,
                                       PGroup pGroup,
                                       int numberOfGenerators) {
        if (log != null) {
            log.info("Derive independent generators using RO.");
        }

        PRG prg = new PRGHeuristic(roHashfunction);
        RandomOracle ro =
            new RandomOracle(roHashfunction, 8 * prg.minNoSeedBytes());

        Hashdigest d = ro.getDigest();
        d.update(globalPrefix);
        d.update((new ByteTree(sid.getBytes())).toByteArray());

        byte[] seed = d.digest();

        prg.setSeed(seed);

        return pGroup.randomElementArray(numberOfGenerators, prg, statDist);
    }
}
