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

SUBDIRS = . gmpmee jgmpmee jecn verificatum

dist_noinst_DATA = .version.m4 checkinstall lazysetup

.PHONY : cleandoc


# We set up variables such that JGMPMEE can find the GMPMEE library,
# and Verificatum can find the JGMPMEE jar-file.
PWD := $(shell pwd)
export C_INCLUDE_PATH := $(C_INCLUDE_PATH):$(PWD)/gmpmee:$(PWD)/jecn
export LD_LIBRARY_PATH := $(LD_LIBRARY_PATH):$(PWD)/gmpmee/.libs:$(PWD)/jgmpmee/native/.libs:$(PWD)/jecn/native/.libs
export CLASSPATH := $(PWD)/jgmpmee/jgmpmee.jar:$(PWD)/jecn/jecn.jar:.:$(CLASSPATH):$(PWD)/verificatum/verificatum.jar


# The .verificatum_env file attempts to set all the necessary
# environment variables.
all-local: .verificatum_env
.verificatum_env:
	$(MAKE) -C verificatum .verificatum_env
	cp verificatum/.verificatum_env .

doc: .doc.stamp
.doc.stamp:
	$(MAKE) -C gmpmee doc 
	mkdir -p doc/gmpmee
	cp -R gmpmee/doc/* doc/gmpmee
	$(MAKE) -C jgmpmee doc
	mkdir -p doc/jgmpmee
	cp -R jgmpmee/doc/* doc/jgmpmee
	$(MAKE) -C jecn doc
	mkdir -p doc/jecn
	cp -R jecn/doc/* doc/jecn
	$(MAKE) -C verificatum doc
	mkdir -p doc/verificatum
	cp -R verificatum/doc/* doc/verificatum
	cp -R verificatum/doc/.verificatum_env doc/verificatum 
	@touch .doc.stamp

clean-local:
	rm -f .verificatum_env
	rm -rf doc
	rm -f .doc.stamp

cleandoc: clean-local
	$(MAKE) -C gmpmee cleandoc
	$(MAKE) -C jgmpmee cleandoc
	$(MAKE) -C jecn cleandoc
	$(MAKE) -C verificatum cleandoc
