
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

package mixnet.protocol.secretsharing;

import java.io.*;
import java.util.*;

import mixnet.arithm.*;
import mixnet.crypto.*;
import mixnet.eio.*;
import mixnet.protocol.*;
import mixnet.protocol.demo.*;
import mixnet.protocol.distrkeygen.*;
import mixnet.ui.*;
import mixnet.ui.info.*;
import mixnet.ui.opt.*;

/**
 * Demonstrates Pedersen's verifiable secret sharing protocol {@link
 * mixnet.protocol.secretsharing.Pedersen}.
 *
 * @author Douglas Wikstrom
 */
public class DemoPedersen extends DemoPlainKeys {

    // These methods are documented in DemoProtocolFactory.java.

    public Runnable newProtocol(PrivateInfo privateInfo,
                                ProtocolInfo protocolInfo,
                                UI ui) {
        return new ExecPedersen(privateInfo, protocolInfo, ui);
    }

    public Opt generateOpt(Demo demo) {
        Opt opt = super.generateOpt(demo);

        opt.addOption("-pGroup", "pGroup",
                      "Prime order group used in the protocol.");
        opt.appendToUsageForm(0, "#-pGroup##");

        return opt;
    }

    public ProtocolInfo newProtocolInfo() {
        ProtocolInfo pi = super.newProtocolInfo();
        pi.addInfoField(new StringField("pgroup", 1, 1));
        return pi;
    }

    public void generateProtocolInfo(ProtocolInfo pi,
                                     Demo demo,
                                     Opt opt) {
        super.generateProtocolInfo(pi, demo, opt);

        pi.addValue("pgroup",
                    opt.getStringValue("-pGroup", ProtocolDefaults.PGroup()));
    }

    /**
     * This is the main function called from the command line to
     * invoke the demonstration.
     *
     * @param args Arguments to the demonstration.
     */
    public static void main(String[] args) {

        Demo demo = new Demo(args, new DemoPedersen());
    }

    /**
     * Turns {@link Pedersen} into a runnable object.
     */
    class ExecPedersen extends Protocol
        implements Runnable {

        public ExecPedersen(PrivateInfo privateInfo,
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
                pGroup = new PPGroup(pGroup, 2);

                BiPRingPGroup biExp = new BiExp(pGroup);
                HomPRingPGroup hom = biExp.restrict(pGroup.getg());

                Pedersen ped = new Pedersen("DemoSID",
                                            this, 1,
                                            hom,
                                            plainKeys.getPKeys(),
                                            plainKeys.getSKey(),
                                            statDist,
                                            true);
                if (j == 1) {
                    if (!ped.stateOnFile()) {
                        PRingElement secret =
                            pGroup.getPRing().randomElement(randomSource,
                                                            statDist);
                        ped.dealSecret(ui.getLog(), secret);

                        ui.getLog().info("secret = " + secret.toString());
                    }
                } else {
                    ped.receiveShare(ui.getLog());
                }
                PRingElement ps = ped.recover(ui.getLog());

                ui.getLog().info("Recovered secret = " + ps.toString());
            } catch (Exception e) {
                throw new DemoError("Unable to run demonstration!", e);
            }
        }
    }
}

