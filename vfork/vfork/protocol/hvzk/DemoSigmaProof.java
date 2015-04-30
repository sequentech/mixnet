
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

package vfork.protocol.hvzk;

import java.io.*;
import java.util.*;

import vfork.arithm.*;
import vfork.crypto.*;
import vfork.eio.*;
import vfork.protocol.*;
import vfork.protocol.coinflip.*;
import vfork.protocol.demo.*;
import vfork.protocol.distrkeygen.*;
import vfork.ui.*;
import vfork.ui.info.*;
import vfork.ui.opt.*;

/**
 * Demonstrates the execution of a {@link SigmaProof}.
 *
 * @author Douglas Wikstrom
 */
public class DemoSigmaProof extends DemoPlainKeys {

    // These methods are documented in DemoProtocolFactory.java.

    public Runnable newProtocol(PrivateInfo privateInfo,
                                ProtocolInfo protocolInfo,
                                UI ui) {
        return new ExecSigmaProof(privateInfo, protocolInfo, ui);
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
        pi.addInfoField(new IntField("challengeBitLength", 1, 1));
        pi.addInfoField(new IntField("batchBitLength", 1, 1));
        return pi;
    }

    public void generateProtocolInfo(ProtocolInfo pi,
                                     Demo demo,
                                     Opt opt) {
        super.generateProtocolInfo(pi, demo, opt);

        pi.addValue("pgroup",
                    opt.getStringValue("-pGroup", ProtocolDefaults.PGroup()));
        pi.addValue("batchBitLength",
                    opt.getIntValue("-sece",
                                    ProtocolDefaults.SEC_PARAM_BATCH));
        pi.addValue("challengeBitLength",
                    opt.getIntValue("-secc",
                                    ProtocolDefaults.SEC_PARAM_CHALLENGE));
    }

    /**
     * This is the main function called from the command line to
     * invoke the demonstration.
     *
     * @param args Arguments to the demonstration.
     */
    public static void main(String[] args) {

        Demo demo = new Demo(args, new DemoSigmaProof());
    }

    /**
     * Turns {@link SigmaProof} into a runnable object.
     */
    class ExecSigmaProof extends Protocol
        implements Runnable {

        public ExecSigmaProof(PrivateInfo privateInfo,
                              ProtocolInfo protocolInfo,
                              UI ui) {
            super(privateInfo, protocolInfo, ui);
            startServers();
        }

        public void run() {
            try {

                int statDist = protocolInfo.getIntValue("statdist");
                int certainty = protocolInfo.getIntValue("certainty");
                int batchBitLength = protocolInfo.getIntValue("batchBitLength");
                int challengeBitLength =
                    protocolInfo.getIntValue("challengeBitLength");

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


                CoinFlipPRingSource coins =
                    new CoinFlipPRingSource("DemoCoinsSID",
                                             this,
                                             pGroup.getg().exp(7),
                                             plainKeys.getPKeys(),
                                             plainKeys.getSKey(),
                                             statDist);
                Challenger challenger1 = new ChallengerI(coins);


                BiPRingPGroup biExp1 = new BiExp(pGroup);
                HomPRingPGroup hom1 = biExp1.restrict(pGroup.getg());

                PRG prg1 = new PRGHeuristic();
                byte[] seed1 = new byte[prg1.minNoSeedBytes()];
                Arrays.fill(seed1, (byte)0);
                prg1.setSeed(seed1);

                PRingElement privateInput1 =
                    hom1.getDomain().randomElement(prg1, statDist);

                PGroupElement commonInput1 = hom1.map(privateInput1);

                 SigmaProof sigma1 = new SigmaProof("DemoSigmaProofSID1",
                                                   this,
                                                   challenger1,
                                                   prg1,
                                                   challengeBitLength,
                                                   batchBitLength,
                                                   statDist);
                if (j == 1) {
                    sigma1.prove(ui.getLog(), hom1, pGroup.getg(),
                                 commonInput1, privateInput1,
                                 null);
                } else {
                    sigma1.verify(ui.getLog(), 1, hom1, pGroup.getg(),
                                  commonInput1, null);
                }


                Challenger challenger2 =
                    new ChallengerRO(new HashfunctionHeuristic("SHA-256"),
                                     new byte[0]);

                PGroup pGroup2 = new APGroup(pGroup, 100);

                BiPRingPGroup biExp2 = new BiExp(pGroup2);
                HomPRingPGroup hom2 = biExp2.restrict(pGroup2.getg());

                PRG prg2 = new PRGHeuristic();
                byte[] seed2 = new byte[prg2.minNoSeedBytes()];
                Arrays.fill(seed2, (byte)0);
                prg2.setSeed(seed2);

                PRingElement privateInput2 =
                    hom2.getDomain().randomElement(prg2, statDist);

                PGroupElement commonInput2 = hom2.map(privateInput2);

                 SigmaProof sigma2 = new SigmaProof("DemoSigmaProofSID2",
                                                   this,
                                                   challenger2,
                                                   prg2,
                                                   challengeBitLength,
                                                   batchBitLength,
                                                   statDist);
                if (j == 1) {
                    sigma2.prove(ui.getLog(), hom2, pGroup2.getg(),
                                 commonInput2, privateInput2, null);
                } else {
                    sigma2.verify(ui.getLog(), 1, hom2, pGroup2.getg(),
                                  commonInput2, null);
                }


                Challenger challenger3 =
                    new ChallengerRO(new HashfunctionHeuristic("SHA-512"),
                                     new byte[0]);

                PGroup pGroup3 = pGroup;

                int size = 100;
                BiPRingPGroup biExp3 = new BiFixedBaseExp(pGroup3, size);
                HomPRingPGroup hom3 = biExp3.restrict(pGroup3.getg());

                PRG prg3 = new PRGHeuristic();
                byte[] seed3 = new byte[prg3.minNoSeedBytes()];
                Arrays.fill(seed3, (byte)0);
                prg3.setSeed(seed3);

                PRingElement privateInput3 =
                    hom3.getDomain().randomElement(prg3, statDist);

                PGroupElement commonInput3 = hom3.map(privateInput3);

                 SigmaProof sigma3 = new SigmaProof("DemoSigmaProofSID3",
                                                  this,
                                                  challenger3,
                                                  prg3,
                                                  challengeBitLength,
                                                  batchBitLength,
                                                  statDist);
                if (j == 1) {
                    sigma3.prove(ui.getLog(), hom3, pGroup3.getg(),
                                 commonInput3, privateInput3, null);
                } else {
                    sigma3.verify(ui.getLog(), 1, hom3, pGroup3.getg(),
                                  commonInput3, null);
                }

            } catch (Exception e) {
                throw new DemoError("Unable to run demonstration!", e);
            }
        }
    }
}

