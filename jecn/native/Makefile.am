
# Copyright 2011 Douglas Wikström

# This file is part of a package for Vfork that provides native
# elliptic curve code (ECN).

# ECN is free software: you can redistribute it and/or modify it under
# the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.

# ECN is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
# or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General
# Public License for more details.

# You should have received a copy of the GNU Lesser General Public
# License along with ECN.  If not, see <http://www.gnu.org/licenses/>.


C_INCLUDE_PATH := $(C_INCLUDE_PATH):$(JNI_LOCATION)

ACLOCAL_AMFLAGS=-I m4

if BITS64
   AM_CFLAGS = -fPIC -m64
else
   AM_CFLAGS = -fPIC
endif

# Actual target.

lib_LTLIBRARIES = libjecn.la
libjecn_la_SOURCES = jecn_ECN.c convert.c convert.h array_alloc.c array_alloc_init.c array_clear_dealloc.c square.c mul.c exp.c sexp_init.c sexp_clear.c sexp_precomp.c sexp_table.c sexp_block_batch.c sexp.c ecn.h 
libjecn_la_LIBADD = -lgmp

nodist_include_HEADERS = jecn_ECN.h

clean-local:
	rm -f *~
