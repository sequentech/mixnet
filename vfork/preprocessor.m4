
define(`USE_JGMPMEE')
define(`USE_JECN')
ifdef(`USE_JGMPMEE',`define(`JGMPMEE_CODE')',`define(`JGMPMEE_PURE_JAVA_CODE')')
ifdef(`USE_JECN',`define(`JECN_CODE')',`define(`JECN_PURE_JAVA_CODE')')
dnl
ifdef(`JGMPMEE_PURE_JAVA_CODE',dnl
`define(`JGMPMEE_BEGIN',`Removed native code here.divert(-1)')dnl
define(`JGMPMEE_END',`divert')dnl
define(`JGMPMEE_PURE_JAVA_BEGIN',`Enabled pure java code begins here.')dnl
define(`JGMPMEE_PURE_JAVA_END',`Enabled pure java code ends here')')dnl
dnl
ifdef(`JGMPMEE_CODE',dnl
`define(`JGMPMEE_PURE_JAVA_BEGIN',`Removed pure java code here.divert(-1)')dnl
define(`JGMPMEE_PURE_JAVA_END',`divert')dnl
define(`JGMPMEE_BEGIN',`Enabled calls to native code begins here.')dnl
define(`JGMPMEE_END',`Enabled calls to native code ends here')')dnl
dnl
ifdef(`JECN_PURE_JAVA_CODE',dnl
`define(`JECN_BEGIN',`Removed native code here.divert(-1)')dnl
define(`JECN_END',`divert')dnl
define(`JECN_PURE_JAVA_BEGIN',`Enabled pure java code begins here.')dnl
define(`JECN_PURE_JAVA_END',`Enabled pure java code ends here')')dnl
dnl
ifdef(`JECN_CODE',dnl
`define(`JECN_PURE_JAVA_BEGIN',`Removed pure java code here.divert(-1)')dnl
define(`JECN_PURE_JAVA_END',`divert')dnl
define(`JECN_BEGIN',`Enabled calls to native code begins here.')dnl
define(`JECN_END',`Enabled calls to native code ends here')')dnl
dnl
undefine(`format')dnl