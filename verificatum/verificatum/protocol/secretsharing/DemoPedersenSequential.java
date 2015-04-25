
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

package verificatum.protocol.secretsharing;

import java.io.*;
import java.util.*;

import verificatum.arithm.*;
import verificatum.crypto.*;
import verificatum.eio.*;
import verificatum.protocol.*;
import verificatum.protocol.demo.*;
import verificatum.protocol.distrkeygen.*;
import verificatum.ui.*;
import verificatum.ui.info.*;
import verificatum.ui.opt.*;

/**
 * Demonstrates {@link
 * verificatum.protocol.secretsharing.PedersenSequential}.
 *
 * @author Douglas Wikstrom
 */
public class DemoPedersenSequential extends DemoPedersen {

    // These methods are documented in DemoProtocolFactory.java.

    public Runnable newProtocol(PrivateInfo privateInfo,
                                ProtocolInfo protocolInfo,
                                UI ui) {
        return new ExecPedersenSequential(privateInfo, protocolInfo, ui);
    }

    /**
     * This is the main function called from the command line to
     * invoke the demonstration.
     *
     * @param args Arguments to the demonstration.
     */
    public static void main(String[] args) {

        Demo demo = new Demo(args, new DemoPedersenSequential());
    }

    /**
     * Turns {@link PedersenSequential} into a runnable object.
     */
    class ExecPedersenSequential extends Protocol
        implements Runnable {

        public ExecPedersenSequential(PrivateInfo privateInfo,
                                      ProtocolInfo protocolInfo,
                                      UI ui) {
            super(privateInfo, protocolInfo, ui);
            startServers();
        }

        public void run() {
            try {

                int statDist = protocolInfo.getIntValue("statdist");
                int certainty = protocolInfo.getIntValue("certainty");


                String keyGenString = privateInfo.getStringValue("keyGen");
                CryptoKeyGen keyGen =
                    Marshalizer.unmarshalHexAux_CryptoKeyGen(keyGenString,
                                                             randomSource,
                                                             certainty);
                PlainKeys plainKeys =
                    new PlainKeys("DemoSID", this, keyGen, statDist);
                plainKeys.generate(ui.getLog());

                String pGroupString = protocolInfo.getStringValue("pgroup");
                PGroup pGroup = Marshalizer.unmarshalHexAux_PGroup(pGroupString,
                                                                   randomSource,
                                                                   certainty);

                boolean[] eliminated = new boolean[k + 1];
                Arrays.fill(eliminated, false);

                BiPRingPGroup biExp = new BiExp(pGroup);
                HomPRingPGroup hom = biExp.restrict(pGroup.getg());

                PedersenSequential peds =
                    new PedersenSequential("DemoSID",
                                           this,
                                           hom,
                                           plainKeys.getPKeys(),
                                           plainKeys.getSKey(),
                                           statDist,
                                           true);

                PRingElement secret =
                    pGroup.getPRing().randomElement(randomSource, statDist);

                ui.getLog().info("secret = " + secret.toString());

                peds.execute(ui.getLog(), secret);

                PRingElement ps;

                for (int l = 1; l <= k; l++) {
                    ps = peds.recover(ui.getLog(), l);
                    ui.getLog().info("Recovered secret of " + l + " = "
                                     + ps.toString());
                }

                Pedersen ped = peds.collapse("Collapsed_sid", this, false,
                                             ui.getLog());
                ps = ped.recover(ui.getLog());
                ui.getLog().info("Recovered secret of collapsed = "
                                 + ps.toString());

            } catch (Exception e) {
                throw new DemoError("Unable to run demonstration!", e);
            }
        }
    }
}
