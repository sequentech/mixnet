
AC_DEFUN([ACE_PROG_JAVAH],[
AC_CHECK_PROG([JAVAH], [javah], [javah], [no])

if test $JAVAH = no
then
   AC_MSG_ERROR([No javah found in \$PATH. Please install JDK 6!])
fi
])
