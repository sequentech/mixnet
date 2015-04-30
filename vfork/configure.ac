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

AC_PREREQ([2.63])
AC_INIT([vfork], [m4_esyscmd([m4 .version.m4])], [dog@csc.kth.se])
AC_CANONICAL_SYSTEM # Must appear before AM_INIT_AUTOMAKE
AM_INIT_AUTOMAKE([tar-ustar -Wall -Werror -Wno-portability])
AC_CONFIG_MACRO_DIR([m4])

AM_PATH_PYTHON

# Checks for development tools we need.
ACE_PROG_JAVAC
ACE_PROG_JAVAH
ACE_PROG_JAR
ACE_PROG_JAVADOC
ACE_PROG_JAVA
ACE_PROG_PYTHON


AC_ARG_ENABLE([jgmpmee],
     [  --enable-jgmpmee    Speed up modular arithmetic using GMP and JGMPMEE.],
     [case "${enableval}" in
       yes) jgmpmee=true ;

            # Checks for libraries.
            ACE_CHECK_JGMPMEE;;

       no)  jgmpmee=false ;;
      esac],[jgmpmee=false])
AM_CONDITIONAL([JGMPMEE], [test x$jgmpmee = xtrue])

AC_ARG_ENABLE([jecn],
     [  --enable-jecn    Speed up elliptic curve arithmetic using GMP and JECN.],
     [case "${enableval}" in
       yes) jecn=true ;

            # Checks for libraries.
            ACE_CHECK_JECN;;

       no)  jecn=false ;;
      esac],[jecn=false])
AM_CONDITIONAL([JECN], [test x$jecn = xtrue])


# We need to figure out if we need to pass -J-d64 to javac.
AM_CONDITIONAL([BITS64], [test x`getconf LONG_BIT` = x64])

AC_ARG_ENABLE([hadoop],
     [  --enable-hadoop    Allow distributing large integer operation using hadoop.],
     [case "${enableval}" in
       yes) hadoop=true ;;
       no)  hadoop=false ;;
      esac],[hadoop=false])
AM_CONDITIONAL([HADOOP], [test x$hadoop = xtrue])


AC_CONFIG_FILES([Makefile])

# Build macro for preprocessing based on the configuration. This is
# done in such a way that if configure changes any parameters that
# have an effect on the preprocessing, then the preprocessing script
# is rebuilt. This then triggers preprocessed files to be rebuilt,
# since preprocessed files depend on preprocessor.m4 in Makefile.am.

echo "" > preprocessor_new.m4

if test x$jgmpmee = xtrue; then
   echo ["define(\`USE_JGMPMEE')"] >> preprocessor_new.m4
fi

if test x$jecn = xtrue; then
   echo ["define(\`USE_JECN')"] >> preprocessor_new.m4
fi

cat preprocessing.m4 >> preprocessor_new.m4
if test ! -f preprocessor.m4
then
   touch preprocessor.m4
fi
if test ! "`diff preprocessor_new.m4 preprocessor.m4`"x = x
then
   mv preprocessor_new.m4 preprocessor.m4
fi
rm -f preprocessor_new.m4

AC_OUTPUT



