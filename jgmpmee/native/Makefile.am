# Copyright 2008 Douglas Wikström

# This file is part of Java GMP Modular Exponentiation Extension
# (JGMPMEE).

# JGMPMEE is free software: you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.

# JGMPMEE is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.

# You should have received a copy of the GNU Lesser General Public
# License along with JGMPMEE.  If not, see
# <http://www.gnu.org/licenses/>.

C_INCLUDE_PATH := $(C_INCLUDE_PATH):$(JNI_LOCATION)

ACLOCAL_AMFLAGS=-I m4

if BITS64
   AM_CFLAGS = -fPIC -m64
else
   AM_CFLAGS = -fPIC
endif

# Actual target.

lib_LTLIBRARIES = libjgmpmee.la
libjgmpmee_la_SOURCES = jgmpmee_GMPMEE.c convert.c convert.h 
libjgmpmee_la_LIBADD = -lgmp -lgmpmee

nodist_include_HEADERS = jgmpmee_GMPMEE.h

clean-local:
	rm -f *~
