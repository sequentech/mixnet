
AC_DEFUN([ACE_CHECK_JGMPMEE],[
AC_REQUIRE([ACE_PROG_JAVA])

AC_ARG_ENABLE([check_jgmpmee],
     [  --disable-check_jgmpmee    Skip checking that JGMPMEE is installed.],
     [],[# Check for non-standard library we need.
ace_res=$($JAVA $JAVAFLAGS -classpath ../jgmpmee/jgmpmee.jar:$CLASSPATH TestLoadJGMPMEE)

echo -n "checking for jgmpmee.jar... "
if test "x$ace_res" = x;
then
   echo "yes"
else
   echo "no"
   AC_MSG_ERROR([$ace_res Please make sure that JGMPMEE is installed (found at www.mixnet.com) and that your \$CLASSPATH points to the proper location. You can check your JGMPMEE installation using 'java jgmpmee.Test'. This should give you a usage description. If you get a NoClassDefFoundError, then the proper jgmpmee.jar file can not be found. You may use the option --disable-check_jgmpmee to ignore this error if you know that you will install this package later.])
fi
])
])
