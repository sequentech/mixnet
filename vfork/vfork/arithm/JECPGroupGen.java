
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

package vfork.arithm;

import java.math.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.ui.gen.*;
import vfork.ui.opt.*;

/**
 * Generates a human oriented string representation of a
 * <code>JECPGroup</code> suitable for initialization files. Using
 * {@link vfork.ui.gen.GeneratorTool} this functionality can be
 * invoked from the command line.
 *
 * @author Douglas Wikstrom
 */
public class JECPGroupGen implements Generator {

    /**
     * Generates an option instance containing suitable options and
     * a general description.
     *
     * @return Option instance representing valid inputs to this
     * instance.
     */
    protected Opt opt() {
        Opt opt =
            GeneratorTool.defaultOpt(JECPGroup.class.getSimpleName(), 1);

        opt.setUsageComment("(where " + JECPGroup.class.getSimpleName() +
                            " = " + JECPGroup.class.getName() + ")");

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n");

        String[] curveNames = JECPGroupParams.getCurveNames();
        sb.append(curveNames[0]);
        for (int i = 1; i < curveNames.length; i++) {
            sb.append(" " + curveNames[i]);
        }

        opt.addOption("-name", "value",
                      "Name of standardized elliptic curve " +
                      "group. The following names can be used." +
                      sb.toString() + "\n\n");

        String s = "Generates one of the standard elliptic curve groups.";

        opt.appendToUsageForm(1, "-name###");

        opt.appendDescription(s);

        return opt;
    }

    /**
     * Turns a hexadecimal string (with spaces) to a non-negative integer.
     *
     * @param s Hexadecimal string to interpret.
     * @return Non-negative integer.
     */
    private static LargeInteger toLargeInteger(String s) {
        return new LargeInteger(new BigInteger("00" + s.replace(" ", ""), 16));
    }


    // Documented in vfork.crypto.Generator.java.

    public String gen(RandomSource randomSource, String[] args)
        throws GenException {
        Opt opt = opt();

        String res = GeneratorTool.defaultProcess(opt, args);
        if (res != null) {
            return res;
        }

        if (opt.valueIsGiven("-name")) {

            String name = opt.getStringValue("-name");

            PGroup pGroup = null;
            try {

                pGroup = JECPGroupParams.getJECPGroup(name);

            } catch (ArithmFormatException afe) {
                throw new GenException("Unknown curve name: \" + name + \"!");
            }

            return Marshalizer.marshalToHexHuman(pGroup,
                                                 opt.getBooleanValue("-v"));
        } else {
            throw new GenException("Unknown argument!");
        }
    }

    public String briefDescription() {
        return "Elliptic curve group.";
    }
}