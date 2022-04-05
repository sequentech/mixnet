
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

import java.io.*;

import mixnet.eio.*;
import mixnet.ui.gen.*;
import mixnet.ui.opt.*;

/**
 * Generates a human oriented string representation of a
 * <code>RandomSourceCombiner</code> suitable for initialization
 * files.
 *
 * @author Douglas Wikstrom
 */
public class RandomSourceCombinerGen implements Generator {

    /**
     * Generates an option instance containing suitable options and
     * description.
     *
     * @return Option instance representing valid inputs to this
     * instance.
     */
    protected Opt opt() {
        Opt opt =
            GeneratorTool.defaultOpt(RandomSourceCombiner.class.
                                     getSimpleName(), 1);

        opt.setUsageComment("(where " +
                            RandomSourceCombiner.class.getSimpleName() +
                            " = " + RandomSourceCombiner.class.getName() + ")");

        opt.addParameter("rs", "Random source, an instance of " +
                         "mixnet.crypto.RandomSource.");
        opt.addParameter("rss", "Random sources, instances of " +
                         "mixnet.crypto.RandomSource.");

        String s = "Generates a random source combiner that is simply xors " +
            "the outputs of the random sources it encapsulates.";

        opt.appendToUsageForm(1, "##rs,+rss#");

        opt.appendDescription(s);

        return opt;
    }

    // Documented in mixnet.crypto.Generator.java.

    public String gen(RandomSource randomSource, String[] args)
    throws GenException {
        Opt opt = opt();

        String res = GeneratorTool.defaultProcess(opt, args);
        if (res != null) {
            return res;
        }

        try {
            String[] multi = opt.getMultiParameters();

            RandomSource[] rs = new RandomSource[1 + multi.length];
            String rsString = opt.getStringValue("rs");
            rs[0] = Marshalizer.unmarshalHex_RandomSource(rsString);

            for (int i = 0; i < multi.length; i++) {
                rsString = multi[i];
                rs[1 + i] = Marshalizer.unmarshalHex_RandomSource(rsString);
            }

            RandomSourceCombiner rsc = new RandomSourceCombiner(rs);

            // Make sure that what we have can be used directly.
            try {
                rsc.getBytes(1);
            } catch (CryptoError ce) {
                throw new GenException("Unable to read random bytes from " +
                                       "combiner! If one of your random " +
                                       "sources is a PRG, then please use " +
                                       "PRGCombiner instead.");
            }

            return Marshalizer.marshalToHexHuman(rsc,
                                                 opt.getBooleanValue("-v"));
        } catch (EIOException eioe) {
            throw new GenException("");
        }
    }

    public String briefDescription() {
        return "Random source combiner.";
    }
}
