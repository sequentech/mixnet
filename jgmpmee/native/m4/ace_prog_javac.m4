
AC_DEFUN([ACE_PROG_JAVAC],[
AC_CHECK_PROG([JAVAC], [javac], [javac], [no])

if test $JAVAC = no
then
   AC_MSG_ERROR([No javac found in \$PATH. Please install JDK 6!])
fi
])
