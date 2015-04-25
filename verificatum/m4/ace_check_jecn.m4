
AC_DEFUN([ACE_CHECK_JECN],[
AC_REQUIRE([ACE_PROG_JAVA])

AC_ARG_ENABLE([check_jecn],
     [  --disable-check_jecn    Skip checking that JECN is installed.],
     [],[# Check for non-standard library we need.
ace_res=$($JAVA $JAVAFLAGS -classpath ../jecn/jecn.jar:$CLASSPATH TestLoadECN)

echo -n "checking for jecn.jar... "
if test "x$ace_res" = x;
then
   echo "yes"
else
   echo "no"
   AC_MSG_ERROR([$ace_res Please make sure that JECN is installed (found at www.verificatum.com) and that your \$CLASSPATH points to the proper location. You may use the option --disable-check_jecn to ignore this error if you know that you will install this package later.])
fi
])
])
