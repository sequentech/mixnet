
AC_DEFUN([ACE_PROG_JAR],[
AC_CHECK_PROG([JAR], [jar], [jar], [no])

if test $JAR = no
then
   AC_MSG_ERROR([No jar found in \$PATH. Please install JDK 6!])
fi
])
