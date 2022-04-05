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

#                                               -*- Autoconf -*-
# Process this file with autoconf to produce a configure script.

AC_PREREQ([2.63])
AC_INIT([mixnet-full], [m4_esyscmd([m4 .version.m4])], [dog@csc.kth.se])
AC_CANONICAL_SYSTEM # Must appear before AM_INIT_AUTOMAKE
AM_INIT_AUTOMAKE([tar-ustar -Wall -Werror -Wno-portability])

# Avoid complaints about jgmpmee not being installed.
ac_configure_args="$ac_configure_args --enable-jgmpmee --disable-check_gmpmee --disable-check_jgmpmee --enable-jecn --disable-check_jecn"

AC_CONFIG_SUBDIRS([gmpmee jgmpmee jecn mixnet])

AC_CONFIG_FILES([Makefile])
AC_OUTPUT
