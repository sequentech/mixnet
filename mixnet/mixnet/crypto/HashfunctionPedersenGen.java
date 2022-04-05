
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

package mixnet.crypto;

import mixnet.arithm.*;
import mixnet.eio.*;
import mixnet.ui.gen.*;
import mixnet.ui.opt.*;

/**
 * Generates a human oriented string representation of a
 * <code>HashfunctionPedersen</code> suitable for initialization
 * files. Using {@link mixnet.ui.gen.GeneratorTool} this
 * functionality can be invoked from the command line.
 *
 * @author Douglas Wikstrom
 */
public class HashfunctionPedersenGen implements Generator {

    /**
     * Default number of generators used in the hashfunction.
     */
    final static int DEFAULT_WIDTH = 2;

    /**
     * Default statistical distance used when generating a hashfunction.
     */
    final static int DEFAULT_STATDIST = 100;

    /**
     * Default statistical distance used when generating a hashfunction.
     */
    final static int DEFAULT_CERT = 100;

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     * instance.
     */
    protected Opt opt() {
        Opt opt = GeneratorTool
            .defaultOpt(HashfunctionPedersen.class.getSimpleName(), 1);

        opt.setUsageComment("(where " +
                            HashfunctionPedersen.class.getSimpleName() + " = "
                            + HashfunctionPedersen.class.getName() + ")");

        opt.addParameter("pGroup", "Underlying prime order group, an " +
                         "instance of " + PGroup.class.getName() +
                         ".");

        opt.addOption("-width", "width",
                      "Number of generators used. Defaults to " +
                      DEFAULT_WIDTH + ".");
        opt.addOption("-statDist", "bits", "Statistical error parameter. " +
                      "Defaults to " + DEFAULT_STATDIST + ".");
        opt.addOption("-cert", "value",
                      "Certainty that the group is correct, i.e., a value " +
                      "of t gives a probability of an" +
                      "incorrect group of at most 2^(-t). " +
                      "Defaults to " + DEFAULT_CERT + ".");

        String s = "Generates an instance of the Pedersen hashfunction over " +
            "the given group.";

        opt.appendToUsageForm(1, "#-width,-statDist,-cert#pGroup#");

        opt.appendDescription(s);

        return opt;
    }

    // Documented in mixnet.crypto.Generator.java.

    public String gen(RandomSource randomSource, String[] args)
    throws GenException {
        GeneratorTool.verify(randomSource);

        Opt opt = opt();

        String res = GeneratorTool.defaultProcess(opt, args);
        if (res != null) {
            return res;
        }

        try {

            int width = opt.getIntValue("-width", DEFAULT_WIDTH);
            if (width > HashfunctionPedersen.MAX_WIDTH) {
                throw new GenException("Too large width, maximum is " +
                                       HashfunctionPedersen.MAX_WIDTH +
                                       "!");
            }

            int statDist = opt.getIntValue("-statDist", DEFAULT_STATDIST);
            int cert = opt.getIntValue("-cert", DEFAULT_CERT);

            String pGroupString = opt.getStringValue("pGroup");
            PGroup pGroup =
                Marshalizer.unmarshalHexAux_PGroup(pGroupString,
                                                   randomSource,
                                                   cert);

            HashfunctionPedersen phf =
                new HashfunctionPedersen(pGroup, width, randomSource, statDist);

            return Marshalizer.marshalToHexHuman(phf,
                                                 opt.getBooleanValue("-v"));
        } catch (EIOException eioe) {
            throw new GenException("Unable to parse group description!", eioe);
        }
    }

    public String briefDescription() {
        return "Pedersen's hashfunction.";
    }
}