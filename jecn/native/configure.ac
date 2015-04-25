
# Copyright 2011 Douglas Wikström

# This file is part of a package for Verificatum that provides native
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

AC_PREREQ([2.63])
AC_INIT([jecn], [0.1], [dog@csc.kth.se])
AC_CONFIG_MACRO_DIR([m4])
AC_CANONICAL_SYSTEM

#$host, $build and $target

# Checks for programs.
AC_PROG_CC

AC_LANG_PUSH([C])
AM_INIT_AUTOMAKE([-Wall -Werror -Wno-portability])
AC_CONFIG_MACRO_DIR([m4])
AC_CONFIG_SRCDIR([jecn_ECN.c])
AC_CONFIG_HEADERS([config.h])

# We are building a library.
AC_PROG_LIBTOOL

# Check for header files.
AC_CHECK_HEADERS([stdlib.h string.h unistd.h])

AC_MSG_NOTICE([Attempting to add locations of jni.h and jni_md.h.])

ACE_PROG_JAVAC
AX_JNI_INCLUDE_DIR

for JNI_INCLUDE_DIR in $JNI_INCLUDE_DIRS
do
    CPPFLAGS="$CPPFLAGS -I$JNI_INCLUDE_DIR"
done

AC_CHECK_HEADERS([jni_md.h jni.h], ,
       [AC_MSG_ERROR([

Unable to find jni.h or jni_md.h in the compiler's include path! These
header files are needed to compile C-code that can be called from a
JVM. They are included in a standard installation of Sun's Java 6
JDK. Normally, the include path is set using the environment variable
C_INCLUDE_PATH. On Ubuntu 10.04 you can use the following snippet:

JNI=`find /usr/lib/jvm -name jni.h`
JNI_MD=`find /usr/lib/jvm -name jni_md.h`
JDK_PATHS=\${JNI%jni.h}:\${JNI_MD%jni_md.h}
export C_INCLUDE_PATH=\${JDK_PATHS}:\${C_INCLUDE_PATH}

])])


# Checks for typedefs, structures, and compiler characteristics.
AC_TYPE_SIZE_T

# Checks for library functions.
AC_FUNC_MALLOC

AM_CONDITIONAL([BITS64], [test x`getconf LONG_BIT` = x64])

AC_CONFIG_FILES([Makefile])
AC_OUTPUT



