
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

package verificatum.crypto;

import java.io.*;

import verificatum.eio.*;
import verificatum.arithm.*;
import verificatum.ui.gen.*;
import verificatum.ui.opt.*;

/**
 * Generates a human oriented string representation of a
 * <code>CramerShoupKeyGen</code> suitable for initialization files.
 *
 * @author Douglas Wikstrom
 */
public class CryptoKeyGenCramerShoupGen implements Generator {

    /**
     * Default certainty used to check correctness of group.
     */
    final static int CERTAINTY = 100;

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     * instance.
     */
    protected Opt opt() {
        Opt opt =
            GeneratorTool.defaultOpt(CryptoKeyGenCramerShoup.class.
                                     getSimpleName(), 1);

        opt.setUsageComment("(where " +
                            CryptoKeyGenCramerShoup.class.getSimpleName() +
                            " = " +
                            CryptoKeyGenCramerShoup.class.getName() + ")");

        opt.addParameter("pGroup", "Group over which the keys are generated, " +
                         "instance of verificatum.arithm.PGroup.");
        opt.addParameter("crhf",
                         "Collision-resistant hashfunction, instance of " +
                         "verificatum.crypto.Hashfunction.");

        opt.addOption("-cert", "value", "Certainty in primality testing, " +
                      "i.e., a value of t gives a probability of an " +
                      "incorrect group of at most 2^(-t).");

        String s = "Outputs a key generator of Cramer-Shoup keys of the " +
            "given group and with the given hashfunction.";

        opt.appendToUsageForm(1, "#-cert#pGroup,crhf#");

        opt.appendDescription(s);

        return opt;
    }

    // Documented in verificatum.crypto.Generator.java.

    public String gen(RandomSource randomSource, String[] args)
    throws GenException {
        Opt opt = opt();

        String res = GeneratorTool.defaultProcess(opt, args);
        if (res != null) {
            return res;
        }

        try {

            int certainty = CERTAINTY;
            if (opt.valueIsGiven("-cert")) {
                certainty = opt.getIntValue("-cert");
            }

            String pGroupString = opt.getStringValue("pGroup");

            PGroup pGroup = Marshalizer.unmarshalHexAux_PGroup(pGroupString,
                                                               randomSource,
                                                               certainty);

            String crhfString = opt.getStringValue("crhf");
            Hashfunction crhf =
                Marshalizer.unmarshalHexAux_Hashfunction(crhfString,
                                                         randomSource,
                                                         certainty);

            CryptoKeyGen keyGen = new CryptoKeyGenCramerShoup(pGroup, crhf);

            return Marshalizer.marshalToHexHuman(keyGen,
                                                 opt.getBooleanValue("-v"));

        } catch (EIOException eioe) {
            throw new GenException("Unable to create key generator! (" +
                                   eioe.getMessage() + ")", eioe);
        }
    }

    public String briefDescription() {
        return "Constructor of Cramer-Shoup key generator.";
    }
}
