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

.PHONY: clean cleandoc

ACLOCAL_AMFLAGS=-I m4

JDK_DOC="http://java.sun.com/javase/6/docs/api/"

JAVAROOT = .

jarfile = $(PACKAGE).jar
jardir = $(datadir)/java
pkgpath = jgmpmee
jhdrout = native

CLASSPATH := $(pkgpath):$(CLASSPATH)

nativeheaders = $(pkgpath)/GMPMEE.java

$(jarfile): $(dist_noinst_JAVA) java-headers.stamp native
	$(JAR) cvf $(JARFLAGS) $@ $(pkgpath)/*.class

jar_DATA = $(jarfile)

java-headers.stamp: $(nativeheaders)
	@list="`echo $(nativeheaders) | sed -e 's|\.java||g' -e 's|/|.|g'`"; \
        for class in $$list; do \
          echo "$(JAVAH) -jni -d $(jhdrout) $(JAVAHFLAGS) $$class"; \
          CLASSPATH=$(CLASSPATH) $(JAVAH) -jni -d $(jhdrout) $(JAVAHFLAGS) $$class; \
        done
	@echo "JNI headers generated" > java-headers.stamp

CLEANFILES = $(jarfile) $(pkgpath)/*.class java-headers.stamp $(jhdrout)/jgmpmee_GMPMEE.h

dist_noinst_JAVA = jgmpmee/*.java

dist_noinst_DATA = .doc .version.m4

SUBDIRS = . native


doc: .doc.stamp
.doc.stamp:
	mkdir -p doc/
	cp -R .doc/html/* doc/
	mkdir -p doc/api
	javadoc -d doc/api/ -link $(JDK_DOC) jgmpmee/GMPMEE.java jgmpmee/JGMPMEE.java jgmpmee/FpowmTab.java jgmpmee/MillerRabin.java

cleandoc:
	rm -rf doc
	rm -f .doc.stamp

clean-local: cleandoc
	rm -f *~
	rm -f java-headers.stamp