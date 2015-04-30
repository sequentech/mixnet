
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

package vfork.protocol.mixnet;

import vfork.crypto.*;
import vfork.protocol.*;
import vfork.eio.*;
import vfork.ui.info.*;
import vfork.util.*;

/**
 * Defines which information is stored in the protocol and private
 * info files, and also defines default values of some fields.
 *
 * @author Douglas Wikstrom
 */
public class MixNetElGamalGen implements InfoGenerator {

    public boolean compatible(String protocolVersion) {
        return MixNetElGamal.compatible(protocolVersion);
    }

    public String protocolVersion() {
        return MixNetElGamal.protocolVersion;
    }

    public String compatibleProtocolVersions() {
        return MixNetElGamal.compatibleProtocolVersions();
    }

    public ProtocolInfo newProtocolInfo() {
        ProtocolInfo pi = Protocol.newProtocolInfo();
        String s;

        s = "Group over which the protocol is executed. An instance of " +
            "vfork.arithm.PGroup.";
        pi.addInfoField(new StringField("pgroup", s, 1, 1));

        s = "Number of ciphertexts shuffled in parallel.";
        pi.addInfoField(new IntField("width", s, 1, 1));

        s = "Interface that defines how to communicate with the mix-net. " +
            "Possible values are \"raw\", \"native\", \"helios\", and " +
            "\"tvs\", or the name of a subclass of " +
            "vfork.protocol.mixnet.MixNetElGamalInterface. " +
            "See the user manual for more information on interfaces.";
        pi.addInfoField(new StringField("inter", s, 1, 1));

        s = "Maximal number of ciphertexts for which precomputation is " +
            "performed. If this is set to zero, then it is assumed that " +
            "precomputation is not performed as a separate phase, i.e., it " +
            "defaults to the number of submitted ciphertexts during mixing.";
        pi.addInfoField(new IntField("maxciph", s, 1, 1));

        s = "Decides statistical error in distribution.";
        pi.addInfoField(new IntField("statdist", s, 1, 1));

        s = "Bit length of challenges in interactive proofs.";
        pi.addInfoField(new IntField("cbitlen", s, 1, 1));

        s = "Bit length of challenges in non-interactive random-oracle proofs.";
        pi.addInfoField(new IntField("cbitlenro", s, 1, 1));

        s = "Bit length of each component in random vectors used for batching.";
        pi.addInfoField(new IntField("vbitlen", s, 1, 1));

        s = "Bit length of each component in random vectors used for " +
            "batching in non-interactive random-oracle proofs.";
        pi.addInfoField(new IntField("vbitlenro", s, 1, 1));

        s = "Pseudo random generator used to derive random vectors from " +
            "jointly generated seeds. This can be one of the strings " +
            "\"SHA-256\", \"SHA-384\", or \"SHA-512\", in which case " +
            "vfork.crypto.PRGHeuristic is instantiated based on this " +
            "hashfunction, or it can be an instance of " +
            "vfork.crypto.PRG.";
        pi.addInfoField(new StringField("prg", s, 1, 1));

        s = "Hashfunction used to implement random oracles. It can be " +
            "one of the strings \"SHA-256\", \"SHA-384\", or \"SHA-512\", " +
            "in which case vfork.crypto.HashfunctionHeuristic is " +
            "is instantiated, or an " +
            "instance of vfork.crypto.Hashfunction. Random oracles " +
            "with various output lengths are then implemented, using the " +
            "given hashfunction, in vfork.crypto.RandomOracle." +
            "\n" +
            "WARNING! Do not change the default unless you know exactly what " +
            "you are doing.";
        pi.addInfoField(new StringField("rohash", s, 1, 1));

        s = "Determines if the proofs of correctness of an execution are " +
            "interactive or non-interactive " +
            "(\"interactive\" or \"noninteractive\").";
        pi.addInfoField(new StringField("corr", s, 1, 1));

        return pi;
    }

