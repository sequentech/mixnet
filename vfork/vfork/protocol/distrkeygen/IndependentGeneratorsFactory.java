package vfork.protocol.distrkeygen;

import java.io.*;
import java.util.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.ui.*;
import vfork.protocol.*;
import vfork.protocol.coinflip.*;
import vfork.ui.info.*;
import vfork.protocol.secretsharing.*;

/**
 * This is a joint factory class for {@link IndependentGeneratorsI}
 * and {@link IndependentGeneratorsRO}, i.e., it can be instantiated
 * to create instances of one of these classes.
 *
 * @author Douglas Wikstrom
 */
public class IndependentGeneratorsFactory {

    /**
     * Source of random coins.
     */
    protected CoinFlipPRingSource coins;

    /**
     * Pseudo-random generator used to extend the seed to an
     * "unpredictable" batching vector.
     */
    protected PRG prg;

    /**
     * Bit-size of the challenge.
     */
    protected int challengeBitLength;

    /**
     * Number of bits in each element in random vector used during
     * batching.
     */
    protected int batchBitLength;

    /**
     * Decides the statistical distance from the uniform distribution.
     */
    protected int statDist;

    /**
     * Hashfunction on which the random oracle is based.
     */
    protected Hashfunction roHashfunction;

    /**
     * Prefix used with each invocation of the random oracle.
     */
    protected byte[] globalPrefix;

    /**
     * Creates an instance of the factory.
     *
     * @param coins Source of jointly generated random bits.
     * @param prg Pseudo random generator used to expand a seed into a
     * random vector used for batching.
     * @param challengeBitLength Number of bits in challenges.
     * @param batchBitLength Number of bits in each component
     * in the random vector used for batching.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public IndependentGeneratorsFactory(CoinFlipPRingSource coins,
                                        PRG prg,
                                        int challengeBitLength,
                                        int batchBitLength,
                                        int statDist) {
        this.coins = coins;
        this.prg = prg;
        this.challengeBitLength = challengeBitLength;
        this.batchBitLength = batchBitLength;
        this.statDist = statDist;
        this.roHashfunction = null;
    }

    /**
     * Creates an instance of the factory.
     *
     * @param pGroup Group over which we run the protocol.
     * @param roHashfunction Hashfunction on which the random oracle
     * is based.
     * @param globalPrefix Prefix to random oracle.
     * @param statDist Decides the statistical distance from the
     * uniform distribution.
     */
    public IndependentGeneratorsFactory(Hashfunction roHashfunction,
                                        byte[] globalPrefix,
                                        int statDist) {
        this.roHashfunction = roHashfunction;
        this.globalPrefix = globalPrefix;
        this.statDist = statDist;
    }

    /**
     * Creates an instance of the protocol.
     *
     * @param sid Session identifier of the created instance.
     * @param protocol Protocol which invokes the created instance.
     * @return An instance of the protocol with the given parent
     * protocol and session identifier.
     */
    public IndependentGenerators newInstance(String sid, Protocol protocol) {
        if (roHashfunction == null) {
            return new IndependentGeneratorsI(sid,
                                              protocol,
                                              coins,
                                              prg,
                                              challengeBitLength,
                                              batchBitLength,
                                              statDist);
        } else {
            return new IndependentGeneratorsRO(sid, roHashfunction,
                                               globalPrefix, statDist);
        }
    }
}
