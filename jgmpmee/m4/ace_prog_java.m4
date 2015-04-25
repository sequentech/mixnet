
AC_DEFUN([ACE_PROG_JAVA],[
AC_CHECK_PROG([JAVA], [java], [java], [no])

if test $JAVA = no
then
   AC_MSG_ERROR([No java found in \$PATH. Please install JDK 6!])
fi
])
