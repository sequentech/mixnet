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

.PHONY: clean cleandoc check doccheck jgmpmee

ACLOCAL_AMFLAGS=-I m4

JDK_DOC="http://java.sun.com/javase/6/docs/api/"

VFORK_DOC = doc
JAVAROOT = classes
BINDIR = bin

jarfile = $(PACKAGE).jar
jardir = $(datadir)/java
pkgpath = mixnet

CLASSPATH := .:$(JAVAROOT):$(pkgpath):$(CLASSPATH)

# Directory used for storing temporary testing files.
tmp_test_dir = .tmp_testing_dir

# Amount of memory used by the JVM.
#JVM_MEMORY_SIZE=-Xms1024m -Xmx6000m
JVM_MEMORY_SIZE=-Xmx4000m


# Choose wether to use JGMPMEE or not.
if JGMPMEE
   USE_JGMPMEE = USE_JGMPMEE
   JGMPMEEGREP = --invert-match MillerRabin
else
   USE_JGMPMEE = USE_PURE_JAVA
   JGMPMEEGREP = .
endif

# Choose wether to use hadoop or not.
if HADOOP
   HADOOPGREP = .
else
   HADOOPGREP = --invert-match hadoop
endif


# Space separated list of all Java source files.
java_files = $(shell find $(pkgpath) -name "*.java" \
| grep $(JGMPMEEGREP) | grep $(HADOOPGREP) | tr "\n" " ")


# Space separated list of all Test classes.
test_classes = $(shell find $(pkgpath) \
| grep -E "\.java|\.magic" \
| grep ".Test" | grep -v ".test." | grep -v "TestOpt" \
| sed -e s£\\.java££g | sed -e s£\\.magic££g | sed -e s£/£\\.£g \
| grep $(JGMPMEEGREP) | grep $(HADOOPGREP) | tr "\n" " ")

# Space separated list of all outer classes. Note that inner classes
# do not appear here, but this is of no consequence, since we only use
# these names to trigger the building rule below.
class_files = $(shell echo $(java_files) | sed -e s£\\.java£\\.class£g | sed -e s£$(pkgpath)£$(JAVAROOT)/$(pkgpath)£g)

# Resource text files that must be copied.
txt_files_src = $(shell find $(pkgpath) -name "*.txt" | tr "\n" " ")
txt_files := $(shell echo $(txt_files_src) | sed -e s£$(pkgpath)£$(JAVAROOT)/$(pkgpath)£g)

# FORK added vmnc
# Executable wrappers.
script_files = $(BINDIR)/javad $(BINDIR)/vmn $(BINDIR)/vmni $(BINDIR)/vmnv $(BINDIR)/vog $(BINDIR)/vspt $(BINDIR)/vmnc $(BINDIR)/vmnd

# Build a jar containing all class-files.
$(jarfile):  .classes_dir.stamp .preprocessed.stamp $(class_files) $(txt_files)
	cd $(JAVAROOT); $(JAR) cvf $(JARFLAGS) $@ *; cp $(jarfile) ..

.classes_dir.stamp:
	@mkdir -p classes
	@touch .classes_dir.stamp

# Makefile-rule for building a class file from its Java source.
$(JAVAROOT)/%.class : %.java
	$(JAVAC) -Xlint:unchecked -classpath $(CLASSPATH) -d $(JAVAROOT) $(AM_JAVACFLAGS) $(JAVACFLAGS) $<

# Makefile-rule for copying resource files of classes to the right
# location.
$(JAVAROOT)/%.txt : %.txt
	mkdir -p $(@D)
	cp $< $(@D)

# Generates some Java code to encapsulate explicit casts in a small
# number of classes.
$(pkgpath)/eio/Marshalizer.java: $(pkgpath)/eio/Marshalizer.gen
	@python type_safety.py

# Generates a simple class containing the version of this package.
$(pkgpath)/Version.java: $(pkgpath)/Version.gen
	@python version.py "$(PACKAGE_VERSION)"

# Preprocessing rule for generating java source files.
%.java : %.magic preprocessor.m4
	cat preprocessor.m4 $< | m4 > $@

# Files created by preprocessing magic files.
preprocessed_java = $(pkgpath)/arithm/LargeInteger.java $(pkgpath)/arithm/JECPGroup.java $(pkgpath)/arithm/JECPGroupElement.java $(pkgpath)/arithm/TestJECPGroup.java $(pkgpath)/arithm/LargeIntegerFixModPowTab.java $(pkgpath)/eio/Marshalizer.java $(pkgpath)/Version.java

# Compile all preprocessed java files.
.preprocessed.stamp: $(preprocessed_java)
	$(JAVAC) -Xlint:unchecked -classpath $(CLASSPATH) -d $(JAVAROOT) $(AM_JAVACFLAGS) $(JAVACFLAGS) $(preprocessed_java)
	@touch .preprocessed.stamp


# On some OS the JVM requires the flag "-d64" to use the 64-bit
# JVM. Since we are using native code, which on such machines is
# native 64-bit, we get a dynamic linking error unless we set up this
# JVM-flag correctly in our shell script wrappers.

if BITS64
   JVM_DATAMODEL=-d64 # 64-bit machine
else
   JVM_DATAMODEL=-d32 # 32-bit machine
endif


