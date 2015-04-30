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
