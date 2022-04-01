#!/usr/bin/python

# Copyright 2008 2009 2010 Douglas Wikstrom

# This file is part of Vfork.

# Vfork is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.

# Vfork is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.

# You should have received a copy of the GNU Lesser General Public
# License along with Vfork.  If not, see
# <http://www.gnu.org/licenses/>.


import os
import sys
import re

import string

# This file generates methods for:
#    mixnet.eio.Marshalizer.java
#    mixnet.eio.HexConverter.java
#
# Please consult the documentation of these classes for more
# information.


######################### Marshalizer.java #########################

def gen_unmarshal_TYPE(theType, auxParams):

    s = "    /**\n" \
    + "    * Converts the input into an instance of {@link " \
    + theType + "}.\n" \
    + "    *\n" \
    + "    * @param bt Representation of the output.\n"

    if auxParams:
        s = s \
    + "    * @param rs Source of randomness used in testing.\n" \
    + "    * @param certainty Determines the probability of accepting a\n" \
    + "    * an incorrect input.\n"

    s = s \
    + "    * @return An instance corresponding to the input.\n" \
    + "    *\n" \
    + "    * @throws EIOException If the input does not represent an\n" \
    + "    * instance.\n" \
    + "    */\n" \
    + '    @SuppressWarnings("unchecked")\n' \
    + "    public static " + theType + "\n"  \
    + "        unmarshal"

    if auxParams:
        s = s + "Aux"

    s = s \
    + "_" + theType \
    + "(ByteTreeReader btr"

    if auxParams:
        s = s + ", RandomSource rs, int certainty"

    s = s \
    +")\n" \
    + "    throws EIOException {\n" \
    + "        Object obj = unmarshal"

    if auxParams:
        s = s + "Aux"

    s = s \
    + "(btr"

    if auxParams:
        s = s + ", rs, certainty"

    s = s \
    + ");\n" \
    + "        if (obj instanceof " + theType + ") {\n" \
    + "            return (" + theType + ")obj;\n" \
    + "        } else {\n" \
    + '            throw new EIOException("Type does not match cast!");\n' \
    + "        }\n" \
    + "    }\n";
    return s;

def gen_unmarshal_TYPE_methods(methodNames):
    s = ""
    for methodName in methodNames:
        (methodType, typeName) = methodName.split("_")
        s = s + gen_unmarshal_TYPE(typeName, methodType.endswith("Aux")) + "\n"
    return s


######################### HexConverter.java #########################

def gen_unmarshalHex_TYPE(theType, auxParams):
    s = "    /**\n" \
    + "    * Converts the input into an instance of {@link " \
    + theType + "}.\n" \
    + "    *\n" \
    + "    * @param hex Hex code representation of the output.\n"

    if auxParams:
        s = s \
    + "    * @param rs Source of randomness used in testing.\n" \
    + "    * @param certainty Determines the probability of accepting a\n" \
    + "    * an incorrect input.\n"

    s = s \
    + "    * @return An instance corresponding to the input.\n" \
    + "    *\n" \
    + "    * @throws EIOException If the input does not represent an\n" \
    + "    * instance.\n" \
    + "    */\n" \
    + '    @SuppressWarnings("unchecked")\n' \
    + "    public static " + theType + "\n"  \
    + "        unmarshalHex"

    if auxParams:
        s = s + "Aux"

    s = s \
    + "_" + theType \
    + "(String hex"

    if auxParams:
        s = s + ", RandomSource rs, int certainty"

    s = s \
    + ")\n" \
    + "    throws EIOException {\n" \
    + "        Object obj = unmarshalHex"

    if auxParams:
        s = s + "Aux"

    s = s + "(hex"

    if auxParams:
        s = s + ", rs, certainty"

    s = s \
    + ");\n" \
    + "        if (obj instanceof " + theType + ") {\n" \
    + "            return (" + theType + ")obj;\n" \
    + "        } else {\n" \
    + '            throw new EIOException("Type does not match cast!");\n' \
    + "        }\n" \
    + "    }\n";
    return s;

def gen_unmarshalHex_TYPE_methods(methodNames):
    s = ""
    for methodName in methodNames:
        (methodType, typeName) = methodName.split("_")
        s = s + gen_unmarshalHex_TYPE(typeName, methodType.endswith("Aux")) + "\n"
    return s




def uniqueMatches(matchString, greps):
    matches = re.findall(matchString + "_.*\(", greps)
    matches = \
        matches + re.findall(matchString + "Aux_.*\(", greps)

    cleanMatches = []
    for item in matches:
        cleanMatches.append(item.split("(")[0])
    matches = cleanMatches

    matches.sort()
    current = ""
    uniques = []
    for item in matches:
        if item != current:
            uniques.append(item)
            current = item
    return uniques

print "Generating Marshalizer.java...",


grepOutput = os.popen('grep -R "Marshalizer.unmarshal" mixnet | grep -v "TYPE" | grep ".java:"').read()
um = uniqueMatches("unmarshal", grepOutput)
methods = gen_unmarshal_TYPE_methods(um)

grepOutput = os.popen('grep -R "Marshalizer.unmarshalHex" mixnet | grep -v "TYPE" | grep ".java:"').read()
um = uniqueMatches("unmarshalHex", grepOutput)
methodsHex = gen_unmarshalHex_TYPE_methods(um)

f = open("mixnet/eio/Marshalizer.gen")
content = f.read()
f.close()

f = open("mixnet/eio/Marshalizer.java", "w")
f.write(content.replace("/* TYPESAFETYBRIDGE */", methods + methodsHex))
f.close()

print "done."
