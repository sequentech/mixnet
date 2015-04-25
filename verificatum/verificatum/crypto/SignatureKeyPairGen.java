
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

import verificatum.arithm.*;
import verificatum.eio.*;
import verificatum.ui.gen.*;
import verificatum.ui.opt.*;

/**
 * Generates a key pair using a key generator.
 *
 * @author Douglas Wikstrom
 */
public class SignatureKeyPairGen implements Generator {

    /**
     * Default statistical distance.
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
        Opt opt = GeneratorTool.
            defaultOpt(SignatureKeyPair.class.getSimpleName(), 1);

        opt.setUsageComment("(where " +
                            SignatureKeyPair.class.getSimpleName() +
                            " = " + SignatureKeyPair.class.getName() +
                            ")");

        opt.addParameter("gen",
                         "Signature key pair generator " +
                         "(verificatum.crypto.SignatureKeyGen).");
        opt.addOption("-cert", "value",
                      "Determines the probability that the underlying " +
                      "key generator is malformed, i.e., a value " +
                      "of t gives a bound of 2^(-t)." +
                      " Defaults to " + DEFAULT_CERT + ".");

        String s =
            "Generates a signature key pair using the given key generator.";

        opt.appendToUsageForm(1, "#-cert#gen#");
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

        String generatorString = opt.getStringValue("gen");
        int certainty = opt.getIntValue("-cert", DEFAULT_CERT);

        try {
            SignatureKeyGen generator =
                Marshalizer.unmarshalHexAux_SignatureKeyGen(generatorString,
                                                            randomSource,
                                                            certainty);

            SignatureKeyPair keyPair = generator.gen(randomSource);

            return Marshalizer.marshalToHexHuman(keyPair,
                                                 opt.getBooleanValue("-v"));
        } catch (EIOException eioe) {
            throw new GenException("Malformed key pair generator!", eioe);
        }
    }

    public String briefDescription() {
        return "Key pair generator.";
    }
}