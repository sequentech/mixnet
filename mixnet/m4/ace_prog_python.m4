AC_DEFUN([ACE_PROG_PYTHON],[
AC_CHECK_PROG([PYTHON], [python], [python], [no])

if test $PYTHON = no
then
   AC_MSG_ERROR([No python found in \$PATH. Please install Python 5!])
fi
])
