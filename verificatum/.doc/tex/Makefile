# Copyright 2008 2009 2010 2011 Douglas Wikstrom

# This file is part of Verificatum.

# Verificatum is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.

# Verificatum is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.

# You should have received a copy of the GNU Lesser General Public
# License along with Verificatum.  If not, see
# <http://www.gnu.org/licenses/>.


.PHONY: clean

all: vmnv vmnum

vmnum.base: vmnum.tex vtm.bib vtm.sty
	latex vmnum.tex
	latex vmnum.tex
	bibtex vmnum
	latex vmnum.tex
	latex vmnum.tex

vmnum.pdf: vmnum.base
	pdflatex vmnum.tex

vmnum.html: vmnum.base
	htlatex vmnum.tex

vmnum: vmnum.pdf

################ vmnv ################


protInfo.xsd: gen_info_files
	vmni -schema protocol > protInfo.xsd

.info.stamp: gen_info_files
	rm -f *.xml
	VERIFICATUM_RANDOM_SOURCE=$(VERIFICATUM_RANDOM_SOURCE) ./gen_info_files .
	cat protInfo.xml | fold -w 73 > protInfo.tex
	cat privInfo1.xml | fold -w 73 > privInfo1.tex
	touch .info.stamp

test_vectors.tex: TestVectors.java
	javac TestVectors.java
	java TestVectors > test_vectors.tex

vmnv-0.1.base: vmnv-0.1.tex vtm.bib vtm.sty protInfo.xsd .info.stamp test_vectors.tex
	latex vmnv-0.1.tex
	latex vmnv-0.1.tex
	bibtex vmnv-0.1
	latex vmnv-0.1.tex
	latex vmnv-0.1.tex

vmnv-0.1.pdf: vmnv-0.1.base
	pdflatex vmnv-0.1.tex

vmnv-0.1.html: vmnv-0.1.base
	htlatex vmnv-0.1.tex

vmnv: vmnv-0.1.pdf

################ clean ################

clean:
	rm -f *.pdf *.ps
	rm -f *.bbl *.cb *.aux *.log *.dvi *~ *.cb2 *.blg
	rm -f *.toc *.ps *.pdf *.stamp .*.stamp *.4ct *.4tc *.png
	rm -f *.xml protInfo* privInfo* *.class
	rm -f *.css *.html *.idv *.lg *.xref *.tmp
	rm -f test_vectors.tex