    /**
     * Adds some default values to the given protocol info.
     *
     * @param pi Protocol info to be initialized.
     */
    public static void addDefaultValues(ProtocolInfo pi) {

        // ProtocolInfo.SESSIONID must be given by the user.
        // ProtocolInfo.NAME must be given by the user.
        // ProtocolInfo.NOPARTIES must be given by the user.
        // ProtocolInfo.THRESHOLD must be given by the user.

        pi.addValue(ProtocolInfo.DESCRIPTION, "");
        pi.addValue("statdist", ProtocolDefaults.STAT_DIST);
        pi.addValue("pgroup", ProtocolDefaults.PGroup());
        pi.addValue("width", 1);
        pi.addValue("cbitlen", ProtocolDefaults.SEC_PARAM_CHALLENGE);
        pi.addValue("cbitlenro", ProtocolDefaults.SEC_PARAM_CHALLENGE_RO);
        pi.addValue("vbitlen", ProtocolDefaults.SEC_PARAM_BATCH);
        pi.addValue("vbitlenro", ProtocolDefaults.SEC_PARAM_BATCH_RO);
        pi.addValue("prg", ProtocolDefaults.PRG());
        pi.addValue("rohash", ProtocolDefaults.Hashfunction());
        pi.addValue("corr", "noninteractive");
        pi.addValue("inter", "native");

        // A value of zero means that precomputation as a separate
        // phase is illegal, i.e., precomputation takes place at
        // mixing time for the actual number of ciphertexts.
        pi.addValue("maxciph", 0);
    }

    public ProtocolInfo defaultProtocolInfo() {
        ProtocolInfo pi = newProtocolInfo();

        addDefaultValues(pi);

        return pi;
    }

    public PrivateInfo newPrivateInfo() {
        PrivateInfo pi = Protocol.newPrivateInfo();
        String s;

        s = "Determines the key generation algorithm used to generate " +
            "keys for the cryptosystem used in subprotocols. An instance of " +
            "vfork.crypto.CryptoKeyGen.";
        pi.addInfoField(new StringField("keygen", s, 1, 1));

        s = "Determines if arrays of group/field elements and integers are " +
            "stored in (possibly virtual) RAM or on file. The latter is " +
            "slower, but can accomodate larger arrays (\"ram\" or \"file\").";
        pi.addInfoField(new StringField("arrays", s, 1, 1));

        return pi;
    }

    public PrivateInfo defaultPrivateInfo(ProtocolInfo protocolInfo,
                                          RandomSource rs) {
        PrivateInfo pi = newPrivateInfo();

        // PrivateInfo.NAME must be given by the user.

        pi.addValue(PrivateInfo.PRIV_KEY,
                    ProtocolDefaults.LazySignatureSKey(rs));
        pi.addValue("keygen", ProtocolDefaults.CryptoKeyGen());

        pi.addValue(PrivateInfo.DIR, ProtocolDefaults.DIR());
        pi.addValue(PrivateInfo.RANDOMNESS, ProtocolDefaults.RandomDevice());
        pi.addValue(PrivateInfo.CERTAINTY, ProtocolDefaults.CERTAINTY);

        pi.addValue(Protocol.HTTPL, ProtocolDefaults.HTTPL());
        pi.addValue(Protocol.HTTP_DIR, ProtocolDefaults.HTTP_DIR());
        pi.addValue(Protocol.HTTP_TYPE, ProtocolDefaults.HTTP_TYPE);

        pi.addValue(Protocol.HINTL, ProtocolDefaults.HINTL());

        pi.addValue("arrays", "ram");

        return pi;
    }


    public PartyInfo newPartyInfo(ProtocolInfo protocolInfo) {
        return protocolInfo.getFactory().newInstance();
    }

    public PartyInfo defaultPartyInfo(final ProtocolInfo protocolInfo,
                                      final PrivateInfo privateInfo,
                                      final RandomSource rs) {
        PartyInfo pi = newPartyInfo(protocolInfo);

        // PrivateInfo.NAME must be given by the user.

        pi.addValue(PartyInfo.SORT_BY_ROLE, "anyrole");

        pi.addValue(PartyInfo.PUB_KEY,
        new Lazy() {
            public String gen() {
                try {
                    Lazy lazyKeyPair =
                        (Lazy)privateInfo.getValue(PrivateInfo.PRIV_KEY);
                    String keyPairString = lazyKeyPair.gen();
                    SignatureKeyPair keyPair = Marshalizer.
                        unmarshalHexAux_SignatureKeyPair(keyPairString,
                                                         rs,
                                                         50);

                    return Marshalizer.marshalToHexHuman(keyPair.getPKey(),
                                                         true);
                } catch (EIOException eioe) {
                    throw new ProtocolError("Failed to extract public key!",
                                            eioe);
                }
            }
        });

        pi.addValue(PartyInfo.DESCRIPTION, "");

        pi.addValue(Protocol.HTTP, ProtocolDefaults.HTTP());
        pi.addValue(Protocol.HINT, ProtocolDefaults.HINT());

        return pi;
    }

}