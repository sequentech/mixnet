package mixnet.protocol.distrkeygen;

import java.io.*;
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.ui.*;
import mixnet.protocol.*;
import mixnet.protocol.coinflip.*;
import mixnet.ui.info.*;
import mixnet.protocol.secretsharing.*;

/**
 * Represents a protocol which jointly generates a list of
 * "independent" generators.
 *
 * @author Douglas Wikstrom
 */
public interface IndependentGenerators {

    /**
     * Generate the independent generators.
     *
     * @param log Logging context.
     * @param pGroup Group to which the generator belongs.
     * @param numberOfGenerators Number of generators.
     * @return Independent generators.
     */
    public PGroupElementArray generate(Log log,
                                       PGroup pGroup,
                                       int numberOfGenerators);
}
