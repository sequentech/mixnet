
AC_DEFUN([ACE_PROG_JAVADOC],[
AC_CHECK_PROG([JAVADOC], [javadoc], [javadoc], [no])

if test $JAVADOC = no
then
   AC_MSG_ERROR([No javadoc found in \$PATH. Please install JDK 6!])
fi
])
