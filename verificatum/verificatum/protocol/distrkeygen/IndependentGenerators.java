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
