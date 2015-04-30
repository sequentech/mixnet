
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
 * Demonstrates the execution of a {@link PoSTW}.
 *
 * @author Douglas Wikstrom
 */
public class DemoPoSTW extends DemoPlainKeys {

    // These methods are documented in DemoProtocolFactory.java.

    public Runnable newProtocol(PrivateInfo privateInfo,
                                ProtocolInfo protocolInfo,
                                UI ui) {
        return new ExecPoSTW(privateInfo, protocolInfo, ui);
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

        Demo demo = new Demo(args, new DemoPoSTW());
    }

    /**
     * Turns {@link PoSTW} into a runnable object.
     */
    class ExecPoSTW extends Protocol
        implements Runnable {

        public ExecPoSTW(PrivateInfo privateInfo,
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
                int size = 100;

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


                PRG prg1 = new PRGHeuristic();
                byte[] seed1 = new byte[prg1.minNoSeedBytes()];
                Arrays.fill(seed1, (byte)0);
                prg1.setSeed(seed1);


                PGroupElement g = pGroup.getg();
                PGroupElementArray h =
                    g.exp(pGroup.getPRing().randomElementArray(size,
                                                               prg1,
                                                               statDist));
                PRingElementArray r =
                    pGroup.getPRing().randomElementArray(size, prg1, statDist);
                PGroupElementArray u = g.exp(r).mul(h);
                Permutation pi = new Permutation(size, randomSource, statDist);

                u = u.permute(pi);

                if (j == 1) {

                    PoSTW P = new PoSTW("",
                                        this,
                                        challenger1,
                                        prg1,
                                        challengeBitLength,
                                        batchBitLength,
                                        statDist);
                    P.prove(ui.getLog(), g, h, u, r, pi, null);


                } else {

                    PoSTW V = new PoSTW("",
                                        this,
                                        challenger1,
                                        prg1,
                                        challengeBitLength,
                                        batchBitLength,
                                        statDist);
                    V.verify(ui.getLog(), 1, g, h, u, null);
                }

            } catch (Exception e) {
                throw new DemoError("Unable to run demonstration!", e);
            }
        }
    }
}