scriptmacros: preprocessor.m4
	@printf "define(JVM_DATAMODEL, $(JVM_DATAMODEL))dnl\n" > scriptmacros
	@printf "define(JVM_MEMORY_SIZE, $(JVM_MEMORY_SIZE))dnl\n" >> scriptmacros
	@printf "define(LIBDIR, $(libdir))dnl\n" >> scriptmacros
	@printf "define(INCLUDEDIR, $(includedir))dnl\n" >> scriptmacros
	@printf "define(BINDIR, $(bindir))dnl\n" >> scriptmacros
	@printf "define(JARDIR, $(jardir))" >> scriptmacros
	@printf "define(VFORK_VOG_BUILTIN, $(BCVOG))" >> scriptmacros

$(script_files): %: %.src scriptmacros
	cat scriptmacros $*.src | m4 > $@
	chmod +x $@


all-local: .mixnet_env
.mixnet_env: .mixnet_env.src scriptmacros
	cat scriptmacros .mixnet_env.src | m4 > .mixnet_env

install-data-hook:
	@echo ""
	@echo "================================================================"
	@echo ""
	@echo "          COMPLETING THE INSTALLATION"
	@echo ""
	@cat mixnet_env_info
	@echo ""
	@cat rndinit_info
	@echo ""
	@cat demo_info
	@echo ""
	@echo "================================================================"
	@echo ""

dist_bin = $(script_files) $(BINDIR)/vsort

jar_DATA = $(jarfile)
dist_noinst_DATA = demo $(pkgpath) TestLoadJGMPMEE.java TestLoadJGMPMEE.class TestLoadECN.java TestLoadECN.class type_safety.py gen_trialdiv.py .doc check_CLASSPATH check_LD_LIBRARY_PATH check_BIN checkinstall .version.m4 .mixnet_env.src mixnet_env_info rndinit_info demo_info preprocessing.m4 preprocessor.m4 .classes_dir.stamp .preprocessed.stamp $(JAVAROOT) $(jarfile)

EXTRA_DIST = $(BINDIR)/*.src


dist_bin_SCRIPTS = $(script_files) $(BINDIR)/vsort

# When we clean, we do not remove the classfiles.
cleanall: clean
	rm -f *.stamp .*.stamp
	rm -rf $(JAVAROOT)/*
	rm -f $(pkgpath)/arithm/LargeInteger.java
	rm -f $(pkgpath)/arithm/LargeIntegerFixModPowTab.java
	rm -f $(pkgpath)/arithm/JECPGroup.java
	rm -f $(pkgpath)/arithm/JECPGroupElement.java
	rm -f $(pkgpath)/arithm/TestJECPGroup.java
	rm -f $(pkgpath)/eio/Marshalizer.java
	rm -f $(pkgpath)/Version.java
	rm -f $(jarfile)

clean-local: cleandoc
	rm -rf $(script_files) scriptmacros \
               $(BINDIR)/*~ $(PACKAGE).jar \
               $(pkgpath)/*~ $(pkgpath)/*/*~ $(pkgpath)/*/*/*~ \
               $(pkgpath)/*/*/*/*~ \
               $(VFORK_DOC)/dcheck \
               $(tmp_test_dir)
	cd demo/mixnet; ./clean
	cd demo; ./clean
	rm -f .mixnet_env

check:
	mkdir -p $(tmp_test_dir)
	$(JAVA) \
	-Djava.security.egd=file:/dev/./urandom \
	-classpath $(CLASSPATH) \
	-Djava.library.path=$(LD_LIBRARY_PATH) \
	mixnet.test.Test $(tmp_test_dir) $(test_classes)
	rm -rf $(tmp_test_dir)

doc: .mixnet_env
	mkdir -p $(VFORK_DOC)
	cp .doc/html/* $(VFORK_DOC)
	cp .mixnet_env $(VFORK_DOC)
	mkdir -p $(VFORK_DOC)/api
	javadoc -classpath $(CLASSPATH) \
                -d $(VFORK_DOC)/api/ \
                -link $(JDK_DOC) \
                -bottom '<a href="{@docRoot}/copyright.html">Copyright</a>' \
                $(java_files)

cleandoc:
	rm -rf $(VFORK_DOC)
	rm -f .doc.stamp


# It seems that doccheck has gone out of fashion, but let's wait a
# little before this is removed.
#
#
# This target is used to verify that the documentation of the code
# satisfies some basic requirements. It is only used for development
# and requires manual download of the Doc Check utility from
# http://java.sun.com/j2se/javadoc/doccheck/. Extract the downloaded
# zipfile side-by-side with this Makefile.am.
#
# It seems Oracle has removed this utility from the Internet (August
# 2011). Remove this code if this has not changed in a year or so.
#
# doccheck:
# 	mkdir -p $(VFORK_DOC)/dcheck
# 	$(JAVADOC) -classpath $(pkgpath):$(CLASSPATH) -doclet com.sun.tools.doclets.doccheck.DocCheck -docletpath doccheck1.2b2/doccheck.jar -skipClass crypto.HashfunctionHeuristic.class $(JAVA_PATHS) -d $(VFORK_DOC)/dcheck -sourcepath . -subpackages $(pkgpath) -exclude $(pkgpath).election:$(pkgpath).submission
